package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;

/**
 * Insert after primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertAfter extends InsertBase {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertAfter(final InputInfo ii, final ANode n, final NodeCache copy) {
    super(PrimitiveType.INSERTAFTER, ii, n, copy);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pre = n.pre + add;
    final int k = d.kind(pre);
    d.insert(pre + d.size(pre, k), d.parent(pre, k), md);
  }
}
