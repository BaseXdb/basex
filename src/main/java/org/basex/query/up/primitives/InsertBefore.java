package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;

/**
 * Insert before primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertBefore extends InsertBase {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertBefore(final InputInfo ii, final ANode n, final NodeCache copy) {
    super(PrimitiveType.INSERTBEFORE, ii, n, copy);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pre = n.pre;
    d.insert(pre, d.parent(pre, d.kind(pre)), md);
  }

  @Override
  public int addend() {
    return md.meta.size;
  }
}
