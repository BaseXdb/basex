package org.basex.build;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.io.TableAccess;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableOutput;
import org.basex.util.Compress;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class creates a disk based database instance. The storage layout is
 * described in the {@link Data} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DiskBuilder extends Builder {
  /** Database table. */
  private DataOutput tout;
  /** Database texts. */
  private DataOutput xout;
  /** Database values. */
  private DataOutput vout;
  /** Output stream for temporary values. */
  private DataOutput sout;

  /** Text pointer. */
  private long txtlen;
  /** Attribute value pointer. */
  private long vallen;

  /** Text compressor. */
  private final Compress comp;

  /**
   * Constructor.
   * @param parse parser
   * @param pr properties
   */
  public DiskBuilder(final Parser parse, final Prop pr) {
    super(parse, pr);
    comp = new Compress(pr.num(Prop.COMPRESS));
  }

  @Override
  public DiskData build(final String name) throws IOException {
    DropDB.drop(name, prop);
    prop.dbpath(name).mkdirs();

    meta = new MetaData(name, prop);
    meta.file = parser.file;
    meta.filesize = meta.file.length();
    meta.time = meta.file.date() != 0 ? meta.file.date() :
      System.currentTimeMillis();

    // calculate optimized output buffer sizes to reduce disk fragmentation
    final Runtime rt = Runtime.getRuntime();
    int bs = (int) Math.min(meta.filesize, Math.min(1 << 22,
        rt.maxMemory() - rt.freeMemory() >> 2));
    bs = Math.max(IO.BLOCKSIZE, bs - bs % IO.BLOCKSIZE);

    tout = new DataOutput(new TableOutput(meta, DATATBL));
    xout = new DataOutput(meta.file(DATATXT), bs);
    vout = new DataOutput(meta.file(DATAATV), bs);
    sout = new DataOutput(meta.file(DATATMP), bs);

    parse(name);
    close();

    // copy temporary values into database table
    final TableAccess ta = new TableDiskAccess(meta, DATATBL);
    final DataInput in = new DataInput(meta.file(DATATMP));
    for(; spos < ssize; ++spos) ta.write4(in.readNum(), 8, in.readNum());
    ta.close();
    in.close();
    meta.file(DATATMP).delete();

    // return database instance
    return new DiskData(meta, tags, atts, path, ns);
  }

  @Override
  public void abort() {
    try {
      close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    DropDB.drop(meta.name, meta.prop);
  }

  @Override
  public void close() throws IOException {
    if(tout != null) tout.close();
    if(xout != null) xout.close();
    if(vout != null) vout.close();
    if(sout != null) sout.close();
    tout = null;
    xout = null;
    vout = null;
    sout = null;
  }

  @Override
  protected void addDoc(final byte[] txt) throws IOException {
    tout.write1(Data.DOC);
    tout.write2(0);
    tout.write5(textOff(txt, true));
    tout.write4(0);
    tout.write4(meta.size++);
  }

  @Override
  protected void addElem(final int dis, final int n, final int as, final int u,
      final boolean ne) throws IOException {

    tout.write1(as << 3 | Data.ELEM);
    tout.write2((ne ? 1 << 15 : 0) | n);
    tout.write1(u);
    tout.write4(dis);
    tout.write4(as);
    tout.write4(meta.size++);
  }

  @Override
  protected void addAttr(final int n, final byte[] v, final int dis,
      final int u) throws IOException {

    tout.write1(dis << 3 | Data.ATTR);
    tout.write2(n);
    tout.write5(textOff(v, false));
    tout.write4(u);
    tout.write4(meta.size++);
  }

  @Override
  protected void addText(final byte[] txt, final int dis, final byte kind)
      throws IOException {

    tout.write1(kind);
    tout.write2(0);
    tout.write5(textOff(txt, true));
    tout.write4(dis);
    tout.write4(meta.size++);
  }

  @Override
  protected void setSize(final int pre, final int val) throws IOException {
    sout.writeNum(pre);
    sout.writeNum(val);
    ++ssize;
  }

  /**
   * Calculates the text offset and writes the text value.
   * @param val value to be inlined
   * @param txt text/attribute flag
   * @return inline value or text position
   * @throws IOException I/O exception
   */
  private long textOff(final byte[] val, final boolean txt) throws IOException {
    // inline integer values...
    long v = Token.toSimpleInt(val);
    if(v != Integer.MIN_VALUE) return v | IO.NUMOFF;

    // compress text
    final byte[] cpr = comp.pack(val);
    if(txt) {
      v = txtlen;
      txtlen += xout.writeToken(cpr);
    } else {
      v = vallen;
      vallen += vout.writeToken(cpr);
    }
    return (cpr != val) ? v | IO.CPROFF : v;
  }
}
