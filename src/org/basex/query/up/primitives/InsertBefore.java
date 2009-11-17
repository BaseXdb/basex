package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;

/**
 * Insert before primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertBefore extends NodeCopy {  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertBefore(final Nod n, final NodIter copy) {
    super(n, copy);
  }
  
  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    // source nodes may be empty, thus insert has no effect at all
    if(m == null) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pos = n.pre;
//    InfoTable.pT(d, 180, -1);
    d.insert(pos, d.parent(pos, Nod.kind(node.type)), m);
//    final int s = m.meta.size;
//    InfoTable.pT(d, 180, -1);
//    if(!mergeTextNodes(d, pos + s - 1, pos + s)) 
//      mergeTextNodes(d, pos - 1, pos);
//    InfoTable.pT(d, 180, -1);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTBEFORE;
  }
  
  @Override
  public boolean addAtt() {
    return false;
  }
  
  @Override
  public boolean remAtt() {
    return false;
  }
}
