package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Data;
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
    final int ls = leftSibling(d, p);
    d.delete(p);
    if(ls == -1) return;
    final int rs = ls + d.size(ls, d.kind(ls));
    if(rs >= d.size(0, d.kind(0)) || d.parent(rs, d.kind(rs)) != 
      d.parent(ls, d.kind(ls))) return;
    mergeTextNodes(d, ls, rs);
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
