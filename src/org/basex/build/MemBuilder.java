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
    meta.file = parser.file;
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
  public void addNode(final int tag, final int tns, final int par,
      final byte[][] atr, final int[] attRef, final byte kind) {
    
    // element node
    final int attl = attRef != null ? attRef.length >> 1 : 0;
    data.addElem(tag, tns, par, attl + 1, attl + 1, kind);

    // attributes
    for(int a = 0; a < attl; a++) {
      data.addAtt(attRef[a << 1], attRef[(a << 1) + 1],
          atr[(a << 1) + 1], a + 1);
    }
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
