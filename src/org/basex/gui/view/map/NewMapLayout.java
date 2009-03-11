package org.basex.gui.view.map;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewData;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Defines shared things of TreeMap Layout Algorithms.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
class NewMapLayout {
  /** Layout rectangle. */
  MapRect layout;
  /** Font size. */
  final int o = GUIProp.fontsize + 4;
  /** List of rectangles. */
  ArrayList<MapRect> rectangles;
  /** data reference. */
  static Data data;
  /** list of nodes. */
  MapList list;
  /** mapalgo to use in this layout. */
  MapAlgo algo;
  
  /**
   * Constructor.
   * @param d data reference to use in this maplayout
   */
  NewMapLayout(final Data d) {
    data = d;
    rectangles = new ArrayList<MapRect>();
    
    switch(GUIProp.mapoffsets) {
      case 0: layout = new MapRect(0, 0, 0, 0); break;
      case 1: layout = new MapRect(1, 1, 2, 2); break;
      case 2: layout = new MapRect(0, o, 0, o); break;
      case 3: layout = new MapRect(2, o - 1, 4, o + 1); break;
      case 4: layout = new MapRect(o >> 2, o, o >> 1, o + (o >> 2)); break;
      case 5: layout = new MapRect(o >> 1, o, o, o + (o >> 1)); break;
      default:
    }
    
    switch(GUIProp.mapalgo) {
      // select method to construct this treemap
      // may should be placed in makeMap to define different method for 
      // different levels
      case 0:
        algo = new SplitAlgo();
        break;
      case 1:
        algo = new SliceDiceAlgo();
        break;
      case 2:
        algo = new SquarifiedAlgo();
        break;
      case 3:
        algo = new StripAlgo();
        break;
      default:
        algo = new SplitAlgo();
        break;
    }
  }

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
   * Computes average aspect ratio of a rectangle list. 
   * [JH] as specified by Shneiderman only leafnodes should be checked
   * [JH] why not more weighten the bigger nodes???
   * 
   * @param r arrray list of rects
   * @return aar 
   */
  static double aar(final ArrayList<MapRect> r) {
    double aar = 0;
    int nrLeaves = 0;
    for(int i = 0; i < r.size(); i++) {
      MapRect curr = r.get(i);
      // Shneiderman would use this: children(data, curr.pre).size == 0 && 
      if (curr.w != 0 && curr.h != 0) {
        nrLeaves++;
        if (curr.w > curr.h) {
          aar += curr.w / curr.h;
        } else {
          aar += curr.h / curr.w;
        }
      }
    }
    return nrLeaves > 0 ? aar / nrLeaves : -1;
  }

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
  static double averageDistanceChange(final ArrayList<MapRect> first, 
      final ArrayList<MapRect> second) {
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
   * @param r array of painted rects
   * @return nr of rects in the list
   */
  int getNumberRects(final ArrayList<MapRect> r) {
    return r.size();
  }

  /**
   * Returns all children of the specified node.
   * @param par parent node
   * @return children
   */
  private static MapList children(final int par) {
    final MapList list = new MapList();

    final int kind = data.kind(par);
    final int last = par + data.size(par, kind);
    final boolean atts = GUIProp.mapatts && data.fs == null;
    int p = par + (atts ? 1 : data.attSize(par, kind));
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
   * weight = size_p * size + (1 - size_p) * |children| whereas size_p in [0;1]
   * 
   * [JH] should be possible to replace size and children by any other
   * numerical attributes in future.
   * [JH] add some more to do this leaving sizes or number of childs if slider
   * is in possition to do so
   * 
   * @param rect pre val of rect
   * @param par comparison rectangles
   * @return weight in context
   */
  static double calcWeight(final int rect, final int par) {
    double weight;
    // get size of the node
    long size = Token.toLong(data.attValue(data.sizeID, rect));
    // parents size
    long sSize = Token.toLong(data.attValue(data.sizeID, par));
    // call weightening function
    weight = calcWeight(size, children(rect).size, 
        sSize, children(par).size);
    return weight;
  }
  
  /**
   * Computes weight with given values for each value using GUIprop.sizep.
   * weight = sizep/100 * size + (1 - sizep/100) * |children|
   * whereas sizep in (0;100)
   * 
   * @param size one nodes size
   * @param children one nodes number of children
   * @param sSize compare to more nodes size
   * @param sChildren compare to more nodes number of children
   * @return weight
   */
  static double calcWeight(final long size, final int children,
      final long sSize, final int sChildren) {
    // if its not a filesystem, set sliderval for calc only to nr of children
    double sizeP = data.fs != null ? (double) GUIProp.sizep : 0d;
    if (sSize == 0) sizeP = 0d;
    long dadSize = (size == 0 && sSize == 0) ? 1 : sSize;
    
    return ((sizeP / 100) * ((double) size / dadSize)) + 
      ((1 - sizeP / 100) * ((double) children / sChildren));
  }
  
  /**
   * Adds all the sizes attribute of the nodes in the given list.
   * @param l list of nodes
   * @param start at element
   * @param end here
   * @return sum of the size attribute
   */
  static long addSizes(final IntList l, final int start, final int end) {
    long sum = 0;
    for (int i = start; i < end; i++) {
      sum += Token.toLong(data.attValue(data.sizeID, l.list[i]));
    }
    return sum;
  }
  
  /**
   * Splits and adds rectangles uniformly distributed.
   * @param r rectangles to lay out in
   * @param l list of rectangles to lay out
   * @param ns starting point
   * @param ne ending
   * @return rectangles uniformly devided
   */
  protected ArrayList<MapRect> splitUniformly(final MapRect r, final MapList l,
      final int ns, final int ne) {
    if (ne - ns == 1) {
      ArrayList<MapRect> rects = new ArrayList<MapRect>();
      rects.add(new MapRect(r, l.list[ns], 0));
      return rects;
    } else {
      ArrayList<MapRect> rects = new ArrayList<MapRect>();
      long nn, ln;
      int ni;
      // number of nodes used to calculate space
      nn = ne - ns;
      // nn / 2, pretends to be the middle of the handled list
      // except if starting point in the list is not at position 0
      ln = nn >> 1;
      // pivot with integrated list start
      ni = (int) (ns + ln);
      
      boolean v = r.w > r.h;
      
      int xx = r.x;
      int yy = r.y;
      int ww = !v ? r.w : (int) (r.w * ln / nn);
      int hh = v ? r.h : (int) (r.h * ln / nn);
  
      // paint both rectangles if enough space is left
      if(ww > 0 && hh > 0) rects.addAll(
          splitUniformly(new MapRect(xx, yy, ww, hh, 0, r.level), l, ns, ni));
      if(v) {
        xx += ww;
        ww = r.w - ww;
      } else {
        yy += hh;
        hh = r.h - hh;
      }
      if(ww > 0 && hh > 0) rects.addAll(
          splitUniformly(new MapRect(xx, yy, ww, hh, 0, r.level), l, ni, ne));
      return rects;
    }
  }


  /**
   * Recursively splits rectangles.
   * @param r parent rectangle
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   * @param level indicates level which is calculated
   */
  void makeMap(final MapRect r, final MapList l, final int ns, final int ne, 
      final int level) {
    if(ne - ns <= 1) {
      // one rectangle left, add it and go deeper
      r.pre = l.list[ns];
      putRect(r, level);
    } else {
      ArrayList<MapRect> rects;
      if(level == 0) {
        rects = splitUniformly(r, l, ns, ne);
      } else {
        int nn = l.list[ne] - l.list[ns];
        long parsize = data.fs != null ? addSizes(l, ns, ne) : 0;
        // init weights of nodes and sort
        l.initWeights(parsize, nn, data);
        rects = algo.calcMap(r, l, l.weights, ns, ne, level);
      }
      // call recursion for next deeper levels
      for(MapRect rect : rects) putRect(rect, rect.level);
    }
  }
  
  /**
   * One rectangle left, add it and continue with its children.
   * @param r parent rectangle
   * @param level indicates level which is calculated
   */
  protected void putRect(final MapRect r, final int level) {
    
    rectangles.add(r);

    // position, with and height calculated using sizes of former level
    final int x = r.x + layout.x;
    final int y = r.y + layout.y;
    final int w = r.w - layout.w;
    final int h = r.h - layout.h;

    // skip too small rectangles and leaf nodes (= meta data in deepfs)
    if((w >= o || h >= o) && w > 0 && h > 0 &&
        !ViewData.isLeaf(data, r.pre)) {
      final MapList ch = children(r.pre);

      if(ch.size != 0) makeMap(new MapRect(x, y, w, h, r.pre, r.level + 1),
          ch, 0, ch.size - 1, level + 1);
    }
  }
}