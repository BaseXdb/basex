package org.basex.gui.view;

/**
 * View Rectangle.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class ViewRect {
  /** X position. */
  public int x;
  /** Y position. */
  public int y;
  /** Width. */
  public int w;
  /** Height. */
  public int h;
  /** Rectangle pre value. */
  public int pre;
  /** Level. */
  public int level;

  /**
   * Default constructor.
   */
  public ViewRect() { }

  /**
   * Simple rectangle constructor.
   * @param xx x position
   * @param yy y position
   * @param ww width
   * @param hh height
   */
  public ViewRect(final int xx, final int yy, final int ww, final int hh) {
    x = xx;
    y = yy;
    w = ww;
    h = hh;
  }

  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @param yy y position
   * @return result of comparison
   */
  public boolean contains(final int xx, final int yy) {
    return (xx >= x && xx <= x + w || xx >= x + w && xx <= x) &&
      (yy >= y && yy <= y + h || yy >= y + h && yy <= y);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[x=" + x + ",y=" + y + ",h=" + h +
      ",w=" + w + ",h=" + h + ",pre=" + pre + ",level=" + level + ']';
  }
}
