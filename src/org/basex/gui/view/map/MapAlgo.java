package org.basex.gui.view.map;

/**
 * Interface MapAlgorithms need to implement.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public abstract class MapAlgo {

  /**
   * Calculates the average aspect Ratios of rectangles given in the list.
   * @param r Array of rectangles
   * @return average aspect ratio
   */
  protected static double lineRatio(final MapRects r) {
    if(r.size == 0) return Double.MAX_VALUE;
    double ar = 0;
    int dev = 0;

    for(final MapRect rect : r) {
      if(rect.w != 0 && rect.h != 0) {
        dev++;
        if(rect.w > rect.h) {
          ar += rect.w / rect.h;
        } else {
          ar += rect.h / rect.w;
        }
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
   * @return ArrayList holding laid out rectangles
   */
  abstract MapRects calcMap(final MapRect r, final MapList l, final int ns,
      final int ne);

  /**
   * Returns the name of the algorithm used.
   * @return name of the alfo
   */
  abstract String getName();

  /*
   * Calculates the worst aspect ratio of the rectangles given in the list.
   * Should return the worst ar....
   * @param r Array of rectangles
   * @return max aspect ratio
  protected static double worstLineRatio(final MapRects r) {
    if(r.size() == 0) return Double.MAX_VALUE;

    double worstar = 0;
    double ar = 0;

    for(final MapRect rect : r) {
      if(rect.w != 0 && rect.h != 0) {
        if(rect.w > rect.h) {
          ar = rect.w / rect.h;
        } else {
          ar = rect.h / rect.w;
        }
      }
      if(ar > worstar) worstar = ar;
    }
    return worstar;
  }
   */
}
