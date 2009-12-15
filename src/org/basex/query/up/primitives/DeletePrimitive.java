package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.up.NamePool;

/**
 * Delete primitive.
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
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int p = n.pre + add;
    d.delete(p);
    mergeTexts(d, p - 1, p);
  }

  @Override
  public void update(final NamePool pool) {
    pool.remove(node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.DELETE;
  }
}
