package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Data;
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
    d.delete(p);
    if(p >= d.meta.size) return;
    final int l = p - 1; 
    if(l > 1 && d.parent(l, d.kind(l)) == d.parent(p, d.kind(p))) 
      mergeTextNodes(d, l, p);
  }

  @Override
  public Type type() {
    return DELETE;
  }

  @Override
  public void check() {
  }

  @Override
  public void merge(final UpdatePrimitive p) {
  }
}
