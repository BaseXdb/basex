package org.basex.gui.view.tree;

/**
 * This is class is used to handle rectangles containing more than one node.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
final class TreeRect {
  /** X position. */
  public int x;
  /** Width. */
  public int w;
  /**
   * Rectangle pre value. In case of multiple pre values used to buffer selected
   * pre.
   */
  public int pre = -1;
  /**
   * array containing all pre values inside the rectangle.
   */
  public int[] multiPres = null;

  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @return result of comparison
   */
  public boolean contains(final int xx) {
    return xx >= x && xx <= x + w || xx >= x + w && xx <= x;
  }
}
