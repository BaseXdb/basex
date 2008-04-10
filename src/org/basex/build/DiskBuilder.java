package org.basex.build;

import static org.basex.data.DataText.*;
import static org.basex.io.IOConstants.BLOCKPOWER;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.core.proc.Drop;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.data.Stats;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IOConstants;
import org.basex.io.TableAccess;
import org.basex.io.TableDiskAccess;
import org.basex.io.TableOutput;
import org.basex.util.Token;

/**
 * This class creates a disk based database instance.
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
    meta.filename = parser.file;
    final File f = new File(parser.file);
    meta.filesize = f.length();
    meta.time = f.lastModified();

    stats = new Stats(db);
    
    Drop.drop(db);
    IOConstants.dbpath(db).mkdirs();

    // calculate output buffer sizes: (1 << 12) < bs < (1 << 22)
    int bs = 1 << BLOCKPOWER;
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
    
    final TableAccess ta = new TableDiskAccess(meta.dbname, DATATBL);
    final DataInput in = new DataInput(meta.dbname, DATATMP);
    for(int pre = 0; pre < ssize; pre++) {
      final int pp = in.readInt();
      final int sz = in.readInt() - pp;
      ta.write4(pp, 4, sz);
    }
    in.close();
    ta.close();
    
    IOConstants.dbfile(meta.dbname, DATATMP).delete();

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
      final int[] attRef, final int type) throws IOException {

    // element node
    final int attl = attRef != null ? attRef.length : 0;
    tout.write(type);
    tout.write2(tag);
    tout.write(attl + 1);
    tout.writeInt(attl + 1);
    tout.writeInt(par);
    tout.writeInt(size++);

    // attributes
    for(int a = 0; a < attl; a++) {
      // inline integer values...
      final byte[] val = atr[(a << 1) + 1];
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
  public void addText(final byte[] txt, final int par, final int type)
      throws IOException {
    
    // inline integer values...
    long v = Token.toSimpleInt(txt);
    if(v != Integer.MIN_VALUE) {
      v |= 0x8000000000L;
    } else {
      v = txtlen;
      txtlen += xout.writeToken(txt);
    }
    
    tout.write(type);
    tout.write(0);
    tout.write(0);
    tout.write5(v);
    tout.writeInt(par);
    tout.writeInt(size++);
  }

  @Override
  public void addSize(final int pre) throws IOException {
    sout.writeInt(pre);
    sout.writeInt(size);
    ssize++;
  }

  /**
   * Test method for the, building the database and storing the table to disk.
   * @param args files to be built
   */
  public static void main(final String[] args) {
    Prop.read();

    // get filename(s) or use default
    final String[] fn = args.length > 0 ? args : new String[] { "input.xml" };
    int c = 0;
    int w = 0;
    for(final String f : fn) {
      try {
        new DiskBuilder().build(new XMLParser(f), "tmp");
        c++;
      } catch(final IOException e) {
        w++;
      }
    }

    BaseX.outln("\n% documents built.", fn.length);
    BaseX.outln("% documents accepted.", c);
    BaseX.outln("% documents rejected.", w);
  }
}
