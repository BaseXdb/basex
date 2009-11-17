package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;

/**
 * Insert into as first primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertIntoFirst extends NodeCopy {
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertIntoFirst(final Nod n, final NodIter copy) {
    super(n, copy);
  }
  
  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    // source nodes may be empty, thus insert has no effect at all
    if(m == null) return;
    final DBNode n = (DBNode) node;
    final int p = n.pre + add;
    final Data d = n.data;
    d.insert(p + d.attSize(p, Nod.kind(node.type)), p, m);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTINTOFI;
  }
}
