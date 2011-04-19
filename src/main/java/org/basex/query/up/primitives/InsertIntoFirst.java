package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;

/**
 * Insert into as first primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertIntoFirst extends InsertBase {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertIntoFirst(final InputInfo ii, final ANode n,
      final NodeCache copy) {
    super(ii, n, copy);
  }

  @Override
  public int apply(final int add) {
    final DBNode n = (DBNode) node;
    final int pre = n.pre + add;
    final Data d = n.data;
    d.insert(pre + d.attSize(pre, d.kind(pre)), pre, md);
    return 0;
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTINTOFIRST;
  }
}
