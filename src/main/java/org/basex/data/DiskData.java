package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;

import org.basex.build.DiskBuilder;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.index.Index;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.FTIndex;
import org.basex.index.path.PathSummary;
import org.basex.index.value.DiskValues;
import org.basex.index.Names;
import org.basex.io.IO;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.random.DataAccess;
import org.basex.io.random.TableDiskAccess;
import org.basex.util.Compress;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed disk structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team 2005-11, BSD License
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

  /**
   * Default constructor.
   * @param db name of database
   * @param ctx database context
   * @throws IOException I/O Exception
   */
  public DiskData(final String db, final Context ctx) throws IOException {
    meta = new MetaData(db, ctx);

    // don't allow to open locked databases
    if(lockFile().exists()) throw new BaseXException(Text.DBLOCKED, meta.name);

    final int cats = ctx.prop.num(Prop.CATEGORIES);
    final DataInput in = new DataInput(meta.dbfile(DATAINF));
    try {
      // read meta data and indexes
      meta.read(in);
      while(true) {
        final String k = string(in.readToken());
        if(k.isEmpty()) break;
        if(k.equals(DBTAGS))      tagindex = new Names(in, cats);
        else if(k.equals(DBATTS)) atnindex = new Names(in, cats);
        else if(k.equals(DBPATH)) pthindex = new PathSummary(this, in);
        else if(k.equals(DBNS))   ns = new Namespaces(in);
        else if(k.equals(DBDOCS)) docindex.read(in);
      }
      // open data and indexes
      init();
      if(meta.textindex) txtindex = new DiskValues(this, true);
      if(meta.attrindex) atvindex = new DiskValues(this, false);
      if(meta.ftxtindex)   ftxindex = FTIndex.get(this, meta.wildcards);
    } finally {
      try { in.close(); } catch(final IOException ex) { }
    }
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
    pthindex = ps;
    pthindex.finish(this);
    ns = n;
    init();
    flush();
  }

  @Override
  public void init() throws IOException {
    table = new TableDiskAccess(meta, DATATBL);
    texts = new DataAccess(meta.dbfile(DATATXT));
    values = new DataAccess(meta.dbfile(DATAATV));
    super.init();
  }

  /**
   * Writes all meta data to disk.
   * @throws IOException I/O exception
   */
  private void write() throws IOException {
    final DataOutput out = new DataOutput(meta.dbfile(DATAINF));
    meta.write(out);
    out.writeToken(token(DBTAGS));
    tagindex.write(out);
    out.writeToken(token(DBATTS));
    atnindex.write(out);
    out.writeToken(token(DBPATH));
    pthindex.write(out);
    out.writeToken(token(DBNS));
    ns.write(out);
    out.writeToken(token(DBDOCS));
    docindex.write(out);
    out.write(0);
    out.close();
  }

  @Override
  public synchronized void flush() {
    if(!meta.prop.is(Prop.AUTOFLUSH)) return;
    try {
      if(meta.dirty) write();
      table.flush();
      texts.flush();
      values.flush();
      meta.dirty = false;
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  @Override
  public synchronized void close() throws IOException {
    if(meta.dirty) write();
    table.close();
    texts.close();
    values.close();
    closeIndex(IndexType.TEXT);
    closeIndex(IndexType.ATTRIBUTE);
    closeIndex(IndexType.FULLTEXT);
  }

  @Override
  public synchronized void closeIndex(final IndexType type) throws IOException {
    final Index index = index(type);
    if(index == null) return;

    index.close();
    switch(type) {
      case TEXT:      txtindex = null; break;
      case ATTRIBUTE: atvindex = null; break;
      case FULLTEXT:  ftxindex = null; break;
      case PATH:      pthindex.close(); break;
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
      case PATH:      pthindex = (PathSummary) index; break;
      default:        break;
    }
  }

  @Override
  public boolean lock() {
    // try several times (may fail at first run)
    final File lock = lockFile();
    for(int i = 0; i < 10; i++) {
      try {
        if(lock.createNewFile()) return true;
      } catch(final IOException ex) {
        Performance.sleep(10);
        Util.debug(ex);
      }
    }
    return false;
  }

  @Override
  public boolean unlock() {
    return lockFile().delete();
  }

  /**
   * Returns a lock file.
   * @return lock file
   */
  public File lockFile() {
    return meta.dbfile(DataText.DATAUPD);
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
  protected void text(final int pre, final byte[] value, final boolean text) {
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
  protected long index(final int pre, final byte[] value, final boolean text) {
    // inline integer value...
    final long v = toSimpleInt(value);
    if(v != Integer.MIN_VALUE) return v | IO.OFFNUM;

    // store text
    final DataAccess store = text ? texts : values;
    final long off = store.length();
    final byte[] val = comp.pack(value);
    store.writeToken(off, val);
    return val == value ? off : off | IO.OFFCOMP;
  }
}
