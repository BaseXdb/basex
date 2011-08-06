package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.build.DiskBuilder;
import org.basex.core.Context;
import org.basex.core.Prop;
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
   * @throws IOException IO Exception
   */
  public DiskData(final String db, final Context ctx) throws IOException {
    meta = new MetaData(db, ctx);

    final int cats = ctx.prop.num(Prop.CATEGORIES);
    final DataInput in = new DataInput(meta.dbfile(DATAINFO));
    try {
      // read meta data and indexes
      meta.read(in);
      while(true) {
        final String k = string(in.readToken());
        if(k.isEmpty()) break;
        if(k.equals(DBTAGS))      tagindex = new Names(in, cats);
        else if(k.equals(DBATTS)) atnindex = new Names(in, cats);
        else if(k.equals(DBPATH)) pthindex = new PathSummary(in);
        else if(k.equals(DBNS))   ns = new Namespaces(in);
        else if(k.equals(DBDOCS)) docindex.read(in);
      }
      // open data and indexes
      init();
      if(meta.textindex) txtindex = new DiskValues(this, true);
      if(meta.attrindex) atvindex = new DiskValues(this, false);
      if(meta.ftindex)   ftxindex = FTIndex.get(this, meta.wildcards);
    } catch(final IOException ex) {
      throw ex;
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
   * @throws IOException IO Exception
   */
  public DiskData(final MetaData md, final Names nm, final Names at,
      final PathSummary ps, final Namespaces n) throws IOException {

    meta = md;
    tagindex = nm;
    atnindex = at;
    pthindex = ps;
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
    final DataOutput out = new DataOutput(meta.dbfile(DATAINFO));
    meta.write(out);
    out.writeString(DBTAGS);
    tagindex.write(out);
    out.writeString(DBATTS);
    atnindex.write(out);
    out.writeString(DBPATH);
    pthindex.write(out);
    out.writeString(DBNS);
    ns.write(out);
    out.writeString(DBDOCS);
    docindex.write(out);
    out.write(0);
    out.close();
  }

  @Override
  public synchronized void flush() {
    if(!meta.prop.is(Prop.FORCEFLUSH)) return;
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
    switch(type) {
      case TEXT:
        if(txtindex != null) { txtindex.close(); txtindex = null; }
        break;
      case ATTRIBUTE:
        if(atvindex != null) { atvindex.close(); atvindex = null; }
        break;
      case FULLTEXT:
        if(ftxindex != null) { ftxindex.close(); ftxindex = null; }
        break;
      default:
        // other indexes will not be closed
        break;
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
      default: break;
    }
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
  protected void text(final int pre, final byte[] val, final boolean text) {
    // reference to text store
    final DataAccess store = text ? texts : values;
    // file length
    final long len = store.length();

    // new entry (offset or value)
    final long v = toSimpleInt(val);
    // flag for inlining numeric value
    final boolean vn = v != Integer.MIN_VALUE;
    // text to be stored (null if value will be inlined)
    final byte[] txt = vn ? null : comp.pack(val);

    // old entry (offset or value)
    final long old = textOff(pre);
    // old offset, and offset to which entry will be written
    long off = old & IO.OFFCOMP - 1;

    // analyze old entry: chop entry, or fill unused space with zero-bytes
    if(num(old)) {
      // numeric entry: append new entry at the end
      off = len;
    } else {
      // text size (0 if value will be inlined)
      int ts = vn ? 0 : txt.length + Num.length(txt.length);
      // old text size (available space)
      int os = store.readNum(off) + (int) (store.cursor() - off);

      // extend available space by subsequent zero-bytes
      store.cursor(off + os);
      for(; off + os < len && os < ts && store.read1() == 0; os++);

      if(off + os == len) {
        // entry is placed last: reset file length (discard last entry)
        store.length(off);
      } else {
        if(os < ts) {
          // gap is too small for new entry...
          // reset cursor to overwrite entry with zero-bytes
          store.cursor(off);
          ts = 0;
          // place new entry after last entry
          off = len;
        }
        // fill gap with zero-bytes for future updates
        store.cursor(off + ts);
        while(ts++ < os) store.write1(0);
      }
    }

    // store new entry
    if(vn) {
      // integer values is inlined (stored in the table)
      textOff(pre, v | IO.OFFNUM);
    } else {
      store.writeToken(off, txt);
      textOff(pre, off | (txt == val ? 0 : IO.OFFCOMP));
    }
  }

  @Override
  protected long index(final byte[] val, final int pre, final boolean text) {
    final DataAccess store = text ? texts : values;
    final long oo = store.length();
    store.writeToken(oo, val);
    return oo;
  }
}
