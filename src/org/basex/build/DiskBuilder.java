package org.basex.build;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.io.TableAccess;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableOutput;
import org.basex.util.Token;

/**
 * This class creates a disk based database instance. The disk layout is
 * described in the {@link DiskData} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DiskBuilder extends Builder {
  /** Database table. */
  private DataOutput tout;
  /** Database texts. */
  private DataOutput xout;
  /** Database values. */
  private DataOutput vout;

  /** Text pointer. */
  private long txtlen;
  /** Attribute value pointer. */
  private long vallen;

  /** Database size stream (temporary). */
  private DataOutput sout;
  /** Database size stream (counter). */
  private int ssize;

  /**
   * Constructor.
   * @param p parser
   */
  public DiskBuilder(final Parser p) {
    super(p);
  }

  @Override
  public void init(final String db) throws IOException {
    final Prop pr = parser.prop;
    meta = new MetaData(db, pr);
    meta.file = parser.io;
    meta.filesize = meta.file.length();
    meta.time = meta.file.date();

    DropDB.drop(db, pr);
    pr.dbpath(db).mkdirs();

    // calculate output buffer sizes: (1 << BLOCKPOWER) < bs < (1 << 22)
    int bs = IO.BLOCKSIZE;
    while(bs < meta.filesize && bs < 1 << 22) bs <<= 1;

    tout = new DataOutput(new TableOutput(db, DATATBL, pr));
    xout = new DataOutput(pr.dbfile(db, DATATXT), bs);
    vout = new DataOutput(pr.dbfile(db, DATAATV), bs);
    sout = new DataOutput(pr.dbfile(db, DATATMP), bs);
  }

  @Override
  protected synchronized DiskData finish() throws IOException {
    close();

    // copy temporary values into database table
    final String db = meta.name;
    final TableAccess ta = new TableDiskAccess(db, DATATBL, parser.prop);
    final DataInput in = new DataInput(parser.prop.dbfile(db, DATATMP));
    for(int pre = 0; pre < ssize; pre++) {
      final boolean sz = in.readBool();
      final int p = in.readNum();
      if(sz) ta.write4(p, 8, in.readNum());
      else ta.write5(p, 3, in.read5());
    }
    ta.close();
    in.close();
    parser.prop.dbfile(db, DATATMP).delete();

    // return database instance
    return new DiskData(meta, tags, atts, path, ns);
  }

  @Override
  public void close() throws IOException {
    if(tout == null) return;
    tout.close();
    tout = null;
    xout.close();
    xout = null;
    vout.close();
    vout = null;
    sout.close();
    sout = null;
  }

  @Override
  public void addDoc(final byte[] txt) throws IOException {
    tout.write(Data.DOC);
    tout.write2(0);
    tout.write5(inline(txt, true));
    tout.writeInt(0);
    tout.writeInt(meta.size++);
  }

  @Override
  public void addElem(final int t, final int s, final int dis,
      final int as, final boolean n) throws IOException {

    tout.write(Data.ELEM + (s << 4) + (n ? 1 << 3 : 0));
    tout.write2(t);
    tout.write(as);
    tout.writeInt(dis);
    tout.writeInt(as);
    tout.writeInt(meta.size++);
    if(meta.size < 0) throw new IOException(LIMITRANGE);
  }

  @Override
  public void addAttr(final int t, final int s, final byte[] txt,
      final int dis) throws IOException {

    tout.write(Data.ATTR + (s << 4));
    tout.write2(t);
    tout.write5(inline(txt, false));
    tout.writeInt(dis);
    tout.writeInt(meta.size++);
  }

  @Override
  public void addText(final byte[] txt, final int dis, final byte kind)
      throws IOException {

    tout.write(kind);
    tout.write2(0);
    tout.write5(inline(txt, true));
    tout.writeInt(dis);
    tout.writeInt(meta.size++);
  }

  /**
   * Calculates the value to be inlined or returns a text position.
   * @param val value to be inlined
   * @param txt text/attribute flag
   * @return inline value or text position
   * @throws IOException I/O exception
   */
  private long inline(final byte[] val, final boolean txt) throws IOException {
    // inline integer values...
    long v = Token.toSimpleInt(val);
    if(v != Integer.MIN_VALUE) {
      v |= 0x8000000000L;
    } else if(txt) {
      v = txtlen;
      txtlen += xout.writeBytes(val);
    } else {
      v = vallen;
      vallen += vout.writeBytes(val);
    }
    return v;
  }

  @Override
  public void setSize(final int pre, final int val) throws IOException {
    sout.writeBool(true);
    sout.writeNum(pre);
    sout.writeNum(val);
    ssize++;
  }

  @Override
  public void setAttValue(final int pre, final byte[] val) throws IOException {
    sout.writeBool(false);
    sout.writeNum(pre);
    sout.write5(inline(val, false));
    ssize++;
  }
}
