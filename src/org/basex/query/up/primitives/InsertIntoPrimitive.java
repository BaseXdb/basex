package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Represents an insert into primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertIntoPrimitive extends InsertPrimitive {
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l actual pre location where nodes are inserted
   */
  public InsertIntoPrimitive(final Nod n, final Iter copy, final int l) {
    super(n, copy, l);
  }
  
  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pos = n.pre + d.size(n.pre, Nod.kind(node.type));
    n.data.insertSeq(pos, n.pre, m);
    final int l = pos - 1;
    if(d.parent(l, d.kind(l)) == d.parent(pos, d.kind(pos)) && 
        mergeTextNodes(d, pos - 1, pos)) return;
    final int r = pos + m.size(0, m.kind(0));
    if(r < d.meta.size && d.parent(r, d.kind(r)) == 
      d.parent(r - 1, d.kind(r - 1))) mergeTextNodes(d, r, r - 1);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTINTO;
  }
}