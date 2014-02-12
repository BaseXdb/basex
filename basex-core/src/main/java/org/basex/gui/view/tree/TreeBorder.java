package org.basex.gui.view.tree;

/**
 * This class is used to store subtree borders.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Wolfgang Miller
 */
final class TreeBorder {
  /** Real Level. */
  final int level;
  /** Start index. */
  final int start;
  /** Size. */
  final int size;

  /**
   * Stores subtree borders.
   * @param lv level
   * @param st start
   * @param si size
   */
  TreeBorder(final int lv, final int st, final int si) {
    level = lv;
    start = st;
    size = si;
  }

  /**
   * Returns end index.
   * @return end
   */
  int getEnd() {
    return start + size - 1;
  }
}
