package org.basex.gui.view.real;

/**
 * This class saves the positions of the RealView rectangles.
 * @author Wolfgang Miller
 */
public class RealRect {

  /** pre value. */
  public int p;
  /** position x1. */
  public int x1;
  /** position x2. */
  public int x2;
  /** position y1. */
  public int y1;
  /** position y2. */
  public int y2;

  /**constructor, saves rects.
   * @param pp pre value
   * @param xx1 position x1
   * @param xx2 position x2
   * @param yy1 position y1
   * @param yy2 position y2
   */

  public RealRect(final int pp, final int xx1, final int yy1, final int xx2,
      final int yy2) {
    p = pp;
    x1 = xx1;
    x2 = xx2;
    y1 = yy1;
    y2 = yy2;
  }

  /**returns true if position is in specific rect.
   * @param xx  postiion x
   * @param yy position y
   * @return boolean
   */
  boolean contains(final int xx, final int yy) {
    return xx >= x1 && yy >= y1 && xx <= x2 && yy <= y2;
  }

}
