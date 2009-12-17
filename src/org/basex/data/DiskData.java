package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.File;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.index.FTFuzzy;
import org.basex.index.FTTrie;
import org.basex.index.Index;
import org.basex.index.Names;
import org.basex.index.Values;
import org.basex.io.DataAccess;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableMemAccess;
import org.basex.util.Token;

/**
 * This class stores and organizes the database table and the index structures
 * for textual content in a compressed disk structure.
 * The table mapping is documented in {@link Data}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DiskData extends Data {
  /** Texts access file. */
  private DataAccess texts;
  /** Values access file. */
  private DataAccess values;

  /**
   * Default constructor.
   * @param db name of database
   * @param pr database properties
   * @throws IOException IO Exception
   */
  public DiskData(final String db, final Prop pr) throws IOException {
    meta = new MetaData(db, pr);

    final DataInput in = new DataInput(meta.file(DATAINFO));
    try {
      // read meta data and indexes
      meta.read(in);
      while(true) {
        final String k = Token.string(in.readBytes());
        if(k.isEmpty()) break;
        if(k.equals(DBTAGS))      tags = new Names(in);
        else if(k.equals(DBATTS)) atts = new Names(in);
        else if(k.equals(DBPATH)) path = new PathSummary(in);
        else if(k.equals(DBNS))   ns   = new Namespaces(in);
      }

      // open data and indexes..
      init();
      if(meta.txtindex) txtindex = new Values(this, true);
      if(meta.atvindex) atvindex = new Values(this, false);
      if(meta.ftxindex) ftxindex = meta.wildcards ?
          new FTTrie(this) : new FTFuzzy(this);
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

    meta = md;
    tags = nm;
    atts = at;
    path = ps;
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
    final File file = meta.file(DATAINFO);
    final DataOutput out = new DataOutput(file);
    meta.write(out);
    out.writeString(DBTAGS);
    tags.write(out);
    out.writeString(DBATTS);
    atts.write(out);
    out.writeString(DBPATH);
    path.write(out);
    out.writeString(DBNS);
    ns.write(out);
    out.write(0);
    out.close();
  }

  @Override
  public void flush() {
    try {
      table.flush();
      texts.flush();
      values.flush();
      write();
      meta.dirty = false;
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void cls() throws IOException {
    if(meta.dirty) flush();
    table.close();
    texts.close();
    values.close();
    closeIndex(Type.TXT);
    closeIndex(Type.ATV);
    closeIndex(Type.FTX);
  }

  @Override
  public void closeIndex(final Type type) throws IOException {
    switch(type) {
      case TXT: if(txtindex != null) txtindex.close(); break;
      case ATV: if(atvindex != null) atvindex.close(); break;
      case FTX: if(ftxindex != null) ftxindex.close(); break;
      case PTH: if(ftxindex != null) path.close(); break;
      default: break;
    }
  }

  @Override
  public void setIndex(final Type type, final Index index) {
    switch(type) {
      case TXT: if(meta.txtindex) txtindex = index; break;
      case ATV: if(meta.atvindex) atvindex = index; break;
      case FTX: if(meta.ftxindex) ftxindex = index; break;
      case PTH: if(meta.pathindex) path = (PathSummary) index; break;
      default: break;
    }
  }

  @Override
  public byte[] text(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? Token.token((int) o) :
      (text ? texts : values).readToken(o);
  }

  @Override
  public double textNum(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? o & (IO.NUMOFF - 1) :
      Token.toDouble((text ? texts : values).readToken(o));
  }

  @Override
  public int textLen(final int pre, final boolean text) {
    final long o = textOff(pre);
    return num(o) ? Token.numDigits((int) o) :
      (text ? texts : values).readNum(o);
  }

  /**
   * Returns true if the specified offset contains a numeric value.
   * @param o offset
   * @return result of check
   */
  private static boolean num(final long o) {
    return (o & IO.NUMOFF) != 0;
  }

  // UPDATE OPERATIONS ========================================================

  @Override
  protected void text(final int pre, final byte[] val, final boolean txt) {
    final long v = Token.toSimpleInt(val);
    if(v != Integer.MIN_VALUE) {
      textOff(pre, v | IO.NUMOFF);
    } else {
      final DataAccess da = txt ? texts : values;
      long o = textOff(pre);
      if(!num(o) && val.length <= textLen(pre, txt)) {
        // text is replaced with old one if it is shorter
        da.writeBytes(o, val);
      } else {
        // if old text is numeric or not placed last, append text at the end
        if(num(o) || da.readNum(o) + da.pos() != da.length()) o = da.length();
        // otherwise, replace it with new one
        da.writeBytes(o, val);
        textOff(pre, o);
      }
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
