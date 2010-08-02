package org.basex.query.up.primitives;

import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;

/**
 * Delete primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class DeletePrimitive extends UpdatePrimitive {
  /**
   * Constructor.
   * @param ii input info
   * @param n expression target node
   */
  public DeletePrimitive(final InputInfo ii, final Nod n) {
    super(ii, n);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    n.data.delete(n.pre + add);
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
