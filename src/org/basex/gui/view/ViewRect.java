package org.basex.gui.view;

/**
 * View Rectangle.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class ViewRect implements Cloneable {
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
  /** File Type. */
  public int type = -1;
  /** Thumbnail view. */
  public boolean thumb;
  /** Fulltext position values. */
  public int[] pos;
  /** Fulltext pointer values. */
  public int[] poi;
  /** Abstraction level for thumbnail. */
  public int thumbal;
  /** Hight of a thumbnail uni.*/
  public int thumbfh;
  /** Hight of an empty line.*/ 
  public int thumblh;
  /** Width of a thumbnail unit. */
  public double thumbf;
  /**
   * Default constructor.
   */
  public ViewRect() {
  }
  
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
   * Default rectangle constructor, specifying additional rectangle
   * attributes such as id, level and orientation.
   * @param xx x position
   * @param yy y position
   * @param ww width
   * @param hh height
   * @param p rectangle pre value
   * @param l level
   */
  public ViewRect(final int xx, final int yy, final int ww, final int hh,
      final int p, final int l) {
    this(xx, yy, ww, hh);
    pre = p;
    level = l;
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

  /**
   * Verifies if the specified rectangle is contained inside the rectangle.
   * @param r rectangle
   * @return result of comparison
   */
  public boolean contains(final ViewRect r) {
    return r.x >= x && r.y >= y && r.x + r.w <= x + w && r.y + r.h <= y + h;
  }

  @Override
  public ViewRect clone() {
    try {
      return (ViewRect) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }
  
  @Override
  public String toString() {
    return "ViewRect[x=" + x + ",y=" + y + ",h=" + h + ",w=" + w +
      ",h=" + h + ",pre=" + pre + ",level=" + level + ']';
  }
}
