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
  public void addDoc(final byte[] tok) {
    data.addDoc(tok, 0);
  }

  @Override
  public void addElem(final int tok, final int s, final int dis,
      final int a, final boolean n) throws IOException {
    data.addElem(tok, s, dis, a, a, n);
    if(data.meta.size < 0) throw new IOException(LIMITRANGE);
  }

  @Override
  public void addAttr(final int n, final int s, final byte[] v,
      final int d) {
    data.addAtt(n, s, v, d);
  }

  @Override
  public void addText(final byte[] tok, final int par, final byte kind) {
    data.addText(tok, par, kind);
  }

  @Override
  public void setSize(final int pre, final int val) {
    data.setSize(pre, val);
  }

  @Override
  public void setAttValue(final int pre, final byte[] val) {
    data.setAttValue(pre, val);
  }
}
