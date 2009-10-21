package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Insert before primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertBeforePrimitive extends InsertPrimitive {  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l actual pre location where nodes are inserted
   */
  public InsertBeforePrimitive(final Nod n, final Iter copy, final int l) {
    super(n, copy, l);
  }
  
  @SuppressWarnings("unused")
  @Override
  public void apply(final int add) throws QueryException {
    if(!(node instanceof DBNode)) return;
    
    // source nodes may be empty, thus insert has no effect at all
    if(m == null) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    // [LK] check if parent null?
    d.insertSeq(n.pre, d.parent(n.pre, Nod.kind(node.type)), m);
  }

  @SuppressWarnings("unused")
  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTBEFORE;
  }
}
