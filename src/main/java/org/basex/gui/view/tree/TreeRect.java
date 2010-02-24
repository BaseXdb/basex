package org.basex.gui.view.tree;


/**
 * This is class is used to handle rectangles.
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
public class TreeRect {
  /** X position. */
  public int x;
  /** Width. */
  public int w;

  /**
   * Initializes TreeRect.
   * @param xx x position
   * @param ww width
   */
  public TreeRect(final int xx, final int ww) {
    x = xx;
    w = ww;
  }

  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @return result of comparison
   */
  public boolean contains(final int xx) {
    return xx >= x && xx <= x + w || xx >= x + w && xx <= x;
  }
  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @param ww width
   * @return result of comparison
   */
  public boolean contains(final int xx, final int ww) {
    return xx <= x && xx + ww >= x || xx <= x + w && xx + ww >= x;
  }
}