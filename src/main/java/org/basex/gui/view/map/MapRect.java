package org.basex.gui.view.map;

import org.basex.data.FTPos;
import org.basex.gui.view.ViewRect;

/**
 * View rectangle.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class MapRect extends ViewRect implements Comparable<MapRect> {
  /** Thumbnail view. */
  boolean thumb;
  /** Full-text position values. */
  FTPos pos;
  /** Abstraction level for thumbnail. */
  byte thumbal;
  /** Height of a thumbnail unit. */
  byte thumbfh;
  /** Height of an empty line. */
  byte thumblh;
  /** Width of a thumbnail unit. */
  double thumbf;
  /** Width of a space between two thumbnails. */
  double thumbsw;

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
   */
  MapRect(final ViewRect r, final int p) {
    this(r.x, r.y, r.w, r.h, p, r.level);
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
  public boolean equals(final Object o) {
    return o instanceof MapRect && ((MapRect) o).pre == pre;
  }

  @Override
  public int compareTo(final MapRect r) {
    return pre - r.pre;
  }

  @Override
  public int hashCode() {
    return pre;
  }
}
