package org.basex.data;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.index.ft.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.util.*;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed disk structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;
  /** Closed flag. */
  private boolean closed;

  /**
   * Default constructor, called from {@link Open#open}.
   * @param meta meta data
   * @throws IOException I/O Exception
   */
  public DiskData(final MetaData meta) throws IOException {
    super(meta);

    try(final DataInput in = new DataInput(meta.dbfile(DATAINF))) {
      meta.read(in);
      while(true) {
        final String k = string(in.readToken());
        if(k.isEmpty()) break;
        switch(k) {
          case DBTAGS: elemNames = new Names(in, meta); break;
          case DBATTS: attrNames = new Names(in, meta); break;
          case DBPATH: paths = new PathIndex(this, in); break;
          case DBNS:   nspaces = new Namespaces(in); break;
          case DBDOCS: resources.read(in); break;
        }
      }
    }

    // open data and indexes
    init();
    if(meta.updindex) {
      idmap = new IdPreMap(meta.dbfile(DATAIDP));
      if(meta.textindex) textIndex = new UpdatableDiskValues(this, IndexType.TEXT);
      if(meta.attrindex) attrIndex = new UpdatableDiskValues(this, IndexType.ATTRIBUTE);
      if(meta.tokenindex) tokenIndex = new UpdatableDiskValues(this, IndexType.TOKEN);
    } else {
      if(meta.textindex) textIndex = new DiskValues(this, IndexType.TEXT);
      if(meta.attrindex) attrIndex = new DiskValues(this, IndexType.ATTRIBUTE);
      if(meta.tokenindex) tokenIndex = new DiskValues(this, IndexType.TOKEN);
    }
    if(meta.ftindex) ftIndex = new FTIndex(this);
  }

  /**
   * Internal database constructor, called from {@link DiskBuilder#build}.
   * @param meta meta data
   * @param elemNames element names
   * @param attrNames attribute names
   * @param paths path index
   * @param nspaces namespaces
   * @throws IOException I/O Exception
   */
  public DiskData(final MetaData meta, final Names elemNames, final Names attrNames,
      final PathIndex paths, final Namespaces nspaces) throws IOException {

    super(meta);
    this.elemNames = elemNames;
    this.attrNames = attrNames;
    this.paths = paths;
    this.nspaces = nspaces;
    paths.data(this);
    if(meta.updindex) idmap = new IdPreMap(meta.lastid);
    init();
  }

  /**
   * Initializes the database.
   * @throws IOException I/O exception
   */
  private void init() throws IOException {
    table = new TableDiskAccess(meta, false);
    texts = new DataAccess(meta.dbfile(DATATXT));
    values = new DataAccess(meta.dbfile(DATAATV));
  }

  /**
   * Writes all meta data to disk.
   * @throws IOException I/O exception
   */
  private void write() throws IOException {
    if(!meta.dirty) return;

    try(final DataOutput out = new DataOutput(meta.dbfile(DATAINF))) {
      meta.write(out);
      out.writeToken(token(DBTAGS));
      elemNames.write(out);
      out.writeToken(token(DBATTS));
      attrNames.write(out);
      out.writeToken(token(DBPATH));
      paths.write(out);
      out.writeToken(token(DBNS));
      nspaces.write(out);
      out.writeToken(token(DBDOCS));
      resources.write(out);
      out.write(0);
    }
    if(meta.updindex) idmap.write(meta.dbfile(DATAIDP));
    meta.dirty = false;
  }

  @Override
  public synchronized void close() {
    if(closed) return;
    closed = true;
    try {
      write();
      table.close();
      texts.close();
      values.close();
      close(IndexType.TEXT);
      close(IndexType.ATTRIBUTE);
      close(IndexType.TOKEN);
      close(IndexType.FULLTEXT);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes the specified index.
   * @param type index to be closed
   */
  private synchronized void close(final IndexType type) {
    // close index and invalidate reference
    final Index index = index(type);
    if(index != null) {
      index.close();
      set(type, null);
    }
  }

  @Override
  public void createIndex(final IndexType type, final Command cmd) throws IOException {
    // close existing index
    close(type);
    final IndexBuilder ib;
    switch(type) {
      case TEXT: case ATTRIBUTE: case TOKEN: ib = new DiskValuesBuilder(this, type); break;
      case FULLTEXT: ib = new FTBuilder(this); break;
      default: throw Util.notExpected();
    }
    if(cmd != null) cmd.job(ib);
    set(type, ib.build());
  }

  @Override
  public void dropIndex(final IndexType type) throws BaseXException {
    close(type);
    final Index index = index(type);
    if(index != null && !index.drop()) throw new BaseXException(INDEX_NOT_DROPPED_X, type);
  }

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param index index instance
   */
  private void set(final IndexType type, final ValueIndex index) {
    meta.dirty = true;
    switch(type) {
      case TEXT:      textIndex = index; break;
      case ATTRIBUTE: attrIndex = index; break;
      case TOKEN:     tokenIndex = index; break;
      case FULLTEXT:  ftIndex = index; break;
      default:        break;
    }
  }

  @Override
  public void startUpdate(final MainOptions opts) throws IOException {
    if(!table.lock(true)) throw new BaseXException(DB_PINNED_X, meta.name);
    if(opts.get(MainOptions.AUTOFLUSH)) {
      final IOFile uf = meta.updateFile();
      if(uf.exists()) throw new BaseXException(DB_UPDATED_X, meta.name);
      if(!uf.touch()) throw Util.notExpected("%: could not create lock file.", meta.name);
    }
  }

  @Override
  public synchronized void finishUpdate(final MainOptions opts) {
    // remove updating file
    final boolean auto = opts.get(MainOptions.AUTOFLUSH);
    if(auto) {
      final IOFile uf = meta.updateFile();
      if(!uf.exists()) throw Util.notExpected("%: lock file does not exist.", meta.name);
      if(!uf.delete()) throw Util.notExpected("%: could not delete lock file.", meta.name);
    }

    // db:optimize(..., true) will close the database before this function is called
    if(!closed) {
      flush(auto);
      if(!table.lock(false)) throw Util.notExpected("Database '%': could not unlock.", meta.name);
    }
  }

  @Override
  public synchronized void flush(final boolean all) {
    try {
      table.flush(all);
      if(all) {
        write();
        texts.flush();
        values.flush();
        if(textIndex != null) textIndex.flush();
        if(attrIndex != null) attrIndex.flush();
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  @Override
  public byte[] text(final int pre, final boolean text) {
    final long o = textRef(pre);
    return number(o) ? token((int) o) : txt(o, text);
  }

  @Override
  public long textItr(final int pre, final boolean text) {
    final long o = textRef(pre);
    return number(o) ? o & IO.OFFNUM - 1 : toLong(txt(o, text));
  }

  @Override
  public double textDbl(final int pre, final boolean text) {
    final long o = textRef(pre);
    return number(o) ? o & IO.OFFNUM - 1 : toDouble(txt(o, text));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    final long o = textRef(pre);
    if(number(o)) return numDigits((int) o);
    final DataAccess da = text ? texts : values;
    final int l = da.readNum(o & IO.OFFCOMP - 1);
    // compressed: next number contains number of compressed bytes
    return compressed(o) ? da.readNum() : l;
  }

  /**
   * Returns a text (text, comment, pi) or attribute value.
   * @param off text offset
   * @param text text or attribute flag
   * @return text
   */
  private byte[] txt(final long off, final boolean text) {
    final byte[] txt = (text ? texts : values).readToken(off & IO.OFFCOMP - 1);
    return compressed(off) ? Compress.unpack(txt) : txt;
  }

  /**
   * Returns true if the specified value contains a number.
   * @param offset offset
   * @return result of check
   */
  private static boolean number(final long offset) {
    return (offset & IO.OFFNUM) != 0;
  }

  /**
   * Returns true if the specified value references a compressed token.
   * @param offset offset
   * @return result of check
   */
  private static boolean compressed(final long offset) {
    return (offset & IO.OFFCOMP) != 0;
  }

  @Override
  public boolean inMemory() {
    return false;
  }

  // UPDATE OPERATIONS ========================================================

  @Override
  protected void delete(final int pre, final boolean text) {
    // old entry (offset or value)
    final long old = textRef(pre);
    // fill unused space with zero-bytes
    if(!number(old)) (text ? texts : values).free(old & IO.OFFCOMP - 1, 0);
  }

  @Override
  protected void updateText(final int pre, final byte[] value, final int kind) {
    // delete existing index entry
    indexDelete(pre, -1, 1);

    // reference to heap file
    final DataAccess store = kind == ATTR ? values : texts;
    // old entry (offset or value)
    final long oldRef = textRef(pre);

    // check if new entry is numeric and can be inlined
    final long v = toSimpleInt(value);
    if(v != Integer.MIN_VALUE) {
      // invalidate old entry if it was not inlined
      if(!number(oldRef)) store.free(oldRef & IO.OFFCOMP - 1, 0);
      // inline integer value
      textRef(pre, v | IO.OFFNUM);
    } else {
      // otherwise, try to compress new value
      final byte[] val = Compress.pack(value);

      // choose inserting position
      final long off;
      if(number(oldRef)) {
        // old entry was numeric: append new entry to heap file
        off = store.length();
      } else {
        // otherwise, compute inserting position and invalidate old entry
        final int vl = val.length;
        off = store.free(oldRef & IO.OFFCOMP - 1, vl + Num.length(vl));
      }

      store.writeToken(off, val);
      textRef(pre, val == value ? off : off | IO.OFFCOMP);
    }

    // insert new entries
    indexAdd(pre, -1, 1, null);
  }

  @Override
  protected long textRef(final byte[] value, final boolean text) {
    // inline integer value
    final long v = toSimpleInt(value);
    if(v != Integer.MIN_VALUE) return v | IO.OFFNUM;

    // store text to heap file
    final DataAccess store = text ? texts : values;
    final long off = store.length();
    final byte[] val = Compress.pack(value);
    store.writeToken(off, val);
    return val == value ? off : off | IO.OFFCOMP;
  }
}
