package org.basex.gui.view.tree;

import java.awt.Graphics;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;
import org.basex.util.IntList;

/**
 * This class determines nodes per level and caches them.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
public class TreeCaching implements TreeViewOptions {
  /** Document depth. */
  public int maxLevel = -1;
  /** All nodes document nodes per level. */
  private IntList[] nodes;
  /** Saved rectangles. */
  public TreeRect[][][] rects;
  /** Subtree borders. */
  private TreeBorder[][] border;
  /** Window properties. */
  private GUIProp prop;

  /**
   * This constructor invokes methods to cache all document nodes.
   * @param data data reference
   * @param gp window properties
   */
  TreeCaching(final Data data, final GUIProp gp) {
    prop = gp;
    if(data == null) return;
    maxLevel = data.meta.height + 1;
    cacheNodes(data);
  }

  /**
   * Determines nodes in each level and caches them.
   * @param data data reference
   */
  private void cacheNodes(final Data data) {

    // long time = System.currentTimeMillis();

    if(USE_CHILDITERATOR) {
      IntList parList = new IntList(1);
      parList.add(0);
      nodes = new IntList[maxLevel];
      int l = 0;
      while(maxLevel > l) {
        nodes[l++] = parList;
        parList = getNextNodeLine(parList, data);
      }
    } else {
      final IntList[] li = new IntList[maxLevel];
      for(int i = 0; i < maxLevel; i++)
        li[i] = new IntList();
      final int ts = data.meta.size;
      final int[] lvlPre = new int[maxLevel];
      li[0].add(0);
      lvlPre[0] = 0;
      for(int p = 1; p < ts; p++) {
        final int k = data.kind(p);
        if(k == Data.ATTR) continue;
        int lv = 0;
        final int par = data.parent(p, k);
        while(par != lvlPre[lv])
          lv++;
        lvlPre[lv + 1] = p;
        li[lv + 1].add(p);
      }
      nodes = li;
    }
  }

  /**
   * Saves node line in parentList.
   * @param parentList array with nodes of the line before
   * @param data the data reference
   * @return IntList filled with nodes of the current line
   */
  private IntList getNextNodeLine(final IntList parentList, final Data data) {
    final int l = parentList.size();
    final IntList line = new IntList();
    for(int i = 0; i < l; i++) {
      final int p = parentList.get(i);
      final ChildIterator iterator = new ChildIterator(data, p);
      while(iterator.more()) {
        final int pre = iterator.next();
        if(data.kind(pre) == Data.ELEM || !ONLY_ELEMENT_NODES) line.add(pre);
      }
    }
    return line;
  }

  /**
   * Returns the lowest index value.
   * @param lv level
   * @param lp left pre
   * @param rp right pre
   * @return index
   */
  private int getMinIndex(final int lv, final int lp, final int rp) {
    final int l = 0;
    final int r = nodes[lv].size() - 1;

    int min = searchPreIndex(lv, lp, rp, l, r);

    if(min == -1) return min;

    final int[] n = nodes[lv].finish();

    while(min-- > 0 && n[min] > lp)
      ;

    return min + 1;
  }

  /**
   * Create new rectangles and set subtree borders.
   * @param g graphics reference
   * @param c context
   * @param sw screen width
   * @return tree distance
   */
  double generateBordersAndRects(final Graphics g, 
      final Context c, final int sw) {
    final Data d = c.current.data;
    final int[] roots = c.current.nodes;
    final int rl = roots.length;
    if(rl == 0) return -1;
    final TreeBorder[][] bo = new TreeBorder[rl][];
    final double w = sw / (double) rl;
    if(w == 0) return -1;
    rects = new TreeRect[rl][][];

    for(int i = 0; i < rl; i++) {
      bo[i] = generateSubtreeBorders(d, roots[i]);
      generateRects(g, i, c, bo[i], w);
    }
    border = bo;
    return w;
  }

  /**
   * Generates subtree borders.
   * @param d data reference
   * @param pre pre value
   * @return borders array
   */
  TreeBorder[] generateSubtreeBorders(final Data d, final int pre) {

    final TreeBorder[] bo = new TreeBorder[maxLevel];
    if(pre == 0) {
      for(int i = 0; i < maxLevel; i++)
        bo[i] = new TreeBorder(i, 0, nodes[i].size());

      return bo;
    }

    final int[] rlp = findPre(pre);
    final int rl = rlp[0];
    final int ri = rlp[1];

    // level pair
    bo[rl] = new TreeBorder(rl, ri, 1);

    final int np = pre + d.size(pre, d.kind(pre));
    int h = 1;

    for(int i = rl + 1; i < maxLevel; i++) {

      final int min = getMinIndex(i, pre, np);

      if(min == -1) break;

      int c = 0;

      for(int j = min; j < nodes[i].size(); j++)
        if(nodes[i].get(j) < np) ++c;
        else break;

      bo[i] = new TreeBorder(i, min, c);
      ++h;
    }

    final TreeBorder[] bon = new TreeBorder[h];
    System.arraycopy(bo, rl, bon, 0, h);

    return bon;
  }

  /**
   * Generates cached rectangles.
   * @param g graphics reference
   * @param rn root number
   * @param c context
   * @param bo TreeBorder
   * @param sw screen width
   */
  private void generateRects(final Graphics g, final int rn, final Context c,
      final TreeBorder[] bo, final double sw) {

    final int h = bo.length;
    rects[rn] = new TreeRect[h][];

    for(int i = 0; i < h; i++) {

      final double w = sw / bo[i].size;

      if(w < 2) {
        bigRectangle(rn, i, sw);
      } else {
        normalRectangle(g, rn, i, c, w, bo[i]);
      }
    }
  }

  /**
   * Invoked if not enough space for more than one big rectangle.
   * @param rn root
   * @param lv level
   * @param w the width
   */
  private void bigRectangle(final int rn, final int lv, final double w) {
    rects[rn][lv] = new TreeRect[1];
    rects[rn][lv][0] = new TreeRect((int) (w * rn), (int) w);
  }

  /**
   * Creates normal rectangles.
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param c context
   * @param w width
   * @param bo TreeBorder
   */
  private void normalRectangle(final Graphics g, final int rn, final int lv,
      final Context c, final double w, final TreeBorder bo) {

 // new array, to be filled with the rectangles of the current level
    rects[rn][lv] = new TreeRect[bo.size];
    
    double xx = rn * w * bo.size;
    double ww = w;

    for(int i = bo.start; i < bo.start + bo.size; i++) {

      if(SLIM_TO_TEXT) {
        final double boxMiddle = xx + ww / 2f;
        final byte[] b = getText(c, rn, nodes[bo.level].get(i));
        int o = calcOptimalRectWidth(g, b) + 10;
        if(o < MIN_TXT_SPACE) o = MIN_TXT_SPACE;
        if(w > o) {
          xx = boxMiddle - o / 2d;
          ww = o;
        }
      }
      rects[rn][lv][i - bo.start] = new TreeRect((int) xx, (int) ww);

      xx += w;
    }
  }

  /**
   * Returns TreeRects in given level.
   * @param rn root number
   * @param lv level
   * @return TreeRect array
   */
  TreeRect[] getTreeRectsPerLevel(final int rn, final int lv) {
    return rects[rn][lv];
  }

  /**
   * Returns TreeRect at given index position.
   * @param rn root number
   * @param lv level
   * @param ix index
   * @return TreeRect rectanlge
   */
  TreeRect getTreeRectPerIndex(final int rn, final int lv, final int ix) {
    return rects[rn][lv][ix];
  }

  /**
   * Finds pre value in cached nodes and returns level and index position.
   * @param pre pre value
   * @return level and position pair
   */
  private int[] findPre(final int pre) {
    int pos = -1;
    int l;
    for(l = 0; l < maxLevel; l++) {
      pos = searchPreArrayPos(l, 0, nodes[l].size(), pre);
      if(pos > -1) break;
    }
    return pos > -1 ? new int[] { l, pos} : null;
  }

  /**
   * Determines the index position of given pre value.
   * @param bo border
   * @param pre pre value
   * @return the determined index position
   */
  private int searchPreArrayPos(final TreeBorder bo, final int pre) {
    return searchPreArrayPos(bo.level, bo.start, bo.start + bo.size, pre)
        - bo.start;
  }

  /**
   * Determines the index position of given pre value.
   * @param lv level to be searched
   * @param l left array border
   * @param r right array border
   * @param pre pre value
   * @return the determined index position
   */
  private int searchPreArrayPos(final int lv, final int l, final int r,
      final int pre) {
    return searchPreIndex(lv, pre, pre, l, r - 1);
  }

  /**
   * Returns subtree height.
   * @param rn root
   * @return height
   */
  int getHeight(final int rn) {
    return border.length > rn ? border[rn].length : -1;
  }

  /**
   * Returns level size.
   * @param rn root
   * @param lv level
   * @return size
   */
  int getLevelSize(final int rn, final int lv) {
    return border[rn][lv].size;
  }

  /**
   * Returns index of pre.
   * @param rn root
   * @param lv level
   * @param pre pre
   * @return index
   */
  int getPreIndex(final int rn, final int lv, final int pre) {
    final TreeBorder bo = getTreeBorder(rn, lv);
    return searchPreIndex(bo.level, pre, pre, bo.start, bo.size - 1) - bo.start;
  }

  /**
   * Returns pre by given index.
   * @param rn root
   * @param lv level
   * @param ix index
   * @return pre
   */
  int getPrePerIndex(final int rn, final int lv, final int ix) {
    return getPrePerIndex(getTreeBorder(rn, lv), ix);
  }

  /**
   * Returns pre by given index.
   * @param bo border
   * @param ix index
   * @return pre
   */
  int getPrePerIndex(final TreeBorder bo, final int ix) {
    return nodes[bo.level].get(bo.start + ix);
  }

  /**
   * Returns TreeBorder.
   * @param rn root
   * @param lv level
   * @return TreeBorder
   */
  TreeBorder getTreeBorder(final int rn, final int lv) {
    return border[rn][lv];
  }

  /**
   * Searches for pre value or pre range.
   * @param lv level
   * @param lb left TreeBorder
   * @param rb right TreeBorder
   * @param l left array TreeBorder
   * @param r right array TreeBorder
   * @return result index
   */
  private int searchPreIndex(final int lv, final int lb, final int rb,
      final int l, final int r) {

    int index = -1;
    int ll = l;
    int rr = r;

    while(rr >= ll && index == -1) {
      final int m = ll + (rr - ll) / 2;

      if(nodes[lv].get(m) < lb) {
        ll = m + 1;
      } else if(nodes[lv].get(m) > rb) {
        rr = m - 1;
      } else {
        index = m;
      }
    }
    return index;
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param rn root number
   * @param lv level
   * @param pre the pre value to be found
   * @return the rectangle containing the given pre value, null else
   */
  TreeRect searchRect(final int rn, final int lv, final int pre) {
    return searchRect(rn, lv, getTreeBorder(rn, lv), pre);
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param rn root number
   * @param lv level
   * @param bo border
   * @param pre the pre value to be found
   * @return the rectangle containing the given pre value, null else
   */
  TreeRect searchRect(final int rn, final int lv, final TreeBorder bo,
      final int pre) {
    final int i = searchPreArrayPos(bo, pre);

    // System.out.println("i " + i + " b " + (i - bo.start) + " l " +
    // rects[rn][lv].length);

    return i < 0 ? null : isBigRectangle(rn, lv) ? rects[rn][lv][0]
        : rects[rn][lv][i];
  }

  /**
   * Returns pre value at given level and index.
   * @param l level
   * @param i index
   * @return pre value
   */
  @SuppressWarnings("unused")
  private int getPrePerLevelAndIndex(final int l, final int i) {
    return nodes[l].get(i);
  }

  /**
   * Returns node text.
   * @param c context
   * @param rn root
   * @param pre pre
   * @return text
   */
  byte[] getText(final Context c, final int rn, final int pre) {
    final Data d = c.data;
    if(pre == c.current.nodes[rn]) return ViewData.path(d, pre);
    if(d.fs != null && d.kind(pre) != Data.TEXT || 
        d.kind(pre) == Data.ELEM) return ViewData.tag(
        prop, d, pre);
    return ViewData.content(d, pre, false);
  }

  /**
   * Calculates optimal rectangle width.
   * @param g the graphics reference
   * @param b byte array
   * @return optimal rectangle width
   */
  private int calcOptimalRectWidth(final Graphics g, final byte[] b) {
    return BaseXLayout.width(g, b);
  }

  /**
   * Returns true if big rectangle, false else.
   * @param rn root
   * @param lv level
   * @return boolean
   */
  boolean isBigRectangle(final int rn, final int lv) {
    return getTreeBorder(rn, lv).size > rects[rn][lv].length;
  }
}
