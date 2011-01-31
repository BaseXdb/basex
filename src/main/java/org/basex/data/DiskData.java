package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.index.FTIndex;
import org.basex.index.Index;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.Names;
import org.basex.index.DiskValues;
import org.basex.io.DataAccess;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableMemAccess;
import org.basex.util.Compress;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed disk structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;
  /** Text compressor. */
  private final Compress comp;

  /**
   * Default constructor.
   * @param db name of database
   * @param pr database properties
   * @throws IOException IO Exception
   */
  public DiskData(final String db, final Prop pr) throws IOException {
    meta = new MetaData(db, pr);
    comp = new Compress();

    final DataInput in = new DataInput(meta.file(DATAINFO));
    try {
      // read meta data and indexes
      meta.read(in);
      while(true) {
        final String k = Token.string(in.readBytes());
        if(k.isEmpty()) break;
        if(k.equals(DBTAGS))      tags = new Names(in);
        else if(k.equals(DBATTS)) atts = new Names(in);
        else if(k.equals(DBPATH)) pthindex = new PathSummary(in);
        else if(k.equals(DBNS))   ns   = new Namespaces(in);
      }

      // open data and indexes..
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
   * Internal constructor, specifying all meta data.
   * @param md meta data
   * @param nm tags
   * @param at attributes
   * @param ps path summary
   * @param n namespaces
   * @throws IOException IO Exception
   */
  public DiskData(final MetaData md, final Names nm, final Names at,
      final PathSummary ps, final Namespaces n) throws IOException {

    comp = new Compress();
    meta = md;
    tags = nm;
    atts = at;
    pthindex = ps;
    ns = n;
    init();
    write();
  }

  @Override
  public void init() throws IOException {
    // table main memory mode..
    table = meta.prop.is(Prop.TABLEMEM) ? new TableMemAccess(meta, DATATBL) :
      new TableDiskAccess(meta, DATATBL);
    texts = new DataAccess(meta.file(DATATXT));
    values = new DataAccess(meta.file(DATAATV));
    super.init();
  }

  /**
   * Writes all meta data to disk.
   * @throws IOException I/O exception
   */
  private void write() throws IOException {
    final DataOutput out = new DataOutput(meta.file(DATAINFO));
    meta.write(out);
    out.writeString(DBTAGS);
    tags.write(out);
    out.writeString(DBATTS);
    atts.write(out);
    out.writeString(DBPATH);
    pthindex.write(out);
    out.writeString(DBNS);
    ns.write(out);
    out.write(0);
    out.close();
  }

  @Override
  public synchronized void flush() {
    try {
      table.flush();
      texts.flush();
      values.flush();
      write();
      meta.dirty = false;
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  @Override
  protected synchronized void cls() throws IOException {
    if(meta.dirty) flush();
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
      case TEXT:      if(txtindex != null) txtindex.close(); break;
      case ATTRIBUTE: if(atvindex != null) atvindex.close(); break;
      case FULLTEXT:  if(ftxindex != null) ftxindex.close(); break;
      case PATH:      if(ftxindex != null) pthindex.close(); break;
      default: break;
    }
  }

  @Override
  public void setIndex(final IndexType type, final Index index) {
    switch(type) {
      case TEXT:      if(meta.textindex) txtindex = index; break;
      case ATTRIBUTE: if(meta.attrindex) atvindex = index; break;
      case FULLTEXT:  if(meta.ftindex)   ftxindex = index; break;
      case PATH:      if(meta.pathindex) pthindex = (PathSummary) index; break;
      default: break;
    }
  }

  @Override
  public byte[] text(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? Token.token((int) o) : txt(o, text);
  }

  @Override
  public long textItr(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? o & IO.NUMOFF - 1 : Token.toLong(txt(o, text));
  }

  @Override
  public double textDbl(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? o & IO.NUMOFF - 1 : Token.toDouble(txt(o, text));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    final long o = textOff(pre);
    if(num(o)) return Token.numDigits((int) o);
    final DataAccess da = text ? texts : values;
    final int l = da.readNum(o & IO.CPROFF - 1);
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
    final byte[] txt = (text ? texts : values).readToken(o & IO.CPROFF - 1);
    return cpr(o) ? comp.unpack(txt) : txt;
  }

  /**
   * Returns true if the specified value contains a number.
   * @param o offset
   * @return result of check
   */
  private static boolean num(final long o) {
    return (o & IO.NUMOFF) != 0;
  }

  /**
   * Returns true if the specified value references a compressed token.
   * @param o offset
   * @return result of check
   */
  private static boolean cpr(final long o) {
    return (o & IO.CPROFF) != 0;
  }

  // UPDATE OPERATIONS ========================================================

  @Override
  protected void text(final int pre, final byte[] val, final boolean txt) {
    final long v = Token.toSimpleInt(val);
    if(v != Integer.MIN_VALUE) {
      textOff(pre, v | IO.NUMOFF);
    } else {
      final DataAccess da = txt ? texts : values;
      long o = textOff(pre) & IO.CPROFF - 1;

      final byte[] cpr = comp.pack(val);
      final boolean cp = cpr != val;

      // if old text is numeric or longer than the old text and not placed last,
      // append text at the end
      if(num(o) || cpr.length > da.readNum(o) &&
          da.readNum(o) + da.pos() != da.length()) o = da.length();

      da.writeBytes(o, cpr);
      textOff(pre, o | (cp ? IO.CPROFF : 0));
    }
  }

  @Override
  protected long index(final byte[] txt, final int pre, final boolean text) {
    final DataAccess da = text ? texts : values;
    final long off = da.length();
    da.writeBytes(off, txt);
    return off;
  }
}
