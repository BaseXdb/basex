package org.basex.build;

import static org.basex.data.DataText.*;
import java.io.IOException;

import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.data.MetaData;
import org.basex.index.Names;
import org.basex.io.IO;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.out.TableOutput;
import org.basex.io.random.TableAccess;
import org.basex.io.random.TableDiskAccess;
import org.basex.util.Compress;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class creates a database instance on disk.
 * The storage layout is described in the {@link Data} class.
 *
 * @author BaseX Team 2005-11, BSD License
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

  /** Admin properties. */
  final MainProp mprop;
  /** Text compressor. */
  private final Compress comp;

  /**
   * Constructor.
   * @param nm name of database
   * @param parse parser
   * @param ctx database context
   */
  public DiskBuilder(final String nm, final Parser parse, final Context ctx) {
    super(nm, parse, ctx.prop);
    comp = new Compress();
    mprop = ctx.mprop;
  }

  @Override
  public DiskData build() throws IOException {
    DropDB.drop(name, mprop);
    mprop.dbpath(name).mkdirs();

    final IO file = parser.src;
    final MetaData md = new MetaData(name, prop, mprop);
    md.original = file != null ? file.path() : "";
    md.filesize = file != null ? file.length() : 0;
    md.time = file != null ? file.date() : System.currentTimeMillis();
    md.dirty = true;

    // calculate optimized output buffer sizes to reduce disk fragmentation
    final Runtime rt = Runtime.getRuntime();
    int bs = (int) Math.min(md.filesize, Math.min(1 << 22,
        rt.maxMemory() - rt.freeMemory() >> 2));
    bs = Math.max(IO.BLOCKSIZE, bs - bs % IO.BLOCKSIZE);

    tout = new DataOutput(new TableOutput(md, DATATBL));
    xout = new DataOutput(md.dbfile(DATATXT), bs);
    vout = new DataOutput(md.dbfile(DATAATV), bs);
    sout = new DataOutput(md.dbfile(DATATMP), bs);

    final Names tags = new Names(md);
    final Names atts = new Names(md);
    parse(md, tags, atts);
    close();

    // copy temporary values into database table
    final TableAccess ta = new TableDiskAccess(md, DATATBL);
    final DataInput in = new DataInput(md.dbfile(DATATMP));
    for(; spos < ssize; ++spos) ta.write4(in.readNum(), 8, in.readNum());
    ta.close();
    in.close();
    md.dbfile(DATATMP).delete();

    // return database instance
    return new DiskData(md, tags, atts, path, ns);
  }

  @Override
  public void abort() {
    try {
      close();
    } catch(final IOException ex) {
      Util.debug(ex);
    }
    DropDB.drop(meta.name, mprop);
  }

  @Override
  public void close() throws IOException {
    if(tout != null) tout.close();
    if(xout != null) xout.close();
    if(vout != null) vout.close();
    if(sout != null) sout.close();
    parser.close();
    tout = null;
    xout = null;
    vout = null;
    sout = null;
  }

  @Override
  protected void addDoc(final byte[] value) throws IOException {
    tout.write1(Data.DOC);
    tout.write2(0);
    tout.write5(textOff(value, true));
    tout.write4(0);
    tout.write4(meta.size++);
  }

  @Override
  protected void addElem(final int dist, final int nm, final int asize,
      final int uri, final boolean ne) throws IOException {

    tout.write1(asize << 3 | Data.ELEM);
    tout.write2((ne ? 1 << 15 : 0) | nm);
    tout.write1(uri);
    tout.write4(dist);
    tout.write4(asize);
    tout.write4(meta.size++);
  }

  @Override
  protected void addAttr(final int nm, final byte[] value, final int dist,
      final int uri) throws IOException {

    tout.write1(dist << 3 | Data.ATTR);
    tout.write2(nm);
    tout.write5(textOff(value, false));
    tout.write4(uri);
    tout.write4(meta.size++);
  }

  @Override
  protected void addText(final byte[] value, final int dist, final byte kind)
      throws IOException {

    tout.write1(kind);
    tout.write2(0);
    tout.write5(textOff(value, true));
    tout.write4(dist);
    tout.write4(meta.size++);
  }

  @Override
  protected void setSize(final int pre, final int size) throws IOException {
    sout.writeNum(pre);
    sout.writeNum(size);
    ++ssize;
  }

  /**
   * Calculates the text offset and writes the text value.
   * @param value value to be inlined
   * @param text text/attribute flag
   * @return inline value or text position
   * @throws IOException I/O exception
   */
  private long textOff(final byte[] value, final boolean text)
      throws IOException {

    // inline integer values...
    final long v = Token.toSimpleInt(value);
    if(v != Integer.MIN_VALUE) return v | IO.OFFNUM;

    // store text
    final DataOutput store = text ? xout : vout;
    final long off = store.size();
    final byte[] val = comp.pack(value);
    store.writeToken(val);
    return val == value ? off : off | IO.OFFCOMP;
  }
}
