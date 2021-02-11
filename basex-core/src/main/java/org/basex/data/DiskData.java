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
 * Texts may be inlined on disk. The bits of the first byte of the 5-byte text reference can be
 * decoded as follows:
 *
 * <pre>
 * Bit 0 [INLINED]    indicates if value is inlined in table or stored externally
 * Bit 1 [COMPRESSED] indicates if value is compressed
 * Bit 2 [STRING]     indicates if an inlined value is a string
 *
 * - INLINED (text is inlined):
 *   - STRING (value is string):
 *     - Bits 4-7 contain string length
 *     - 32 remaining bits contain inlined string
 *     - COMPRESSED: unpack and return inlined text
 *     - NOT COMPRESSED: return text unchanged
 *   - NOT STRING (value is integer):
 *     - return 32 bits of remaining 4 bytes as integer
 * - NOT INLINED (text is stored externally):
 *   - 38 remaining bits contain text reference
 *   - COMPRESSED: unpack and return external text
 *   - NOT COMPRESSED: return external text unchanged
 * </pre>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;

  /**
   * Default constructor, called from {@link Open#open}.
   * @param meta meta data
   * @throws IOException I/O Exception
   */
  public DiskData(final MetaData meta) throws IOException {
    super(meta);

    try(DataInput in = new DataInput(meta.dbFile(DATAINF))) {
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
      idmap = new IdPreMap(meta.dbFile(DATAIDP));
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
    texts = new DataAccess(meta.dbFile(DATATXT));
    values = new DataAccess(meta.dbFile(DATAATV));
  }

  /**
   * Writes all meta data to disk.
   * @throws IOException I/O exception
   */
  private void write() throws IOException {
    if(!meta.dirty) return;

    try(DataOutput out = new DataOutput(meta.dbFile(DATAINF))) {
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
    if(meta.updindex) idmap.write(meta.dbFile(DATAIDP));
    meta.dirty = false;
  }

  @Override
  public synchronized void close() {
    if(closed) return;
    super.close();
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
    try {
      if(cmd != null) cmd.pushJob(ib);
      set(type, ib.build());
    } finally {
      if(cmd != null) cmd.popJob();
    }
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
  public void startUpdate(final MainOptions opts) throws BaseXException {
    if(!table.lock(true)) throw new BaseXException(DB_PINNED_X, meta.name);
    if(opts.get(MainOptions.AUTOFLUSH)) {
      final IOFile upd = meta.updateFile();
      if(upd.exists()) throw new BaseXException(DB_UPDATED_X, meta.name);
      if(!upd.touch()) throw Util.notExpected("%: could not create lock file.", meta.name);
    }
  }

  @Override
  public synchronized void finishUpdate(final MainOptions opts) {
    // OPTIMIZE ALL / db:optimize(..., true) will close the database before this function is called
    if(closed) return;

    // remove updating file
    final boolean auto = opts.get(MainOptions.AUTOFLUSH);
    if(auto) {
      final IOFile upd = meta.updateFile();
      if(!upd.exists()) throw Util.notExpected("%: lock file does not exist.", meta.name);
      if(!upd.delete()) throw Util.notExpected("%: could not delete lock file.", meta.name);
    }

    flush(auto);
    if(!table.lock(false)) throw Util.notExpected("Database '%': could not unlock.", meta.name);
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
    final long value = textRef(pre);
    return Inline.inlined(value) ? Inline.unpack(value) : txt(value, text);
  }

  @Override
  public long textItr(final int pre, final boolean text) {
    final long value = textRef(pre);
    return Inline.inlined(value) ? Inline.unpackLong(value) : toLong(txt(value, text));
  }

  @Override
  public double textDbl(final int pre, final boolean text) {
    final long value = textRef(pre);
    return Inline.inlined(value) ? Inline.unpackDouble(value) : toDouble(txt(value, text));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    final long value = textRef(pre);
    if(Inline.inlined(value)) return Inline.unpackLength(value);

    final DataAccess da = text ? texts : values;
    final int l = da.readNum(value & Compress.COMPRESS - 1);
    // if text is compressed, read number of compressed bytes
    return Compress.compressed(value) ? da.readNum() : l;
  }

  /**
   * Returns a text (text, comment, pi) or attribute value.
   * @param offset text offset
   * @param text text or attribute flag
   * @return text
   */
  private byte[] txt(final long offset, final boolean text) {
    final byte[] txt = (text ? texts : values).readToken(offset & Compress.COMPRESS - 1);
    return Compress.compressed(offset) ? Compress.unpack(txt) : txt;
  }

  @Override
  public boolean inMemory() {
    return false;
  }

  // UPDATE OPERATIONS ============================================================================

  @Override
  protected void delete(final int pre, final boolean text) {
    // old entry (offset or value)
    final long old = textRef(pre);
    // if old text was not inlined, fill unused space in text file with zero bytes
    if(!Inline.inlined(old)) (text ? texts : values).free(old & Compress.COMPRESS - 1, 0);
  }

  @Override
  protected void updateText(final int pre, final byte[] value, final int kind) {
    // delete existing index entry
    indexDelete(pre, -1, 1);

    // reference to heap file
    final DataAccess store = kind == ATTR ? values : texts;
    // old entry (offset or value)
    final long oldRef = textRef(pre);

    // check if new entry can be inlined
    final long v = Inline.packInt(value);
    if(v != -1) {
      // invalidate old entry if it was not inlined
      if(!Inline.inlined(oldRef)) store.free(oldRef & Compress.COMPRESS - 1, 0);
      // inline integer value
      textRef(pre, v);
    } else {
      // otherwise, try to compress new value
      final byte[] val = Compress.pack(value);

      // choose inserting position
      final long off;
      if(Inline.inlined(oldRef)) {
        // old entry was inlined: append new entry to heap file
        off = store.length();
      } else {
        // otherwise, compute inserting position and invalidate old entry
        final int vl = val.length;
        off = store.free(oldRef & Compress.COMPRESS - 1, vl + Num.length(vl));
      }

      store.writeToken(off, val);
      textRef(pre, val == value ? off : off | Compress.COMPRESS);
    }

    // insert new entries
    indexAdd(pre, -1, 1, null);
  }

  @Override
  protected long textRef(final byte[] value, final boolean text) {
    // try to inline value
    final long inlined = Inline.pack(value);
    if(inlined != 0) return inlined;

    // store text in heap file
    final byte[] packed = Compress.pack(value);
    final DataAccess store = text ? texts : values;
    final long offset = store.length();
    store.writeToken(offset, packed);
    return packed == value ? offset : Compress.COMPRESS | offset;
  }
}
