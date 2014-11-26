package org.basex.data;

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
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed disk structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Text compressor. */
  private static final ThreadLocal<Compress> COMP = new ThreadLocal<Compress>() {
    @Override
    protected Compress initialValue() {
      return new Compress();
    }
  };

  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;
  /** Texts buffered for subsequent index updates. */
  private TokenObjMap<IntList> txtBuffer;
  /** Attribute values buffered for subsequent index updates. */
  private TokenObjMap<IntList> atvBuffer;
  /** Closed flag. */
  private boolean closed;

  /**
   * Default constructor, called from {@link Open#open}.
   * @param db name of database
   * @param ctx database context
   * @throws IOException I/O Exception
   */
  public DiskData(final String db, final Context ctx) throws IOException {
    meta = new MetaData(db, ctx);

    // don't open databases marked as updating
    if(updateFile().exists()) throw new BaseXException(Text.DB_UPDATED_X, meta.name);

    try(final DataInput in = new DataInput(meta.dbfile(DATAINF))) {
      // read meta data and indexes
      meta.read(in);
      while(true) {
        final String k = string(in.readToken());
        if(k.isEmpty()) break;
        if(k.equals(DBTAGS))      elemNames = new Names(in, meta);
        else if(k.equals(DBATTS)) attrNames = new Names(in, meta);
        else if(k.equals(DBPATH)) paths = new PathSummary(this, in);
        else if(k.equals(DBNS))   nspaces = new Namespaces(in);
        else if(k.equals(DBDOCS)) resources.read(in);
      }
    }

    // open data and indexes
    init();
    if(meta.updindex) {
      idmap = new IdPreMap(meta.dbfile(DATAIDP));
      if(meta.textindex) textIndex = new UpdatableDiskValues(this, true);
      if(meta.attrindex) attrIndex = new UpdatableDiskValues(this, false);
    } else {
      if(meta.textindex) textIndex = new DiskValues(this, true);
      if(meta.attrindex) attrIndex = new DiskValues(this, false);
    }
    if(meta.ftxtindex) ftxtIndex = new FTIndex(this);
  }

  /**
   * Internal database constructor, called from {@link DiskBuilder#build}.
   * @param meta meta data
   * @param elemNames element names
   * @param attrNames attribute names
   * @param paths path summary
   * @param n namespaces
   * @throws IOException I/O Exception
   */
  public DiskData(final MetaData meta, final Names elemNames, final Names attrNames,
      final PathSummary paths, final Namespaces n) throws IOException {

    this.meta = meta;
    this.elemNames = elemNames;
    this.attrNames = attrNames;
    this.paths = paths;
    this.nspaces = n;
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
    if(meta.dirty) {
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
      if(idmap != null) idmap.write(meta.dbfile(DATAIDP));
      meta.dirty = false;
    }
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
  public void createIndex(final IndexType type, final MainOptions options, final Command cmd)
      throws IOException {

    // close existing index
    close(type);
    final IndexBuilder ib;
    switch(type) {
      case TEXT:      ib = new DiskValuesBuilder(this, options, true); break;
      case ATTRIBUTE: ib = new DiskValuesBuilder(this, options, false); break;
      case FULLTEXT:  ib = new FTBuilder(this, options); break;
      default:        throw Util.notExpected();
    }
    if(cmd != null) cmd.proc(ib);
    set(type, ib.build());
  }

  @Override
  public boolean dropIndex(final IndexType type) {
    // close and drop index (return true if no index exists)
    final Index index = index(type);
    close(type);
    return index == null || index.drop();
  }

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param index index instance
   */
  private void set(final IndexType type, final Index index) {
    meta.dirty = true;
    switch(type) {
      case TEXT:      textIndex = index; break;
      case ATTRIBUTE: attrIndex = index; break;
      case FULLTEXT:  ftxtIndex = index; break;
      default:        break;
    }
  }

  @Override
  public void startUpdate(final MainOptions opts) throws IOException {
    if(!table.lock(true)) throw new BaseXException(Text.DB_PINNED_X, meta.name);
    if(opts.get(MainOptions.AUTOFLUSH)) {
      final IOFile uf = updateFile();
      if(uf.exists()) throw new BaseXException(Text.DB_UPDATED_X, meta.name);
      if(!uf.touch()) throw Util.notExpected("%: could not create lock file.", meta.name);
    }
  }

  @Override
  public synchronized void finishUpdate(final MainOptions opts) {
    // remove updating file
    final boolean auto = opts.get(MainOptions.AUTOFLUSH);
    if(auto) {
      final IOFile uf = updateFile();
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
        if(textIndex != null) ((DiskValues) textIndex).flush();
        if(attrIndex != null) ((DiskValues) attrIndex).flush();
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Returns a file that indicates ongoing updates.
   * @return updating file
   */
  public IOFile updateFile() {
    return meta.dbfile(DATAUPD);
  }

  @Override
  public byte[] text(final int pre, final boolean text) {
    final long o = textOff(pre);
    return number(o) ? token((int) o) : txt(o, text);
  }

  @Override
  public long textItr(final int pre, final boolean text) {
    final long o = textOff(pre);
    return number(o) ? o & IO.OFFNUM - 1 : toLong(txt(o, text));
  }

  @Override
  public double textDbl(final int pre, final boolean text) {
    final long o = textOff(pre);
    return number(o) ? o & IO.OFFNUM - 1 : toDouble(txt(o, text));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    final long o = textOff(pre);
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
    return compressed(off) ? COMP.get().unpack(txt) : txt;
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

  // UPDATE OPERATIONS ========================================================

  @Override
  protected void delete(final int pre, final boolean text) {
    // old entry (offset or value)
    final long old = textOff(pre);
    // fill unused space with zero-bytes
    if(!number(old)) (text ? texts : values).free(old & IO.OFFCOMP - 1, 0);
  }

  @Override
  protected void updateText(final int pre, final byte[] value, final int kind) {
    final boolean text = kind != ATTR;

    if(meta.updindex) {
      // update indexes
      final int id = id(pre);
      final byte[] oldval = text(pre, text);
      final DiskValues index = (DiskValues) (text ? textIndex : attrIndex);
      // don't index document names
      if(index != null && kind != DOC) index.replace(oldval, value, id);
    }

    // reference to text store
    final DataAccess store = text ? texts : values;
    // file length
    final long len = store.length();

    // new entry (offset or value)
    final long v = toSimpleInt(value);
    if(v != Integer.MIN_VALUE) {
      // inline integer value
      textOff(pre, v | IO.OFFNUM);
    } else {
      // text to be stored (possibly packed)
      final byte[] val = COMP.get().pack(value);
      // old entry (offset or value)
      final long old = textOff(pre);

      // find text store offset
      final long off;
      if(number(old)) {
        // numeric entry: append new entry at the end
        off = len;
      } else {
        // text size (0 if value will be inlined)
        final int vl = val.length;
        off = store.free(old & IO.OFFCOMP - 1, vl + Num.length(vl));
      }

      store.writeToken(off, val);
      textOff(pre, val == value ? off : off | IO.OFFCOMP);
    }
  }

  @Override
  void indexBegin() {
    txtBuffer = new TokenObjMap<>();
    atvBuffer = new TokenObjMap<>();
  }

  @Override
  protected void indexAdd() {
    if(!txtBuffer.isEmpty()) ((DiskValues) textIndex).add(txtBuffer);
    if(!atvBuffer.isEmpty()) ((DiskValues) attrIndex).add(atvBuffer);
  }

  @Override
  void indexDelete() {
    if(!txtBuffer.isEmpty()) ((DiskValues) textIndex).delete(txtBuffer);
    if(!atvBuffer.isEmpty()) ((DiskValues) attrIndex).delete(atvBuffer);
  }

  @Override
  protected long index(final int pre, final int id, final byte[] value, final int kind) {
    final DataAccess store;
    final TokenObjMap<IntList> map;
    if(kind == ATTR) {
      store = values;
      map = meta.attrindex ? atvBuffer : null;
    } else {
      store = texts;
      // don't index document names
      map = meta.textindex && kind != DOC ? txtBuffer : null;
    }

    // add text to map to index later
    if(meta.updindex && map != null && value.length <= meta.maxlen) {
      IntList ids = map.get(value);
      if(ids == null) {
        ids = new IntList(1);
        map.put(value, ids);
      }
      ids.add(id);
    }

    // add text to text file
    // inline integer value...
    final long v = toSimpleInt(value);
    if(v != Integer.MIN_VALUE) return v | IO.OFFNUM;

    // store text
    final long off = store.length();
    final byte[] val = COMP.get().pack(value);
    store.writeToken(off, val);
    return val == value ? off : off | IO.OFFCOMP;
  }

  @Override
  protected void indexDelete(final int pre, final int size) {
    final boolean textI = meta.textindex, attrI = meta.attrindex;
    if(textI || attrI) {
      // collect all keys and ids
      indexBegin();
      final int l = pre + size;
      for(int p = pre; p < l; ++p) {
        final int k = kind(p);
        // consider nodes which are attribute, text, comment, or proc. instruction
        final boolean text = k == TEXT || k == COMM || k == PI;
        if(textI && text || attrI && k == ATTR) {
          final byte[] key = text(p, text);
          if(key.length <= meta.maxlen) {
            final TokenObjMap<IntList> m = text ? txtBuffer : atvBuffer;
            IntList ids = m.get(key);
            if(ids == null) {
              ids = new IntList(1);
              m.put(key, ids);
            }
            ids.add(id(p));
          }
        }
      }
      indexDelete();
    }
  }

  @Override
  public boolean inMemory() {
    return false;
  }
}
