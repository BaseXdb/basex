package org.basex.query.up.primitives;

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
//    int a = leftSibling(d, pos);
    // source nodes may be empty, thus insert has no effect at all
    n.data.insertSeq(pos, n.pre, m);
//    if(m.meta.size < 1) return;
//    if(a > -1 && m.meta.size > a + 1) {
//      mergeTextNodes(d, a, a + 1);
//      // if nodes could be merged on left hand, no more merges possible
//      return;
//    }
//    a = pos + m.meta.size;
//    mergeTextNodes(d, a, a + d.size(a, d.kind(a)));
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