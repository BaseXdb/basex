package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Represents a delete primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class DeletePrimitive extends UpdatePrimitive {
  /**
   * Constructor.
   * @param n expression target node
   */
  public DeletePrimitive(final Nod n) {
    super(n);
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int p = n.pre + add;
//    System.out.println("before");
//    UpdateFunctions.printTable(n.data);
    final int l = leftSibling(d, p);
    deleteDBNodes(new Nodes(new int[]{p}, d));
    // [LK] merging text nodes ... looks crappy 
    if(l == -1) return;
    final int lk = d.kind(l);
    final int r = l + d.size(l, lk);
    if(d.parent(l, lk) == d.parent(r, d.kind(r)))
    mergeTextNodes(d, l, r);
//    System.out.println("after");
//    UpdateFunctions.printTable(n.data);
  }

  @Override
  public Type type() {
    return DELETE;
  }

  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @Override
  public void merge(final UpdatePrimitive p) {
  }
}
