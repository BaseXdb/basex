package org.basex.build;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.MetaData;

/**
 * This class creates a memory based database instance.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemBuilder extends Builder {
  // [CG] namespaces are currently ignored...
  
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
    data = new MemData(64, tags, atts, ns);
    data.meta = meta;
    return this;
  }

  @Override
  public MemData finish() throws IOException {
    // check if data ranges exceed database limits
    if(size > 0x1FFFFF) throw new IOException(LIMITRANGE);
    if(tags.size() > 0xFF) throw new IOException(LIMITTAGS);
    if(atts.size() > 0xFF) throw new IOException(LIMITATTS);

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
  protected void addElem(final int n, final int s, final int dis, final int a) {
    data.addElem(n, s, dis, a, a);
    size = data.size;
  }

  @Override
  protected void addAttr(final int n, final int s, final byte[] v,
      final int d) {
    data.addAtt(n, s, v, d, Data.ATTR);
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
