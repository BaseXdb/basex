package org.basex.build;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.TableAccess;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableOutput;
import org.basex.util.Token;

/**
 * This class creates a disk based database instance. The disk layout is
 * described in the {@link DiskData} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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

  @Override
  public DiskBuilder init(final String db) throws IOException {
    meta = new MetaData(db);
    meta.file = parser.io;
    meta.filesize = meta.file.length();
    meta.time = meta.file.date();
    
    DropDB.drop(db);
    IO.dbpath(db).mkdirs();

    // calculate output buffer sizes: (1 << BLOCKPOWER) < bs < (1 << 22)
    int bs = IO.BLOCKSIZE;
    while(bs < meta.filesize && bs < (1 << 22)) bs <<= 1;

    tout = new DataOutput(new TableOutput(db, DATATBL));
    xout = new DataOutput(db, DATATXT, bs);
    vout = new DataOutput(db, DATAATV, bs);
    sout = new DataOutput(db, DATATMP, bs);
    return this;
  }

  @Override
  protected synchronized DiskData finish() throws IOException {
    // check if data ranges exceed database limits
    if(tags.size() > 0x0FFF) throw new IOException(LIMITTAGS);
    if(atts.size() > 0x0FFF) throw new IOException(LIMITATTS);
    close();
    
    // close files
    final String db = meta.dbname;
    final DataOutput out = new DataOutput(meta.dbname, DATAINFO);
    meta.finish(out, size);
    tags.finish(out);
    atts.finish(out);
    skel.finish(out);
    ns.finish(out);
    out.close();
    
    // write size attribute into main table
    final TableAccess ta = new TableDiskAccess(db, DATATBL);
    final DataInput in = new DataInput(db, DATATMP);
    for(int pre = 0; pre < ssize; pre++) {
      ta.write4(in.readNum(), 8, in.readNum());
    }
    ta.close();
    in.close();
    
    IO.dbfile(db, DATATMP).delete();

    // return database instance
    return new DiskData(db, false);
  }
  
  @Override
  public void close() throws IOException {
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
    tout.write(0);
    tout.write(0);
    tout.write5(inline(txt, true));
    tout.writeInt(0);
    tout.writeInt(size++);
  }
  
  @Override
  protected void addElem(final int t, final int s, final int dis,
      final int as, final boolean n) throws IOException {

    tout.write(Data.ELEM);
    tout.write2(t + (s << 12) + (n ? 1 << 11 : 0));
    tout.write(as);
    tout.writeInt(dis);
    tout.writeInt(as);
    tout.writeInt(size++);
    if(size < 0) throw new IOException(LIMITRANGE);
  }

  @Override
  public void addAttr(final int t, final int s, final byte[] txt,
      final int dis) throws IOException {

    tout.write(Data.ATTR);
    tout.write2(t + (s << 12));
    tout.write5(inline(txt, false));
    tout.writeInt(dis);
    tout.writeInt(size++);
  }

  @Override
  public void addText(final byte[] txt, final int dis, final byte kind)
      throws IOException {
    
    tout.write(kind);
    tout.write(0);
    tout.write(0);
    tout.write5(inline(txt, true));
    tout.writeInt(dis);
    tout.writeInt(size++);
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
  public void addSize(final int pre) throws IOException {
    sout.writeNum(pre);
    sout.writeNum(size - pre);
    ssize++;
  }
}
