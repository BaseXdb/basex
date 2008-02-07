package org.basex.gui.view.map;

import org.basex.util.Array;

/**
 * Rectangle Container.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
class MapRects {
  /** Rectangle Array. */
  MapRect[] rect = new MapRect[8];
  /** Number of stored rectangles. */
  int size;
  
  /**
   * Adds a rectangle.
   * @param r rectangle to be added.
   */
  void add(final MapRect r) {
    if(size == rect.length) rect = Array.extend(rect);
    rect[size++] = r;
  }
  
  /**
   * Returns the specified rectangle.
   * @param i rectangle index
   * @return rectangle
   */
  MapRect get(final int i) {
    return rect[i];
  }
  
  @Override
  public String toString() {
    return "Rects[size=" + size + ']';
  }
}
