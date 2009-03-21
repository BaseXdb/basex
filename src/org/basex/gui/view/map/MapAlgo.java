package org.basex.gui.view.map;

/**
 * Interface MapAlgorithms need to implement.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
public abstract class MapAlgo {

  /**
   * Calculates the average aspect Ratios of rectangles given in the List.
   *
   * @param r Array of rectangles
   * @return average aspect ratio
   */
  static double lineRatio(final MapRects r) {
    if (r.size() == 0) return Double.MAX_VALUE;
    double ar = 0;

    for(int i = 0; i < r.size(); i++) {
      if (r.get(i).w != 0 && r.get(i).h != 0) {
        if (r.get(i).w > r.get(i).h) {
          ar += r.get(i).w / r.get(i).h;
        } else {
          ar += r.get(i).h / r.get(i).w;
        }
      }
    }
    return ar / r.size();
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
  abstract MapRects calcMap(final MapRect r, final MapList l, 
      final int ns, final int ne, final int level);
}