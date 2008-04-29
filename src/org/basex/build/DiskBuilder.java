package org.basex.build;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.core.proc.Drop;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.data.Stats;
import org.basex.io.IO;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.TableAccess;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableOutput;
import org.basex.util.Performance;
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
  protected DataOutput tout;
  /** Database texts. */
  protected DataOutput xout;
  /** Database values. */
  protected DataOutput vout;

  /** Text pointer. */
  protected long txtlen;
  /** Attribute value pointer. */
  protected long vallen;

  /** Database size stream (temporary). */
  protected DataOutput sout;
  /** Database size stream (counter). */
  protected int ssize;

  @Override
  public DiskBuilder init(final String db) throws IOException {
    meta = new MetaData(db);
    meta.file = parser.file;
    meta.filesize = meta.file.length();
    meta.time = meta.file.date();
    stats = new Stats(db);
    
    Drop.drop(db);
    IO.dbpath(db).mkdirs();

    // calculate output buffer sizes: (1 << BLOCKPOWER) < bs < (1 << 22)
    int bs = 1 << IO.BLOCKPOWER;
    while(bs < meta.filesize && bs < (1 << 22)) bs <<= 1;

    tout = new DataOutput(new TableOutput(db, DATATBL));
    xout = new DataOutput(db, DATATXT, bs);
    vout = new DataOutput(db, DATAATV, bs);
    sout = new DataOutput(db, DATATMP, bs);
    return this;
  }

  @Override
  protected Data finish() throws IOException {
    // close files
    tags.finish(meta.dbname);
    atts.finish(meta.dbname);
    close();
    
    // write size attribute into main table
    final TableAccess ta = new TableDiskAccess(meta.dbname, DATATBL);
    final DataInput in = new DataInput(meta.dbname, DATATMP);
    for(int pre = 0; pre < ssize; pre++) {
      final int pp = in.readNum();
      final int sz = in.readNum();
      ta.write4(pp, 4, sz);
    }
    in.close();
    ta.close();
    
    IO.dbfile(meta.dbname, DATATMP).delete();

    meta.write(size);
    stats.finish();
    stats.write();
    // return database instance, excluding indexes
    return new DiskData(meta.dbname, false);
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
  protected void addNode(final int tag, final int par, final byte[][] atr,
      final int[] attRef, final byte kind) throws IOException {

    // element node
    final int attl = attRef != null ? attRef.length : 0;
    tout.write(kind);
    tout.write2(tag);
    tout.write(attl + 1);
    tout.writeInt(attl + 1);
    tout.writeInt(par);
    tout.writeInt(size++);

    // attributes
    for(int a = 0; a < attl; a++) {
      final byte[] val = atr[(a << 1) + 1];
      // inline integer value?
      long v = Token.toSimpleInt(val);
      if(v != Integer.MIN_VALUE) {
        v |= 0x8000000000L;
      } else {
        v = vallen;
        vallen += vout.writeToken(val);
      }
      
      tout.write(Data.ATTR);
      tout.write2(attRef[a]);
      tout.write5(v);
      tout.write(0);
      tout.write(0);
      tout.write(0);
      tout.write(a + 1);
      tout.writeInt(size++);
    }
  }

  @Override
  public void addText(final byte[] txt, final int par, final byte kind)
      throws IOException {
    
    // inline integer values...
    long v = Token.toSimpleInt(txt);
    if(v != Integer.MIN_VALUE) {
      v |= 0x8000000000L;
    } else {
      v = txtlen;
      txtlen += xout.writeToken(txt);
    }
    
    tout.write(kind);
    tout.write(0);
    tout.write(0);
    tout.write5(v);
    tout.writeInt(par);
    tout.writeInt(size++);
  }

  @Override
  public void addSize(final int pre) throws IOException {
    sout.writeNum(pre);
    sout.writeNum(size - pre);
    ssize++;
  }

  /**
   * Test method for building the database and storing the table to disk.
   * @param args files to be built
   */
  public static void main(final String[] args) {
    Prop.read();
    
    // get filename(s) or use default
    //final String[] fn = args.length > 0 ? args : new String[] { "input.xml" };
    final String[] fn = new String[] { "/home/dbis/xml/11mb.xml" };    
    int runs = 2;
    run(runs, fn, false);
    run(runs, fn, true);
  }

  /**
   * Runs the test.
   * @param r number of runs
   * @param fn files
   * @param s sax flag
   */
  public static void run(final int r, final String[] fn, final boolean s) {
    int c = 0;
    int w = 0;

    Performance p = new Performance();
    for(int i = 0; i < r; i++) {
      for(final String f : fn) {
        try {
          final IO bxf = new IO(f);
          Parser parser = s ? new SAXWrapper(bxf) : new XMLParser(bxf);
          new DiskBuilder().build(parser, "tmp");
          c++;
        } catch(final IOException e) {
          w++;
        }
      }
    }

    BaseX.outln((s ? "Xerces" : "BaseX") + " Parser, " + r + " runs.");
    BaseX.outln("% documents built in %.", fn.length, p.getTimer(r));
    BaseX.outln("% documents accepted.", c);
    BaseX.outln("% documents rejected.\n", w);
  }
}
