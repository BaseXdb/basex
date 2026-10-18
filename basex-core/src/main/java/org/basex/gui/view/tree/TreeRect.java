package org.basex.gui.view.tree;

/**
 * Tree rectangles.
 *
 * @author BaseX Team, BSD License
 * @author Wolfgang Miller
 * @param x x position
 * @param w width
 */
record TreeRect(int x, int w) {
  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @return result of check
   */
  boolean contains(final int xx) {
    return xx >= x && xx <= x + w;
  }

  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @param ww width
   * @return result of check
   */
  boolean contains(final int xx, final int ww) {
    return xx + ww >= x && xx <= x + w;
  }
}
