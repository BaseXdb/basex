package org.basex.gui.view.tree;

/**
 * This class is used to store subtree borders.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
final class TreeBorder {
  /** Real Level. */
  int level;
  /** Start index. */
  int start;
  /** Size. */
  int size;

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
