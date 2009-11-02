package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.up.UpdateFunctions;
import org.basex.query.util.Err;

/**
 * Represents a replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopy {
  /** Target node is an attribute. */
  public boolean a;

  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   * @param attr replacing nodes are attributes
   */
  public ReplacePrimitive(final Nod n, final Iter replace, 
      final boolean attr) {
    super(n, replace);
    a = attr;
  }
  
  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
      
    final DBNode n = (DBNode) node;
    final int p = n.pre + add;
    final Data d = n.data;
//    InfoTable.pT(d, 100, -1);
    // source nodes may be empty, thus the replace results in deleting the 
    // target node
    if(m == null) {
      d.delete(p);
      return;
    }
    final int k = Nod.kind(n.type);
    final int par = d.parent(p, k);
    final int s = m.size(0, Data.DOC) - 1;
    if(a)
      UpdateFunctions.insertAttributes(p, par, d, m);
    else d.insertSeq(p + d.size(p, k), par , m);
    d.delete(p + (a ? s : 0));
  }

  @Override
  public Type type() {
    return REPLACENODE;
  }
  
  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREPL, node);
  }
}
