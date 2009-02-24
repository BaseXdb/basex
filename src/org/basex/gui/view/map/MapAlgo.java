package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.gui.GUIProp;

/**
 * Interface MapAlgorithms need to implement.
 * 
 * @author joggele
 *
 */
public abstract class MapAlgo {
  /** Layout rectangle. */
  MapRect border;
  /** Font size. */
  final int o = GUIProp.fontsize + 4;
  
  /**
   * Constructor.
   */
  public MapAlgo() {
    switch(GUIProp.maplayout) {
      case 0: border = new MapRect(0, 0, 0, 0); break;
      case 1: border = new MapRect(1, 1, 2, 2); break;
      case 2: border = new MapRect(0, o, 0, o); break;
      case 3: border = new MapRect(2, o - 1, 4, o + 1); break;
      case 4: border = new MapRect(o >> 2, o, o >> 1, o + (o >> 2)); break;
      case 5: border = new MapRect(o >> 1, o, o, o + (o >> 1)); break;
      default:
    }
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