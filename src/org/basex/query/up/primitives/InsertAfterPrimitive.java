package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Insert after primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertAfterPrimitive extends InsertPrimitive {  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l actual pre location where nodes are inserted
   */
  public InsertAfterPrimitive(final Nod n, final Iter copy, final int l) {
    super(n, copy, l);
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    // source nodes may be empty, thus insert has no effect at all
    if(m == null) return;
    final DBNode n = (DBNode) node;
    final int p = n.pre + add;
    final Data d = n.data;
    final int k = Nod.kind(node.type);
    // [LK] check if parent null?
    d.insertSeq(p + d.size(p, k), d.parent(p, k), m);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTAFTER;
  }
}
