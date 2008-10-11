package org.basex.build;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.data.MemData;
import org.basex.data.MetaData;

/**
 * This class creates a memory based database instance.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemBuilder extends Builder {
  /** Data reference. */
  private MemData data;

  @Override
  public MemBuilder init(final String db) {
    // always index values in main memory mode
    meta = new MetaData(db);
    meta.txtindex = true;
    meta.atvindex = true;
    meta.ftxindex = false;
    meta.file = parser.io;
    data = new MemData(64, tags, atts, ns, skel);
    data.meta = meta;
    return this;
  }

  @Override
  public MemData finish() throws IOException {
    // check if data ranges exceed database limits
    if(tags.size() > 0xFFF) throw new IOException(LIMITTAGS);
    if(atts.size() > 0xFFF) throw new IOException(LIMITATTS);
    data.initNames();
    return data;
  }
  
  @Override
  public void close() { }

  @Override
  protected void addDoc(final byte[] tok) {
    data.addDoc(tok, 0);
    size = data.size;
  }

  @Override
  protected void addElem(final int tok, final int s, final int dis,
      final int a, final boolean n) throws IOException {
    data.addElem(tok, s, dis, a, 0, n);
    size = data.size;
    if(size < 0) throw new IOException(LIMITRANGE);
  }

  @Override
  protected void addAttr(final int n, final int s, final byte[] v,
      final int d) {
    data.addAtt(n, s, v, d);
    size = data.size;
  }

  @Override
  protected void addText(final byte[] tok, final int par, final byte kind) {
    data.addText(tok, par, kind);
    size = data.size;
  }

  @Override
  protected void addSize(final int pre) {
    data.finishElem(pre);
  }
}
