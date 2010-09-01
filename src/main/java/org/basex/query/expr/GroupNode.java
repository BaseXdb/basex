package org.basex.query.expr;

import java.util.Arrays;

import org.basex.query.QueryException;
import org.basex.query.item.Item;

/**
 * GroupNode defines one valid partitioning setting.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
final class GroupNode {
  /** List of grouping var items. */
  final Item[] its;
  /** Hashes for the group representative values. */
  final int hash;

  /**
   * Creates a group node.
   * @param is grouping var items
   */
  GroupNode(final Item[] is) {
    its = is;
    int h = 0;
    for(final Item it : its) h = (h << 5) - h + it.hashCode();
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
      if(!its[i].equiv(null, c.its[i])) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  /* for debugging (should be removed later) */
  @Override
  public String toString() {
    return " with grouping var " + Arrays.toString(its);
  }
}
