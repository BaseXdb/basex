package org.basex.gui.view.map;

/**
 * Single Treemap Rectangle.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MapRect implements Cloneable {
  /** Rectangle pre value. */
  public int p;
  /** X position. */
  public int x;
  /** Y position. */
  public int y;
  /** Width. */
  public int w;
  /** Height. */
  public int h;
  /** Level. */
  int l;
  /** File Type. */
  int type = -1;
  /** Thumbnail view. */
  boolean thumb;
  
  /**
   * Simple rectangle constructor.
   * @param xx x position
   * @param yy y position
   * @param ww width
   * @param hh height
   */
  public MapRect(final int xx, final int yy, final int ww, final int hh) {
    x = xx;
    y = yy;
    w = ww;
    h = hh;
  }

  /**
   * Default rectangle constructor, specifying additional rectangle
   * attributes such as id, level and orientation.
   * @param xx x position
   * @param yy y position
   * @param ww width
   * @param hh height
   * @param i0 rectangle id
   * @param ll level
   */
  MapRect(final int xx, final int yy, final int ww, final int hh,
      final int i0, final int ll) {
    this(xx, yy, ww, hh);
    p = i0;
    l = ll;
  }
  
  /**
   * Verifies if the specified coordinates are inside the rectangle.
   * @param xx x position
   * @param yy y position
   * @return result of comparison
   */
  boolean contains(final int xx, final int yy) {
    return xx >= x && yy >= y && xx <= x + w && yy <= y + h;
  }

  /**
   * Verifies if the specified rectangle is contained inside the rectangle.
   * @param r rectangle
   * @return result of comparison
   */
  boolean contains(final MapRect r) {
    return r.x >= x && r.y >= y && r.x + r.w <= x + w && r.y + r.h <= y + h;
  }

  @Override
  protected MapRect clone() {
    try {
      return (MapRect) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }
  
  @Override
  public String toString() {
    return "Rect[x=" + x + ",y=" + y + ",h=" + h + ",w=" + w + ",h=" + h +
      ",id=" + p + ",level=" + l + ']';
  }
}
