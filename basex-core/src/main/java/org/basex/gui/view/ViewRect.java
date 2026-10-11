package org.basex.gui.view;

import org.basex.util.*;

/**
 * View rectangle.
 *
 * @author BaseX Team, BSD License
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
  /** Rectangle PRE value. */
  public int pre;
  /** Level. */
  public int level;

  /**
   * Default constructor.
   */
  public ViewRect() { }

  /**
   * Simple rectangle constructor.
   * @param x x position
   * @param y y position
   * @param w width
   * @param h height
   */
  protected ViewRect(final int x, final int y, final int w, final int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @param yy y position
   * @return result of check
   */
  public final boolean contains(final int xx, final int yy) {
    return (xx >= x && xx <= x + w || xx >= x + w && xx <= x) &&
      (yy >= y && yy <= y + h || yy >= y + h && yy <= y);
  }

  /**
   * Returns a copy of this rectangle with non-negative width and height.
   * @return normalized rectangle
   */
  public final ViewRect normalize() {
    return new ViewRect(w < 0 ? x + w : x, h < 0 ? y + h : y, Math.abs(w), Math.abs(h));
  }

  @Override
  public final String toString() {
    return Util.className(this) + "[x=" + x + ",y=" + y + ",w=" + w + ",h=" + h +
      ",pre=" + pre + ",level=" + level + ']';
  }
}
