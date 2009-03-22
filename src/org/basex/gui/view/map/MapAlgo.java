package org.basex.gui.view.map;

/**
 * Interface MapAlgorithms need to implement.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public abstract class MapAlgo {

  /**
   * Calculates the aspect Ratios of rectangles given in the List. Bigger 
   * rectangles have more influence to the result.
   * 
   * @param r Array of rectangles
   * @return average aspect ratio
   */
  static double lineRatio(final MapRects r) {
    if(r.size() == 0) return Double.MAX_VALUE;
    double ar = 0;
    double dev = 0;

    for(MapRect rect : r) {
      if(rect.w != 0 && rect.h != 0) {
        double d = rect.w * rect.h;
        dev += d;
        if(rect.w > rect.h) {
          ar += d * rect.w / rect.h;
        } else {
          ar += d * rect.h / rect.w;
        }
      }
    }
    return ar / dev;
  }

  /**
   * Splits List nodes into Rectangles matching in given space.
   * 
   * @param r parent rectangle
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   * @param level indicates level which is calculated
   * @return ArrayList holding laid out rectangles
   */
  abstract MapRects calcMap(final MapRect r, final MapList l, final int ns,
      final int ne, final int level);
}
