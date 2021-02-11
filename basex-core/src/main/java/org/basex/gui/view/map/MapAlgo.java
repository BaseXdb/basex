package org.basex.gui.view.map;

/**
 * Interface MapAlgorithms need to implement.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Joerg Hauser
 */
abstract class MapAlgo {
  /**
   * Calculates the average aspect Ratios of rectangles given in the list.
   * @param r Array of rectangles
   * @return average aspect ratio
   */
  static double lineRatio(final MapRects r) {
    if(r.size == 0) return Double.MAX_VALUE;
    double ar = 0;
    int dev = 0;

    for(final MapRect rect : r) {
      if(rect.w != 0 && rect.h != 0) {
        ++dev;
        ar += rect.w > rect.h ? (double) rect.w / rect.h : (double) rect.h / rect.w;
      }
    }
    return ar / dev;
  }

  /**
   * Splits List nodes into Rectangles matching in given space.
   * @param r parent rectangle
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   * @return rectangles
   */
  abstract MapRects calcMap(MapRect r, MapList l, int ns, int ne);
}
