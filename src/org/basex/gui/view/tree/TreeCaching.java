package org.basex.gui.view.tree;

import java.awt.Graphics;
import java.util.ArrayList;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class determines nodes per level and caches them.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
final class TreeCaching implements TreeViewOptions {
  /** document depth. */
  int maxLevel = -1;
  /** All nodes document nodes per level. */
  private ArrayList<int[]> nodesPerLevel;
  /** Rectangles per level. */
  private ArrayList<RectangleCache[]> rectsPerLevel;
  /** Lists per level if big rectangle or not. */
  private boolean[] bigRectangle;

  /**
   * This constructor invokes methods to cache all document nodes.
   * @param data data reference
   */
  TreeCaching(final Data data) {
    if(data == null) return;
    maxLevel = data.meta.height + 1;
    cacheNodes(data);
  }

  /**
   * Determines nodes in each level and caches them.
   * @param data data reference
   */
  private void cacheNodes(final Data data) {

    nodesPerLevel = null;
    final ArrayList<int[]> nL = new ArrayList<int[]>();

    int[] parentList = { 0};
    int level = 0;

    while(maxLevel > level++) {
      nL.add(parentList);
      parentList = getNextNodeLine(parentList, data);
    }
    nL.trimToSize();
    nodesPerLevel = nL;
  }

  /**
   * Saves node line in parentList.
   * @param parentList array with nodes of the line before
   * @param data the data reference
   * @return array filled with nodes of the current line
   */
  private int[] getNextNodeLine(final int[] parentList, final Data data) {
    final int l = parentList.length;
    final IntList temp = new IntList();

    for(int i = 0; i < l; i++) {

      final int p = parentList[i];
      final ChildIterator iterator = new ChildIterator(data, p);

      while(iterator.more()) {
        final int pre = iterator.next();

        if(data.kind(pre) == Data.ELEM || !ONLY_ELEMENT_NODES) {
          temp.add(pre);
        }
      }
    }
    return temp.finish();
  }

//  /**
//   * @param data data reference
//   * @param pre
//   */
//  private int[][] generateLevelBorders(final Data data, final int pre) {
//
//    final int[] rlp = findPre(pre);
//    final int rl = rlp[0];
//    final int ri = rlp[1];
//
//    int np = nodesPerLevel.get(rl).length > ri + 1 ?
//  nodesPerLevel.get(rl)[ri + 1]
//        : -1;
//
//    // level pair
//    int[][] lp = new int[maxLevel][2];
//    lp[rl] = new int[] { ri, 1};
//
//    for(int i = rl + 1; i < maxLevel; i++) {
//
//      int idx = -1;
//      int j = -1;
//      int[] n = nodesPerLevel.get(i);
//      while(n[++j] < pre)
//        ;
//      idx = j;
//
//      if(np == -1) {
//        lp[i] = new int[] { idx, n.length - idx};
//
//      } else {
//
//        while(j < n.length && n[j++] < np)
//          ;
//
//        lp[i] = new int[] { idx, j - 1};
//
//      }
//
//      np = n.length > j + 1 ? n[j + 1] : -1;
//
//    }
//    return lp;
//  }

  /**
   * Generates cached rectangles.
   * @param g graphics reference
   * @param c database context
   * @param sW screen width
   */
  void generateRects(final Graphics g, final Context c, final int sW) {

    final Data d = c.data;
    final int[] roots = c.current.nodes;
    final int rc = roots.length;
    final double width = sW / (double) rc;

    for(int r = 0; r < rc; r++) {

      final int root = roots[r];

      if(root > 0) {
//        generateLevelBorders(d, root);
      }

      rectsPerLevel = new ArrayList<RectangleCache[]>();
      bigRectangle = new boolean[maxLevel];

      for(int i = 0; i < maxLevel; i++) {
        final int[] currLine = nodesPerLevel.get(i);
        final int lS = currLine.length;
        final double w = width / lS;

        if(w < 2) {
          bigRectangle[i] = true;
          rectsPerLevel.add(bigRectangle(width));
        } else {
          bigRectangle[i] = false;
          rectsPerLevel.add(normalRectangle(g, d, i, w, lS));
        }
      }
      rectsPerLevel.trimToSize();
    }
  }

  /**
   * Invoked if not enough space for more than one big rectangle.
   * @param w the width
   * @return big rectangle
   */
  private RectangleCache[] bigRectangle(final double w) {
    final RectangleCache bigRect = new RectangleCache(0, (int) w);
    final RectangleCache[] rL = { bigRect};
    return rL;
  }

  /**
   * Creates normal rectangles.
   * @param g graphics reference
   * @param d data reference
   * @param l level
   * @param w width
   * @param s size
   * @return rectangles
   */
  private RectangleCache[] normalRectangle(final Graphics g, final Data d,
      final int l, final double w, final int s) {

    double xx = 0;
    double ww = w;

    // new array, to be filled with the rectangles of the current level
    final RectangleCache[] rects = new RectangleCache[s];

    for(int i = 0; i < s; i++) {

      final double boxMiddle = xx + ww / 2f;

      if(SLIM_TO_TEXT) {
        final String st = getText(d, getPrePerLevelAndIndex(l, i));
        int o = calcOptimalRectWidth(g, st) + 10;
        if(o < MIN_SPACE) o = MIN_SPACE;

        if(w > o) {
          xx = boxMiddle - o / 2d;
          ww = o;
        }
      }

      final RectangleCache rect = new RectangleCache((int) xx, (int) ww);

      rects[i] = rect;

      xx += w;
    }

    return rects;
  }

  /**
   * Returns TreeRects in given level.
   * @param l level
   * @return TreeRect array
   */
  TreeRect[] getTreeRectsPerLevel(final int l) {
    final int s = isBigRectangle(l) ? 1 : getSizePerLevel(l);
    final TreeRect[] tr = new TreeRect[s];

    for(int i = 0; i < s; i++) {
      tr[i] = getTreeRect(l, i);
    }
    return tr;
  }

  /**
   * Finds pre value in cached nodes and returns level and index position.
   * @param pre pre value
   * @return level and position pair
  private int[] findPre(final int pre) {

    int pos = -1;
    int l;

    for(l = 0; l < maxLevel; l++) {

      pos = searchPreArrayPosition(l, pre);

      if(pos > -1) break;
    }

    return pos > -1 ? new int[] { l, pos} : null;
  }
   */

  /**
   * Determines the index position of given pre value.
   * @param lv level to be searched
   * @param pre the pre value
   * @return the determined index position
   */
  int searchPreArrayPosition(final int lv, final int pre) {

    final int[] a = nodesPerLevel.get(lv);
    int index = -1;
    int l = 0;
    int r = a.length - 1;

    while(r >= l && index == -1) {
      final int m = l + (r - l) / 2;

      if(a[m] < pre) {
        l = m + 1;
      } else if(a[m] > pre) {
        r = m - 1;
      } else {
        index = m;
      }
    }
    return index;
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param lv level
   * @param pre the pre value to be found
   * @return the rectangle containing the given pre value, null else
   */
  TreeRect searchRect(final int lv, final int pre) {

    final int[] pres = nodesPerLevel.get(lv);
    TreeRect rect = null;
    int l = 0;
    int r = pres.length - 1;
    int m = -1;

    while(r >= l && rect == null) {
      m = l + (r - l) / 2;

      if(pres[m] < pre) {
        l = m + 1;
      } else if(pres[m] > pre) {
        r = m - 1;
      } else {
        rect = getTreeRect(lv, m);
      }
    }
    return rect;
  }

  /**
   * Returns size of given level.
   * @param l level
   * @return size
   */
  int getSizePerLevel(final int l) {
    return nodesPerLevel.get(l).length;
  }

  /**
   * Returns pre value at given level and index.
   * @param l level
   * @param i index
   * @return pre value
   */
  int getPrePerLevelAndIndex(final int l, final int i) {
    return nodesPerLevel.get(l)[i];
  }

  /**
   * Extends CachedRect to TreeRect.
   * @param l level
   * @param i index
   * @return TreeRect
   */
  private TreeRect getTreeRect(final int l, final int i) {
    final TreeRect r = new TreeRect(rectsPerLevel.get(l)[i]);
    r.pre = isBigRectangle(l) ? -1 : nodesPerLevel.get(l)[i];
    return r;
  }

  /**
   * returns the node text.
   * @param data data reference
   * @param pre the pre value
   * @return String with node text
   */
  String getText(final Data data, final int pre) {
    final int k = data.kind(pre);
    final TokenBuilder tb = new TokenBuilder();

    if(data.meta.deepfs) {
      if(data.fs.isFile(pre)) tb.add(data.fs.name(pre));
      else tb.add(data.text(pre + 1, false));
    } else {
      if(k == Data.ELEM) {
        tb.add(data.name(pre, k));
      } else {
        tb.add(data.text(pre, true));
      }
    }
    return tb.toString();
  }

  /**
   * Returns true if big rectangle, false else.
   * @param l level
   * @return boolean
   */
  boolean isBigRectangle(final int l) {
    return bigRectangle[l];
  }

  /**
   * Calculates optimal rectangle width.
   * @param g the graphics reference
   * @param s given string
   * @return optimal rectangle width
   */
  private int calcOptimalRectWidth(final Graphics g, final String s) {
    return BaseXLayout.width(g, s);
  }

  /**
   * This is class is used to cache rectangles.
   * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
   * @author Wolfgang Miller
   */
  private static class RectangleCache {
    /** X position. */
    final int x;
    /** Width. */
    final int w;

    /**
     * Saves width and x position.
     * @param xx x position
     * @param ww width
     */
    public RectangleCache(final int xx, final int ww) {
      w = ww;
      x = xx;
    }
  }

  /**
   * This is class is used to handle rectangles.
   * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
   * @author Wolfgang Miller
   */
  static class TreeRect extends RectangleCache {
    /** The pre value. */
    int pre;

    /**
     * Initializes TreeRect.
     * @param xx x position
     * @param ww width
     */
    TreeRect(final int xx, final int ww) {
      super(xx, ww);
    }

    /**
     * Initializes TreeRect.
     * @param c RectangleCache
     */
    TreeRect(final RectangleCache c) {
      super(c.x, c.w);
    }

    /**
     * Verifies if the specified coordinates are inside the rectangle.
     * @param xx x position
     * @return result of comparison
     */
    boolean contains(final int xx) {
      return xx >= x && xx <= x + w || xx >= x + w && xx <= x;
    }
  }
}
