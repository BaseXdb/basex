package org.basex.gui.view.tree;

import org.basex.data.*;

/**
 * Offers an iterator for the children of a node. Could as well be
 * defined as generic child iterator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class ChildIterator {
  /** Data reference. */
  private final Data data;
  /** Maximum size. */
  private int size;
  /** Current pre value. */
  private int pre;

  /**
   * Default constructor.
   * @param d data reference
   * @param p value of directory node
   */
  ChildIterator(final Data d, final int p) {
    data = d;
    init(p);
  }

  /**
   * Initializes the iterator.
   * @param p root pre value
   */
  private void init(final int p) {
    final int k = data.kind(p);
    size = p + data.size(p, k);
    pre = p + data.attSize(p, k);
  }

  /**
   * Tests if the node offers more children.
   * @return result of check
   */
  boolean more() {
    return pre < size;
  }

  /**
   * Returns the pre value of the next child.
   * @return next child reference
   */
  int next() {
    final int p = pre;
    pre += data.size(pre, data.kind(pre));
    return p;
  }
}
