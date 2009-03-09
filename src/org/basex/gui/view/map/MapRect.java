package org.basex.gui.view.map;

import org.basex.gui.view.ViewRect;

/**
 * View Rectangle.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
class MapRect extends ViewRect implements Cloneable {
  /** File Type. */
  short type = -1;
  /** Thumbnail view. */
  boolean thumb;
  /** Fulltext position values. */
  int[] pos;
  /** Fulltext pointer values. */
  int[] poi;
  /** Color for and combinations. */
  byte[][] acol;
  /** Abstraction level for thumbnail. */
  byte thumbal;
  /** Height of a thumbnail unit.*/
  byte thumbfh;
  /** Height of an empty line.*/ 
  byte thumblh;
  /** Width of a thumbnail unit. */
  double thumbf;

  /**
   * Default constructor.
   */
  MapRect() {
  }
  
  /**
   * Simple rectangle constructor.
   * @param xx x position
   * @param yy y position
   * @param ww width
   * @param hh height
   */
  MapRect(final int xx, final int yy, final int ww, final int hh) {
    super(xx, yy, ww, hh);
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
  MapRect(final int xx, final int yy, final int ww, final int hh,
      final int p, final int l) {
    this(xx, yy, ww, hh);
    pre = p;
    level = l;
  }
  
  /**
   * Constructor taking MapRect for position initialization.
   * @param r pos and dimension rectangle
   * @param p rectangle pre value
   * @param l level
   */
  MapRect(final ViewRect r, final int p, final int l) {
    this(r.x, r.y, r.w, r.h);
    pre = p;
    level = l;
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
  public MapRect clone() {
    try {
      return (MapRect) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }
}
