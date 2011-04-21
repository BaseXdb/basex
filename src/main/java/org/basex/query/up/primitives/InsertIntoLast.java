package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;

/**
 * Insert into as last primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class InsertIntoLast extends InsertBase {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param nc copy of nodes to be inserted
   */
  public InsertIntoLast(final InputInfo ii, final ANode n,
      final NodeCache nc) {
    super(ii, n, nc);
  }

  @Override
  public int apply(final int add) throws QueryException {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    int pre = n.pre + d.size(n.pre, d.kind(n.pre)) + add;
    d.insert(pre, n.pre, md);
    if(!mergeTexts(d, pre - 1, pre)) {
      pre += md.meta.size;
      mergeTexts(d, pre - 1, pre);
    }
    return 0;
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTINTOLAST;
  }
}
