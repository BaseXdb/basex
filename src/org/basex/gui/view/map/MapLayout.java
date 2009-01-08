package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewRect;
import org.basex.util.IntList;

/**
 * Defines shared things of TreeMap Layout Algorithms.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
abstract class MapLayout {
  /** Layout rectangle. */
  ViewRect layout;
  /** Font size. */
  final int o = GUIProp.fontsize + 4;
  
  /**
   * Constructor.
   */
  MapLayout() {
    switch(GUIProp.maplayout) {
      case 0: layout = new ViewRect(0, 0, 0, 0); break;
      case 1: layout = new ViewRect(1, 1, 2, 2); break;
      case 2: layout = new ViewRect(0, o, 0, o); break;
      case 3: layout = new ViewRect(2, o - 1, 4, o + 1); break;
      case 4: layout = new ViewRect(o >> 2, o, o >> 1, o + (o >> 2)); break;
      case 5: layout = new ViewRect(o >> 1, o, o, o + (o >> 1)); break;
      default:
    }
  }

  /*
   * Calculates the average aspect Ratios of rectangles given in the List.
   * 
   * @param rects Array of rectangles
   * @return average aspect ratio
  double aspectRatio(final ArrayList<ViewRect> rects) {
    double aar = 0.0;
    for(int i = 0; i < rects.size(); i++) {
      aar += rects.get(i).x < rects.get(i).y ? rects.get(i).y / rects.get(i).x :
        rects.get(i).x / rects.get(i).y;
    }
    return aar / rects.size();
  }
   */
  
  /**
   * Returns all children of the specified node.
   * @param par parent node
   * @return children
   */
  protected IntList children(final int par) {
    final IntList list = new IntList();
    final Data data = GUI.context.data();

    final int kind = data.kind(par);
    final int last = par + data.size(par, kind);
    int p = par + (GUIProp.mapatts ? 1 : data.attSize(par, kind));
    while(p != last) {
      list.add(p);
      p += data.size(p, data.kind(p));
    }

    // paint all children
    if(list.size != 0) list.add(p);
    return list;
  }
  
  /**
   * Recursively splits rectangles.
   * 
   * @param r parent rectangle
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   * @param level indicates level which is calculated
   * @param mainRects stores already layouted rects
   */
  abstract void calcMap(final ViewRect r, ArrayList<ViewRect> mainRects,
      final IntList l, final int ns, final int ne, final int level);
  
}
