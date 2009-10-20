package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
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
  
  @SuppressWarnings("unused")
  @Override
  public void apply(final int add) throws QueryException {
    if(!(node instanceof DBNode)) return;
    
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pos = n.pre + d.size(n.pre, Nod.kind(node.type));
    n.data.insertSeq(pos, n.pre, m);
    final int ls = leftSibling(d, pos);
    if(ls != -1 && mergeTextNodes(d, ls, pos)) return;
    final int rs = pos + d.size(pos, d.kind(pos));
    if(rs == d.size(0, d.kind(0)) || d.parent(rs, d.kind(rs)) != 
      d.parent(pos, d.kind(pos))) return;
    mergeTextNodes(d, pos, rs);
  }

  @SuppressWarnings("unused")
  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTINTO;
  }
}