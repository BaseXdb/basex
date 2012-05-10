package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Text compressor. */
  private final Compress comp = new Compress();
  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;
  /** Texts buffered for subsequent index updates. */
  private TokenObjMap<IntList> txts;
  /** Attribute values buffered for subsequent index updates. */
  private TokenObjMap<IntList> atvs;
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

    final DataInput in = new DataInput(meta.dbfile(DATAINF));
    try {
      // read meta data and indexes
      meta.read(in);
      while(true) {
        final String k = string(in.readToken());
        if(k.isEmpty()) break;
        if(k.equals(DBTAGS))      tagindex = new Names(in, meta);
        else if(k.equals(DBATTS)) atnindex = new Names(in, meta);
        else if(k.equals(DBPATH)) paths = new PathSummary(this, in);
        else if(k.equals(DBNS))   nspaces = new Namespaces(in);
        else if(k.equals(DBDOCS)) resources.read(in);
      }
    } finally {
      in.close();
    }

    // open data and indexes
    if(meta.updindex) {
      idmap = new IdPreMap(meta.dbfile(DATAIDP));
      if(meta.textindex) txtindex = new UpdatableDiskValues(this, true);
      if(meta.attrindex) atvindex = new UpdatableDiskValues(this, false);
    } else {
      if(meta.textindex) txtindex = new DiskValues(this, true);
      if(meta.attrindex) atvindex = new DiskValues(this, false);
    }
    if(meta.ftxtindex) ftxindex = new FTIndex(this);
    init();
  }

  /**
   * Internal database constructor, called from {@link DiskBuilder#build}.
   * @param md meta data
   * @param nm tags
   * @param at attributes
   * @param ps path summary
   * @param n namespaces
   * @throws IOException I/O Exception
   */
  public DiskData(final MetaData md, final Names nm, final Names at,
      final PathSummary ps, final Namespaces n) throws IOException {

    meta = md;
    tagindex = nm;
    atnindex = at;
    paths = ps;
    paths.finish(this);
    nspaces = n;
    if(meta.updindex) idmap = new IdPreMap(meta.lastid);
    init();
  }

  /**
   * Initializes the database.
   * @throws IOException I/O exception
   */
  public void init() throws IOException {
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
      final DataOutput out = new DataOutput(meta.dbfile(DATAINF));
      meta.write(out);
      out.writeToken(token(DBTAGS));
      tagindex.write(out);
      out.writeToken(token(DBATTS));
      atnindex.write(out);
      out.writeToken(token(DBPATH));
      paths.write(out);
      out.writeToken(token(DBNS));
      nspaces.write(out);
      out.writeToken(token(DBDOCS));
      resources.write(out);
      out.write(0);
      out.close();
      if(idmap != null) idmap.write(meta.dbfile(DATAIDP));
      meta.dirty = false;
    }
    // in all cases, remove updating file
    updateFile().delete();
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
      closeIndex(IndexType.TEXT);
      closeIndex(IndexType.ATTRIBUTE);
      closeIndex(IndexType.FULLTEXT);
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  @Override
  public synchronized void closeIndex(final IndexType type) {
    // close existing index
    final Index index = index(type);
    if(index == null) return;
    index.close();

    // invalidate index reference
    meta.dirty = true;
    switch(type) {
      case TEXT:      txtindex = null; break;
      case ATTRIBUTE: atvindex = null; break;
      case FULLTEXT:  ftxindex = null; break;
      default:        break;
    }
  }

  @Override
  public void setIndex(final IndexType type, final Index index) {
    meta.dirty = true;
    switch(type) {
      case TEXT:      txtindex = index; break;
      case ATTRIBUTE: atvindex = index; break;
      case FULLTEXT:  ftxindex = index; break;
      default:        break;
    }
  }

  @Override
  public boolean startUpdate() {
    final IOFile uf = updateFile();
    return (uf.exists() || uf.touch()) && table.lock(true);
  }

  @Override
  public synchronized void finishUpdate() {
    // skip all flush operations if auto flush is off, or file has already been closed
    if(!meta.prop.is(Prop.AUTOFLUSH) || closed) return;

    try {
      write();
      table.flush();
      texts.flush();
      values.flush();
      if(txtindex != null) ((DiskValues) txtindex).flush();
      if(atvindex != null) ((DiskValues) atvindex).flush();
    } catch(final IOException ex) {
      Util.stack(ex);
    } finally {
      table.lock(false);
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
    return num(o) ? token((int) o) : txt(o, text);
  }

  @Override
  public long textItr(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? o & IO.OFFNUM - 1 : toLong(txt(o, text));
  }

  @Override
  public double textDbl(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? o & IO.OFFNUM - 1 : toDouble(txt(o, text));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    final long o = textOff(pre);
    if(num(o)) return numDigits((int) o);
    final DataAccess da = text ? texts : values;
    final int l = da.readNum(o & IO.OFFCOMP - 1);
    // compressed: next number contains number of compressed bytes
    return cpr(o) ? da.readNum() : l;
  }

  /**
   * Returns a text (text, comment, pi) or attribute value.
   * @param o text offset
   * @param text text or attribute flag
   * @return text
   */
  private byte[] txt(final long o, final boolean text) {
    final byte[] txt = (text ? texts : values).readToken(o & IO.OFFCOMP - 1);
    return cpr(o) ? comp.unpack(txt) : txt;
  }

  /**
   * Returns true if the specified value contains a number.
   * @param o offset
   * @return result of check
   */
  private static boolean num(final long o) {
    return (o & IO.OFFNUM) != 0;
  }

  /**
   * Returns true if the specified value references a compressed token.
   * @param o offset
   * @return result of check
   */
  private static boolean cpr(final long o) {
    return (o & IO.OFFCOMP) != 0;
  }

  // UPDATE OPERATIONS ========================================================

  @Override
  protected void delete(final int pre, final boolean text) {
    // old entry (offset or value)
    final long old = textOff(pre);
    // fill unused space with zero-bytes
    if(!num(old)) (text ? texts : values).free(old & IO.OFFCOMP - 1, 0);
  }

  @Override
  protected void updateText(final int pre, final byte[] value, final int kind) {
    final boolean text = kind != ATTR;

    if(meta.updindex) {
      // update indexes
      final int id = id(pre);
      final byte[] oldval = text(pre, text);
      final DiskValues index = (DiskValues) (text ? txtindex : atvindex);
      // don't index document names
      if(index != null && kind != DOC) index.replace(oldval, value, id);
    }

    // reference to text store
    final DataAccess store = text ? texts : values;
    // file length
    final long len = store.length();

    // new entry (offset or value)
    final long v = toSimpleInt(value);
    // flag for inlining numeric value
    final boolean vn = v != Integer.MIN_VALUE;
    // text to be stored (null if value will be inlined)
    final byte[] vl = vn ? null : comp.pack(value);

    // old entry (offset or value)
    final long old = textOff(pre);
    // find text store offset
    final long off;
    if(num(old)) {
      // numeric entry: append new entry at the end
      off = len;
    } else {
      // text size (0 if value will be inlined)
      final int l = vn ? 0 : vl.length + Num.length(vl.length);
      off = store.free(old & IO.OFFCOMP - 1, l);
    }

    // store new entry
    if(vn) {
      // inline integer value
      textOff(pre, v | IO.OFFNUM);
    } else {
      store.writeToken(off, vl);
      textOff(pre, vl == value ? off : off | IO.OFFCOMP);
    }
  }

  @Override
  protected void indexBegin() {
    txts = new TokenObjMap<IntList>();
    atvs = new TokenObjMap<IntList>();
  }

  @Override
  protected void indexEnd() {
    // update all indexes in parallel
    // [DP] Full-text index updates: update the existing indexes
    final Thread txtupdater = txts.size() > 0 ?
        runIndexInsert((DiskValues) txtindex, txts) : null;
    final Thread atvupdater = atvs.size() > 0 ?
        runIndexInsert((DiskValues) atvindex, atvs) : null;

    // wait for all tasks to finish
    try {
      if(txtupdater != null) txtupdater.join();
      if(atvupdater != null) atvupdater.join();
    } catch(final InterruptedException ex) {
      Util.stack(ex);
    }
  }

  @Override
  protected long index(final int pre, final int id, final byte[] value, final int kind) {
    final DataAccess store;
    final TokenObjMap<IntList> m;

    if(kind == ATTR) {
      store = values;
      m = meta.attrindex ? atvs : null;
    } else {
      store = texts;
      // don't index document names
      m = meta.textindex && kind != DOC ? txts : null;
    }

    // add text to map to index later
    if(meta.updindex && m != null && value.length <= meta.maxlen) {
      final IntList ids;
      final int hash = m.id(value);
      if(hash == 0) {
        ids = new IntList();
        m.add(value, ids);
      } else {
        ids = m.value(hash);
      }
      ids.add(id);
    }

    // add text to text file
    // inline integer value...
    final long v = toSimpleInt(value);
    if(v != Integer.MIN_VALUE) return v | IO.OFFNUM;

    // store text
    final long off = store.length();
    final byte[] val = comp.pack(value);
    store.writeToken(off, val);
    return val == value ? off : off | IO.OFFCOMP;
  }

  @Override
  protected void indexDelete(final int pre, final int size) {
    if(!(meta.textindex || meta.attrindex)) return;

    // collect all keys and ids
    txts = new TokenObjMap<IntList>();
    atvs = new TokenObjMap<IntList>();
    final int l = pre + size;
    for(int p = pre; p < l; ++p) {
      final int k = kind(p);
      final boolean isAttr = k == ATTR;
      // consider nodes which are attribute, text, comment, or proc. instruction
      if(meta.attrindex && isAttr ||
         meta.textindex && (k == TEXT || k == COMM || k == PI)) {
        final byte[] key = text(p, !isAttr);
        if(key.length <= meta.maxlen) {
          final IntList ids;
          final TokenObjMap<IntList> m = isAttr ? atvs : txts;
          final int hash = m.id(key);
          if(hash == 0) {
            ids = new IntList();
            m.add(key, ids);
          } else {
            ids = m.value(hash);
          }
          ids.add(id(p));
        }
      }
    }

    // update all indexes in parallel
    // [DP] Full-text index updates: update the existing indexes
    final Thread txtupdater = txts.size() > 0 ?
        runIndexDelete((DiskValues) txtindex, txts) : null;
    final Thread atvupdater = atvs.size() > 0 ?
        runIndexDelete((DiskValues) atvindex, atvs) : null;

    // wait for all tasks to finish
    try {
      if(txtupdater != null) txtupdater.join();
      if(atvupdater != null) atvupdater.join();
    } catch(final InterruptedException ex) {
      Util.errln(ex);
    }
  }

  /**
   * Starts a new thread which inserts records into an index.
   * @param dv index
   * @param m records to be inserted
   * @return the new thread
   */
  private static Thread runIndexInsert(final DiskValues dv,
      final TokenObjMap<IntList> m) {

    final Thread t = new Thread() {
      @Override
      public void run() { dv.index(m); }
    };
    t.start();
    return t;
  }

  /**
   * Starts a new thread which deletes records from an index.
   * @param dv index
   * @param m records to be deleted
   * @return the new thread
   */
  private static Thread runIndexDelete(final DiskValues dv,
      final TokenObjMap<IntList> m) {

    final Thread t = new Thread() {
      @Override
      public void run() { dv.delete(m); }
    };
    t.start();
    return t;
  }
}
