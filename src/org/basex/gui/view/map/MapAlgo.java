package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * Interface MapAlgorithms need to implement.
 * 
 * @author joggele
 *
 */
public abstract class MapAlgo {

  /**
   * Calculates the average aspect Ratios of rectangles given in the List.
   *
   * @param r Array of rectangles
   * @return average aspect ratio
   */
  static double lineRatio(final ArrayList<MapRect> r) {
    if (r.isEmpty()) return Double.MAX_VALUE;
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
   * Recursively splits rectangles.
   * @param r parent rectangle
   * @param l children array
   * @param w weights array
   * @param ns start array position
   * @param ne end array position
   * @param level indicates level which is calculated
   * @return ArrayList holding layedout rectangles
   */
  abstract ArrayList<MapRect> calcMap(final MapRect r, final MapList l, 
      final double[] w, final int ns, final int ne, final int level);
  
  /**
   * Find out which kind of Maplayout is used right now.
   * @return name Of Layoutalgorithm
   */
  abstract String getType();
}