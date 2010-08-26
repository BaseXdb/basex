package org.basex.gui.view.tree;

import java.awt.Graphics;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;

/**
 * This class stores the rectangles.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
final class TreeRects implements TreeViewOptions {
  /** Saved rectangles. */
  private TreeRect[][][] rects;
  /** Window properties. */
  private final GUIProp prop;

  /**
   * Creates new TreeRects instance.
   * @param gp window properties
   */
  public TreeRects(final GUIProp gp) {
    prop = gp;
  }

  /**
   * Create new rectangles and set subtree borders.
   * @param sub subtree
   * @param g graphics reference
   * @param c context
   * @param sw screen width
   * @return tree distance
   */
  double generateRects(final TreeSubtree sub, final Graphics g,
      final Context c, final int sw) {
    final int[] roots = c.current.nodes;
    final int rl = roots.length;
    if(rl == 0) return 0;
    final double w = (sw - BORDER_PADDING) / (double) rl;
    if(w <= 2) {
      return -1;
    }

    rects = new TreeRect[rl][][];

    for(int i = 0; i < rl; ++i) {
      generateRects(sub, c, g, i, w);
    }
    return w;
  }

  /**
   * Generates cached rectangles.
   * @param g graphics reference
   * @param rn root number
   * @param c context
   * @param sub subtree
   * @param sw screen width
   * @return tree distance
   */
  private int generateRects(final TreeSubtree sub, final Context c,
      final Graphics g, final int rn, final double sw) {

    final int h = sub.getSubtreeHeight(rn);
    rects[rn] = new TreeRect[h][];
    double w = -1;

    for(int lv = 0; lv < h; ++lv) {

      w = sw / sub.getLevelSize(rn, lv);

      if(w < 2) {
        bigRectangle(rn, lv, sw);
      } else {
        normalRectangle(sub, c, g, rn, lv, w);
      }
    }
    return (int) w;
  }

  /**
   * Invoked if not enough space for more than one big rectangle.
   * @param rn root
   * @param lv level
   * @param w the width
   */
  private void bigRectangle(final int rn, final int lv, final double w) {
    rects[rn][lv] = new TreeRect[1];
    rects[rn][lv][0] = new TreeRect((int) (w * rn) + BORDER_PADDING, (int) w
        - BORDER_PADDING);
  }

  /**
   * Creates normal rectangles.
   * @param sub subtree
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param c context
   * @param w width
   */
  private void normalRectangle(final TreeSubtree sub, final Context c,
      final Graphics g, final int rn, final int lv, final double w) {

    final int subSi = sub.getLevelSize(rn, lv);
    // new array, to be filled with the rectangles of the current level
    rects[rn][lv] = new TreeRect[subSi];

    double xx = rn * w * subSi;
    double ww = w;

    for(int i = 0; i < subSi; ++i) {

      if(SLIM_TO_TEXT) {
        final double boxMiddle = xx + ww / 2f;
        final byte[] b = getText(c, rn, sub.getPrePerIndex(rn, lv, i));
        int o = calcOptimalRectWidth(g, b) + 10;
        if(o < MIN_TXT_SPACE) o = MIN_TXT_SPACE;
        if(w > o) {
          xx = boxMiddle - o / 2d;
          ww = o;
        }
      }
      rects[rn][lv][i] = new TreeRect((int) xx + BORDER_PADDING, (int) ww
          - BORDER_PADDING);

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
   * Returns node text.
   * @param c context
   * @param rn root
   * @param pre pre
   * @return text
   */
  byte[] getText(final Context c, final int rn, final int pre) {
    final Data d = c.data;
    if(pre == c.current.nodes[rn]) return ViewData.path(d, pre);
    if(d.fs != null && d.kind(pre) != Data.TEXT || d.kind(pre) == Data.ELEM)
      return ViewData.tag(
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
   * @param sub subtree
   * @param rn root
   * @param lv level
   * @return boolean
   */
  boolean isBigRectangle(final TreeSubtree sub, final int rn, final int lv) {
    return !(sub.getLevelSize(rn, lv) == rects[rn][lv].length);
  }

  /**
   * Returns pre value for given x position of a big rectangle.
   * @param sub subtree
   * @param rn root
   * @param lv level
   * @param x x position
   * @return pre value
   */
  int getPrePerXPos(final TreeSubtree sub, final int rn, final int lv,
      final int x) {
    final TreeRect r = getTreeRectsPerLevel(rn, lv)[0];
    final double ratio = (x - r.x) / (double) r.w;
    final int idx = (int) (ratio * sub.getLevelSize(rn, lv));
    return sub.getPrePerIndex(rn, lv, idx);
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param sub subtree
   * @param rn root number
   * @param lv level
   * @param pre the pre value to be found
   * @return the rectangle containing the given pre value, null else
   */
  TreeRect searchRect(final TreeSubtree sub, final int rn, final int lv,
      final int pre) {
    final int i = sub.searchPreArrayPos(rn, lv, pre);

    // System.out.println("i " + i + " b " + (i - bo.start) + " l " +
    // rects[rn][lv].length);

    return i < 0 ? null : isBigRectangle(sub, rn, lv) ? rects[rn][lv][0]
        : rects[rn][lv][i];
  }

}
