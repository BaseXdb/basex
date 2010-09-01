package org.basex.query.expr;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;

/**
 * GroupNode defines one valid partitioning setting.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
final class GroupNode {
  /** List of grouping var items. */
  final Value[] its;
  /** Hashes for the group representative values. */
  private final int hash;

  /**
   * Creates a group node.
   * @param is grouping var items
   */
  GroupNode(final Value[] is) {
    its = is;
    int h = 0;
    for(final Value it : its) h = (h << 5) - h + it.hashCode();
    hash = h;
  }

  /**
   * Checks the nodes for equality.
   * @param c second group node
   * @return result of check
   * @throws QueryException query exception
   */
  boolean eq(final GroupNode c) throws QueryException {
    if(its.length != c.its.length) return false;
    for(int i = 0; i < its.length; ++i) {
      final boolean isitem = its[i].item();
      if(isitem ^ c.its[i].item() || its[i].empty() ^ c.its[i].empty())
        return false;
      if(isitem && !((Item) its[i]).equiv(null, (Item) c.its[i])) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return hash;
  }

}
