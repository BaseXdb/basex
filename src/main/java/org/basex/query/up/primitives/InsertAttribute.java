package org.basex.query.up.primitives;

import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;

/**
 * Insert attribute primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertAttribute extends InsertBase {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param copy insertion nods
   */
  public InsertAttribute(final InputInfo ii, final ANode n,
      final NodeCache copy) {
    super(ii, n, copy);
  }

  @Override
  public int apply(final int add) {
    if(md != null) {
      final DBNode n = (DBNode) node;
      n.data.insertAttr(n.pre + 1, n.pre, md);
    }
    return 0;
  }

  @Override
  public void update(final NamePool pool) {
    if(md == null) return;
    add(pool);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTATTR;
  }
}
