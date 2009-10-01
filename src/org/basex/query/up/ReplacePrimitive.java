package org.basex.query.up;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Represents a replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends UpdatePrimitive {
  /** Nodes replacing the target. */
  final MemData r;

  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   */
  public ReplacePrimitive(final Nod n, final MemData replace) {
    super(n);
    r = replace;
  }

  @Override
  public void apply() {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final Data data = n.data;
    final int k = Nod.kind(n.type);
    data.insertSeq(n.pre + data.size(n.pre, k), data.parent(n.pre, k), r);
    data.delete(n.pre);
  }
}
