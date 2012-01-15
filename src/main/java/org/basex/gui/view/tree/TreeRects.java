package org.basex.gui.view.tree;

import java.awt.Graphics;

import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.ViewData;

/**
 * This class stores the rectangles.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Wolfgang Miller
 */
final class TreeRects implements TreeConstants {
  /** Saved rectangles. */
  private TreeRect[][][] rects;

  /**
   * Create new rectangles and set subtree borders.
   * @param sub subtree
   * @param g graphics reference
   * @param c context
   * @param ds draw start
   * @param dw draw width
   * @param slim slim to text
   * @return tree distance
   */
  double generateRects(final TreeSubtree sub, final Graphics g,
      final Context c, final int ds, final int dw, final boolean slim) {
    final int[] roots = c.current().list;
    final int rl = roots.length;
    if(rl == 0) return 0;
    final double w = (dw - BORDER_PADDING - ds) / (double) rl;
    if(w < 2) {
      return -1;
    }

    rects = new TreeRect[rl][][];

    for(int i = 0; i < rl; ++i) {
      generateRects(sub, c, g, i, ds, w, slim);
    }
    return w;
  }

  /**
   * Generates cached rectangles.
   * @param g graphics reference
   * @param rn root number
   * @param c context
   * @param sub subtree
   * @param ds draw start
   * @param dw draw width
   * @param slim slim to text
   * @return tree distance
   */
  private int generateRects(final TreeSubtree sub, final Context c,
      final Graphics g, final int rn, final int ds, final double dw,
      final boolean slim) {

    final int h = sub.getSubtreeHeight(rn);
    rects[rn] = new TreeRect[h][];
    double w = -1;

    for(int lv = 0; lv < h; ++lv) {

      w = dw / sub.levelSize(rn, lv);

      if(w < 2) {
        bigRectangle(rn, lv, ds, dw);
      } else {
        normalRectangle(sub, c, g, rn, lv, ds, w, slim);
      }
    }
    return (int) w;
  }

  /**
   * Invoked if not enough space for more than one big rectangle.
   * @param rn root
   * @param lv level
   * @param ds draw start
   * @param w the width
   */
  private void bigRectangle(final int rn, final int lv, final int ds,
      final double w) {
    rects[rn][lv] = new TreeRect[1];
    rects[rn][lv][0] = new TreeRect((int) (w * rn) + BORDER_PADDING + ds,
        (int) w - BORDER_PADDING);
  }

  /**
   * Creates normal rectangles.
   * @param sub subtree
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param c context
   * @param ds draw start
   * @param w width
   * @param slim slim to text
   */
  private void normalRectangle(final TreeSubtree sub, final Context c,
      final Graphics g, final int rn, final int lv, final int ds,
      final double w, final boolean slim) {

    final int subSi = sub.levelSize(rn, lv);
    // new array, to be filled with the rectangles of the current level
    rects[rn][lv] = new TreeRect[subSi];

    double xx = rn * w * subSi + ds;
    double ww = w;

    for(int i = 0; i < subSi; ++i) {

      if(slim) {
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
    final Data d = c.data();
    if(pre == c.current().list[rn]) return ViewData.path(d, pre);
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
  boolean bigRect(final TreeSubtree sub, final int rn, final int lv) {
    return !(sub.levelSize(rn, lv) == rects[rn][lv].length);
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
    final int idx = (int) (ratio * sub.levelSize(rn, lv));
    return sub.getPrePerIndex(rn, lv, idx);
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param sub subtree
   * @param rn root number
   * @param lv level
   * @param pre the pre value to be found
   * @return the rectangle containing the given pre value, {@code null} else
   */
  TreeRect searchRect(final TreeSubtree sub, final int rn, final int lv,
      final int pre) {

    final int i = sub.searchPreArrayPos(rn, lv, pre);
    return i < 0 ? null : bigRect(sub, rn, lv) ? rects[rn][lv][0]
        : rects[rn][lv][i];
  }
}
