package org.basex.gui.view.tree;

import java.awt.Graphics;
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
public class TreeCaching implements TreeViewOptions {
  /** Document depth. */
  public int maxLevel = -1;
  /** All nodes document nodes per level. */
  private int[][] nodes;
  /** Saved rectangles. */
  public TreeRect[][][] rects;
  /** Subtree borders. */
  private TreeBorder[][] border;

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

    // long time = System.currentTimeMillis();
    nodes = new int[maxLevel][];

    if(USE_CHILDITERATOR) {

      int[] parentList = { 0};
      int l = 0;

      while(maxLevel > l) {
        nodes[l++] = parentList;
        parentList = getNextNodeLine(parentList, data);
      }
    } else {

      IntList[] li = new IntList[maxLevel];
      for(int i = 0; i < maxLevel; i++)
        li[i] = new IntList();

      final int ts = data.meta.size;
      int[] lvlPre = new int[maxLevel];
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
      nodes[0] = new int[] { 0};
      for(int i = 1; i < maxLevel; i++)
        nodes[i] = li[i].finish();
    }
    // System.out.println(System.currentTimeMillis() - time);
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

  /**
   * Returns the lowest index value.
   * @param lv level
   * @param lp left pre
   * @param rp right pre
   * @return index
   */
  private int getMinIndex(final int lv, final int lp, final int rp) {
    int l = 0;
    int r = nodes[lv].length - 1;

    int min = searchPreIndex(lv, lp, rp, l, r);

    if(min == -1) return min;

    final int[] n = nodes[lv];

    while(min-- > 0 && n[min] > lp)
      ;

    return min + 1;
  }

  /**
   * Create new rectangles and set subtree borders.
   * @param g graphics reference
   * @param c context
   * @param sw screen width
   */
  void generateBordersAndRects(final Graphics g, 
      final Context c, final int sw) {

    final Data d = c.current.data;
    int[] roots = c.current.nodes;
    final int rl = roots.length;
    TreeBorder[][] bo = new TreeBorder[rl][];
    rects = new TreeRect[rl][][];

    for(int i = 0; i < rl; i++) {
      bo[i] = generateSubtreeBorders(d, roots[i]);
      generateRects(g, i, d, bo[i], sw - rl);
    }
    border = bo;
  }

  /**
   * Generates subtree borders.
   * @param d data reference
   * @param pre pre value
   * @return borders array
   */
  TreeBorder[] generateSubtreeBorders(final Data d, final int pre) {

    TreeBorder[] bo = new TreeBorder[maxLevel];
    if(pre == 0) {
      for(int i = 0; i < maxLevel; i++)
        bo[i] = new TreeBorder(i, 0, nodes[i].length);

      return bo;
    }

    final int[] rlp = findPre(pre);
    final int rl = rlp[0];
    final int ri = rlp[1];

    // level pair
    bo[rl] = new TreeBorder(rl, ri, 1);

    // System.out.println("rootl: " + rl);

    int np = pre + d.size(pre, d.kind(pre));
    int h = 1;

    for(int i = rl + 1; i < maxLevel; i++) {

      int min = getMinIndex(i, pre, np);

      if(min == -1) break;

      int c = 0;

      for(int j = min; j < nodes[i].length; j++)
        if(nodes[i][j] < np) ++c;
        else break;

      bo[i] = new TreeBorder(i, min, c);
      ++h;
    }

    TreeBorder[] bon = new TreeBorder[h];
    System.arraycopy(bo, rl, bon, 0, h);

    return bon;
  }

  /**
   * Generates cached rectangles.
   * @param g graphics reference
   * @param rn root number
   * @param d data
   * @param bo TreeBorder
   * @param sw screen width
   */
  private void generateRects(final Graphics g, final int rn, final Data d,
      final TreeBorder[] bo, final int sw) {

    final int h = bo.length;
    rects[rn] = new TreeRect[h][];

    for(int i = 0; i < h; i++) {

      double w = sw / bo[i].size;

      if(w < 2) {
        bigRectangle(rn, i, sw);
      } else {
        normalRectangle(g, rn, i, d, w, bo[i]);
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
    rects[rn][lv][0] = new TreeRect(0, (int) w);
  }

  /**
   * Creates normal rectangles.
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param d data reference
   * @param w width
   * @param bo TreeBorder
   */
  private void normalRectangle(final Graphics g, final int rn, final int lv,
      final Data d, final double w, final TreeBorder bo) {

    double xx = 0;
    double ww = w;

    // new array, to be filled with the rectangles of the current level
    rects[rn][lv] = new TreeRect[bo.size];

    for(int i = bo.start; i < bo.start + bo.size; i++) {

      final double boxMiddle = xx + ww / 2f;

      if(SLIM_TO_TEXT) {
        final String st = getText(d, getPrePerLevelAndIndex(bo.level, i));
        int o = calcOptimalRectWidth(g, st) + 10;
        if(o < MIN_SPACE) o = MIN_SPACE;

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
   * Finds pre value in cached nodes and returns level and index position.
   * @param pre pre value
   * @return level and position pair
   */
  private int[] findPre(final int pre) {

    int pos = -1;
    int l;

    for(l = 0; l < maxLevel; l++) {

      pos = searchPreArrayPos(l, pre);

      if(pos > -1) break;
    }

    return pos > -1 ? new int[] { l, pos} : null;
  }

  /**
   * Determines the index position of given pre value.
   * @param lv level to be searched
   * @param pre the pre value
   * @return the determined index position
   */
  private int searchPreArrayPos(final int lv, final int pre) {
    return searchPreIndex(lv, pre, pre, 0, nodes[lv].length - 1);
  }

  /**
   * Returns subtree height.
   * @param rn root
   * @return height
   */
  int getHeight(final int rn) {
    return border[rn].length;
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
    return searchPreIndex(bo.level, pre, pre, bo.start, bo.size) - bo.start;
  }

  /**
   * Returns pre by given index.
   * @param rn root
   * @param lv level
   * @param ix index
   * @return pre
   */
  int getPrePerIndex(final int rn, final int lv, final int ix) {
    final TreeBorder bo = getTreeBorder(rn, lv);
    return nodes[bo.level][bo.start + ix];
  }

  /**
   * Returns TreeBorder.
   * @param rn root
   * @param lv level
   * @return TreeBorder
   */
  private TreeBorder getTreeBorder(final int rn, final int lv) {
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

      if(nodes[lv][m] < lb) {
        ll = m + 1;
      } else if(nodes[lv][m] > rb) {
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
    final TreeBorder bo = getTreeBorder(rn, lv);

    int i = searchPreArrayPos(bo.level, pre);

    return i == -1 ? null : rects[rn][lv][i - bo.start];
  }

  /**
   * Returns pre value at given level and index.
   * @param l level
   * @param i index
   * @return pre value
   */
  private int getPrePerLevelAndIndex(final int l, final int i) {
    return nodes[l][i];
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
   * Calculates optimal rectangle width.
   * @param g the graphics reference
   * @param s given string
   * @return optimal rectangle width
   */
  private int calcOptimalRectWidth(final Graphics g, final String s) {
    return BaseXLayout.width(g, s);
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
