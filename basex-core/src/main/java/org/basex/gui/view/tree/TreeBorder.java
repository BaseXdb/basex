package org.basex.gui.view.tree;

/**
 * This class is used to store subtree borders.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param level level
   * @param start start
   * @param size size
   */
  TreeBorder(final int level, final int start, final int size) {
    this.level = level;
    this.start = start;
    this.size = size;
  }

  /**
   * Returns end index.
   * @return end
   */
  int getEnd() {
    return start + size - 1;
  }
}
