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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
class MapLayout {
  /** Font size. */
  private final int off = GUIProp.fontsize + 4;
  /** Data reference. */
  private final Data data;
  /** Map algorithm to use in this layout. */
  protected final MapAlgo algo;
  /** Text lengths. */
  private int[] textLen;

  /** List of rectangles. */
  final ArrayList<MapRect> rectangles;
  /** Layout rectangle. */
  final MapRect layout;

  /**
   * Constructor.
   * @param d data reference to use in this maplayout
   * @param tl text lengths array
   */
  MapLayout(final Data d, final int[] tl) {
    data = d;
    textLen = tl;
    rectangles = new ArrayList<MapRect>();
    
    switch(GUIProp.mapoffsets) {
      case 1 :
        layout = new MapRect(1, 1, 2, 2); break;
      case 2 :
        layout = new MapRect(0, off, 0, off); break;
      case 3 :
        layout = new MapRect(2, off - 1, 4, off + 1); break;
      case 4 :
        layout = new MapRect(off >> 2, off, off >> 1, off + (off >> 2)); break;
      case 5 :
        layout = new MapRect(off >> 1, off, off, off + (off >> 1)); break;
      default:
        layout = new MapRect(0, 0, 0, 0); break;
    }
    
    switch(GUIProp.mapalgo) {
      // select method to construct this treemap
      // may should be placed in makeMap to define different method for 
      // different levels
      case 1 : algo = new StripAlgo(); break;
      case 2 : algo = new SquarifiedAlgo(); break;
      case 3 : algo = new SliceDiceAlgo(); break;
      default: algo = new SplitAlgo(); break;
    }
    
//    if (data.fs == null && GUIProp.usetextlength) initLen();
  }

  /**
   * Calculates the average aspect Ratios of rectangles given in the List.
   *
   * @param r Array of rectangles
   * @return average aspect ratio
  private static double lineRatio(final ArrayList<MapRect> r) {
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
   */

  /**
   * Computes average aspect ratio of a rectangle list. 
   * note: as specified by Shneiderman only leafnodes should be checked
   * 
   * [JH] why not weight the bigger nodes more than smaller ones???
   * 
   * @param r arrray list of rects
   * @return aar 
   */
  static double aar(final ArrayList<MapRect> r) {
    double aar = 0;
    int nrLeaves = 0;
    for(int i = 0; i < r.size(); i++) {
      final MapRect curr = r.get(i);
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
  private static double averageDistanceChange(final ArrayList<MapRect> first, 
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
   */

  /*
   * Returns the number of rectangles painted.
   *
   * @param r array of painted rects
   * @return nr of rects in the list
  private int getNumberRects(final ArrayList<MapRect> r) {
    return r.size();
  }
   */

  /**
   * Returns all children of the specified node.
   * @param par parent node
   * @return children
   */
  private MapList children(final int par) {
    final MapList list = new MapList();

    final int kind = data.kind(par);
    final int last = par + data.size(par, kind);
    final boolean atts = GUIProp.mapatts && data.fs == null;
    int p = par + (atts ? 1 : data.attSize(par, kind));
    while(p < last) {
      list.add(p);
      p += data.size(p, data.kind(p));
    }

    // paint all children
//    if(list.size != 0) list.add(p);
    return list;
  }
  
  /**
   * Adds all the sizes attribute of the nodes in the given list.
   * @param l list of nodes
   * @return sum of the size attribute
   */
  private long addSizes(final IntList l) {
    long sum = 0;
    for (int i = 0; i < l.size; i++) {
      final byte[] val = data.attValue(data.sizeID, l.list[i]);
      if(val != null) sum += Token.toLong(val);
    }
    return sum;
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
    if(ne - ns == 0) {
      // one rectangle left, add it and go deeper
      r.pre = l.list[ns];
      putRect(r, level);  
    } else {
      final long parsize = data.fs != null ? addSizes(l) : 0;
      int nn;
      ArrayList<MapRect> rects;
      if(level == 0) {
        // [JH] may first level layout should not be defined to splitalgo
        nn = 0;
        for (int i = 0; i < l.size; i++) {
          nn += data.size(l.list[i], data.kind(l.list[i]));
        }
        if(GUIProp.mapsimple) {
          l.initWeights();
        } else if(GUIProp.usetextlength && data.fs == null) {
          l.initWeights(textLen, nn, data);
        } else l.initWeights(parsize, nn, data);
        
        final MapAlgo tmp = new SplitAlgo();
        rects = tmp.calcMap(r, l, l.weight, 0, l.size - 1, level);
      } else {
        nn = l.list[ne] - l.list[ns] + 
            data.size(l.list[ne], data.kind(l.list[ne]));
        
        // init weights of nodes
        if(GUIProp.mapsimple) {
          l.initWeights();
        } else if(GUIProp.usetextlength && data.fs == null) {
          l.initWeights(textLen, nn, data);
        } else l.initWeights(parsize, nn, data);
        
        rects = algo.calcMap(r, l, l.weight, ns, ne, level);
      }
      // call recursion for next deeper levels
      for(final MapRect rect : rects) putRect(rect, rect.level);
    }
  }
  
  /**
   * One rectangle left, add it and continue with its children.
   * @param r parent rectangle
   * @param level indicates level which is calculated
   */
  private void putRect(final MapRect r, final int level) {
    rectangles.add(r);

    // position, with and height calculated using sizes of former level
    final int x = r.x + layout.x;
    final int y = r.y + layout.y;
    final int w = r.w - layout.w;
    final int h = r.h - layout.h;
    
    // skip too small rectangles and meta data in file systems
    if((w < off && h < off) || w < 1 || h < 1 || GUIProp.mapfs && 
        ViewData.isLeaf(data, r.pre)) return;

    final MapList ch = children(r.pre);
    if(ch.size != 0) makeMap(new MapRect(x, y, w, h, r.pre, r.level + 1),
        ch, 0, ch.size - 1, level + 1);
  }
  
//  /**
//   * Initializes the text lengths and stores them into an array.
//   */
//  private void initLen() {
//    int size = data.meta.size;
//    textLen = new int[size];
//
//    final int[] parStack = new int[IO.MAXHEIGHT];
//    int l = 0;
//    int par = 0;
//
//    for(int pre = 0; pre < size; pre++) {
//      final int kind = data.kind(pre);
//      par = data.parent(pre, kind);
//      
//      int ll = l;
//      while(l > 0 && parStack[l - 1] > par) {
//        textLen[parStack[l - 1]] += textLen[parStack[l]];
//        --l;
//      }
//      if(l > 0 && ll != l) textLen[parStack[l - 1]] += textLen[parStack[l]];
//
//      parStack[l] = pre;
//
//      if(kind == Data.TEXT || kind == Data.COMM || kind == Data.PI) {
//        textLen[pre] = data.textLen(pre);
//      } else if(kind == Data.ATTR) {
//        textLen[pre] = data.attLen(pre);
//      } else if(kind == Data.ELEM || kind == Data.DOC) {
//        l++;
//      } 
//    }
//    while(--l >= 0) textLen[parStack[l]] += textLen[parStack[l + 1]];
//  }
}