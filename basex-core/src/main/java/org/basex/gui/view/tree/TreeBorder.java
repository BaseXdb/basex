package org.basex.gui.view.tree;

/**
 * Subtree borders.
 * @param level real Level
 * @param start start index
 * @param size  size
 *
 * @author BaseX Team, BSD License
 * @author Wolfgang Miller
 */
record TreeBorder(int level, int start, int size) {
  /**
   * Returns end index.
   * @return end
   */
  int getEnd() {
    return start + size - 1;
  }
}
