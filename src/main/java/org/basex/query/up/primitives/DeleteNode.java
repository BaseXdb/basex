package org.basex.query.up.primitives;

import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Delete primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class DeleteNode extends Primitive {
  /**
   * Constructor.
   * @param ii input info
   * @param n expression target node
   */
  public DeleteNode(final InputInfo ii, final ANode n) {
    super(PrimitiveType.DELETENODE, ii, n);
  }

  @Override
  public int apply(final int add) {
    final DBNode n = (DBNode) node;
    n.data.delete(n.pre + add);
    return 0;
  }

  @Override
  public void update(final NamePool pool) {
    pool.remove(node);
  }

  @Override
  public String toString() {
    return Util.info("%[%]", Util.name(this), node);
  }
}
