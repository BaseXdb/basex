package org.basex.build;

import org.basex.data.MemData;
import org.basex.data.MetaData;
import org.basex.data.Stats;

/**
 * This class creates a memory based database instance.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemBuilder extends Builder {
  /** Data reference. */
  protected MemData data;

  @Override
  public MemBuilder init(final String db) {
    // always index values in main memory mode
    meta = new MetaData(db);
    meta.txtindex = true;
    meta.atvindex = true;
    meta.ftxindex = false;
    meta.file = parser.file;
    stats = new Stats(db);
    data = new MemData(64, tags, atts);
    data.meta = meta;
    return this;
  }

  @Override
  public MemData finish() {
    data.initNames();
    return data;
  }
  
  @Override
  public void close() { }

  @Override
  public void addNode(final int tag, final int par, final byte[][] atr,
      final int[] attRef, final byte kind) {

    // element node
    final int attl = attRef != null ? attRef.length : 0;
    data.addElem(tag, par, attl + 1, attl + 1, kind);

    // attributes
    for(int a = 0; a < attl; a++) {
      data.addAtt(attRef[a], atr[(a << 1) + 1], a + 1);
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
    data.finishTag(pre);
  }
}
