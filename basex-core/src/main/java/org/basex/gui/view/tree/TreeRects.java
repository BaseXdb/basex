package org.basex.gui.view.tree;

import java.awt.*;

import org.basex.data.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;

/**
 * This class stores the rectangles.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Wolfgang Miller
 */
final class TreeRects implements TreeConstants {
  /** Saved rectangles. */
  private TreeRect[][][] rects;
  /** View. */
  final View view;
  /** Displayed nods. */
  Nodes nodes;

  /**
   * Constructor.
   * @param v view
   */
  TreeRects(final View v) {
    view = v;
  }

  /**
   * Create new rectangles and set subtree borders.
   * @param sub subtree
   * @param g graphics reference
   * @param ds draw start
   * @param dw draw width
   * @param slim slim to text
   * @return tree distance
   */
  double generateRects(final TreeSubtree sub, final Graphics g, final int ds, final int dw,
      final boolean slim) {

    final int[] roots = nodes.pres;
    final int rl = roots.length;
    if(rl == 0) return 0;
    final double w = (dw - BORDER_PADDING - ds) / (double) rl;
    if(w < 2) return -1;

    rects = new TreeRect[rl][][];
    for(int i = 0; i < rl; ++i) generateRects(sub, g, i, ds, w, slim);
    return w;
  }

  /**
   * Generates cached rectangles.
   *
   * @param sub subtree
   * @param g graphics reference
   * @param rn root number
   * @param ds draw start
   * @param dw draw width
   * @param slim slim to text
   */
  private void generateRects(final TreeSubtree sub, final Graphics g, final int rn, final int ds,
      final double dw, final boolean slim) {

    final int h = sub.subtreeHeight(rn);
    rects[rn] = new TreeRect[h][];

    for(int lv = 0; lv < h; ++lv) {
      final double w = dw / sub.levelSize(rn, lv);
      if(w < 2) {
        bigRectangle(rn, lv, ds, dw);
      } else {
        normalRectangle(sub, g, rn, lv, ds, w, slim);
      }
    }
  }

  /**
   * Invoked if not enough space for more than one big rectangle.
   * @param rn root
   * @param lv level
   * @param ds draw start
   * @param w the width
   */
  private void bigRectangle(final int rn, final int lv, final int ds, final double w) {
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
   * @param ds draw start
   * @param w width
   * @param slim slim to text
   */
  private void normalRectangle(final TreeSubtree sub, final Graphics g, final int rn,
      final int lv, final int ds, final double w, final boolean slim) {

    final int subSi = sub.levelSize(rn, lv);
    // new array, to be filled with the rectangles of the current level
    rects[rn][lv] = new TreeRect[subSi];

    double xx = rn * w * subSi + ds;
    double ww = w;

    for(int i = 0; i < subSi; ++i) {

      if(slim) {
        final double boxMiddle = xx + ww / 2f;
        final byte[] b = getText(sub.prePerIndex(rn, lv, i));
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
   * @return tree rectangle
   */
  TreeRect getTreeRectPerIndex(final int rn, final int lv, final int ix) {
    return rects[rn][lv][ix];
  }

  /**
   * Returns node text.
   * @param pre pre
   * @return text
   */
  byte[] getText(final int pre) {
    return ViewData.name(view.gui.gopts, nodes.data, pre);
  }

  /**
   * Calculates optimal rectangle width.
   * @param g the graphics reference
   * @param b byte array
   * @return optimal rectangle width
   */
  private static int calcOptimalRectWidth(final Graphics g, final byte[] b) {
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
  int getPrePerXPos(final TreeSubtree sub, final int rn, final int lv, final int x) {
    final TreeRect r = getTreeRectsPerLevel(rn, lv)[0];
    final double ratio = (x - r.x) / (double) r.w;
    final int idx = (int) (ratio * sub.levelSize(rn, lv));
    return sub.prePerIndex(rn, lv, idx);
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param sub subtree
   * @param rn root number
   * @param lv level
   * @param pre the pre value to be found
   * @return the rectangle containing the given pre value, {@code null} else
   */
  TreeRect searchRect(final TreeSubtree sub, final int rn, final int lv, final int pre) {
    final int i = sub.searchPreArrayPos(rn, lv, pre);
    return i < 0 ? null : bigRect(sub, rn, lv) ? rects[rn][lv][0] : rects[rn][lv][i];
  }
}
