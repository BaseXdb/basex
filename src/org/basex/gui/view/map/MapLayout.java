package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewData;
import org.basex.gui.view.ViewRect;
import org.basex.util.IntList;
import org.basex.util.Token;

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

  /**
   * Calculates the average aspect Ratios of rectangles given in the List.
   *
   * @param r Array of rectangles
   * @return average aspect ratio
   */
  public static double lineRatio(final ArrayList<ViewRect> r) {
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
   * Computes average aspect ratio of a rectangle list as specified by 
   * Shneiderman (checks only leafnodes).
   * 
   * @param r arrraylist of rects
   * @param data Gui data reference
   * @return aar 
   */
  public static double aar(final ArrayList<ViewRect> r, final Data data) {
    double aar = 0;

    for(int i = 0; i < r.size(); i++) {
      ViewRect curr = r.get(i);
      if (ViewData.isLeaf(data, curr.pre) && curr.w != 0 && curr.h != 0) {
        if (curr.w > curr.h) {
          aar += curr.w / curr.h;
        } else {
          aar += curr.h / curr.w;
        }
      }
    }
    return aar / r.size();
  }
  
  /**
   * Sorts Rectangles of a given List.
   * 
   * @param nodes
   * @return sorted
   */

  /**
   * Calculates the average distance of two maplayouts using the euclidean 
   * distance of each rect in the first and the second rectlist.
   * 
   * [JH] how to handle rects available in one of the lists not included in 
   * the other one?
   *
   * @param first array of view rectangles
   * @param second array of view rectangles
   * @return average distance
   */
  public static double averageDistanceChange(final ArrayList<ViewRect> first, 
      final ArrayList<ViewRect> second) {
    double aDist = 0.0;
    int length = Math.min(first.size(), second.size());
    int x1, x2, y1, y2;

    for (int i = 0; i < length; i++) {
      if(first.get(i).pre == second.get(i).pre) {
        x1 = first.get(i).x + first.get(i).w >> 1;
        x2 = second.get(i).x + second.get(i).w >> 1;
        y1 = first.get(i).y + first.get(i).h >> 1;
        y2 = second.get(i).y + second.get(i).h >> 1;
        aDist += ((x1 - x2) ^ 2 + (y1 - y2) ^ 2) ^ (1 / 2);
      }
    }
    return aDist / length;
  }

  /**
   * Returns the number of rectangles painted.
   *
   * @param rectangles array of painted rects
   * @return nr of rects in the list
   */
  public int getRectNumber(final ArrayList<ViewRect> rectangles) {
    return rectangles.size();
  }

  /**
   * Returns all children of the specified node.
   * @param data data reference
   * @param par parent node
   * @return children
   */
  protected static IntList children(final Data data, final int par) {
    final IntList list = new IntList();

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
   * Calculates the percentual weight to use.
   * uses gui prop slider (size_p) to define size by any attributes (for now
   * use mixture of size and number of children) 
   * weight = size_p * size + (1 - size_p) * |childs| whereas size_p in [0;1]
   * 
   * [JH] should be possible to replace size and childs by any other
   * numerical attributes in future.
   * 
   * @param rect pre val of rect
   * @param par comparison rectangles
   * @param data Data reference
   * @return weight in context
   */
  public static double calcWeight(final int rect, final int par, 
      final Data data) {
    double weight;
    // get size of the node
    long size = Token.toLong(data.attValue(data.sizeID, rect));
    // parents size
    long sSize = Token.toLong(data.attValue(data.sizeID, par));
    // call weightening function
    weight = calcWeight((int) size, children(data, rect).size,
        (int) sSize, children(data, par).size);
    return weight;
  }
  
  /**
   * Computes weight with given values for each value using GUIprop.sizeP.
   * weight = sizeP * size + (1 - sizeP) * |childs| whereas sizeP in (0;100)
   * 
   * @param size one nodes size
   * @param childs one nodes number of childs
   * @param sSize compare to more nodes size
   * @param sChilds compare to more nodes number of childs
   * @return weight
   */
  public static double calcWeight(final int size, final int childs,
      final int sSize, final int sChilds) {
    return (GUIProp.sizeP * size / sSize) + 
    (100 - GUIProp.sizeP * childs / sChilds);
  }
  
  /**
   * Recursively splits rectangles.
   * @param data data reference
   * @param r parent rectangle
   * @param mainRects stores already layout rectangles
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   * @param level indicates level which is calculated
   */
  abstract void calcMap(final Data data, final ViewRect r,
      ArrayList<ViewRect> mainRects, final IntList l, final int ns,
      final int ne, final int level);
}
