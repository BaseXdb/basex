package org.basex.gui.view.tree;

import static org.basex.gui.view.tree.TreeConstants.*;
import java.awt.*;

import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.query.value.seq.*;

/**
 * This class stores the rectangles.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Wolfgang Miller
 */
final class TreeRects {
  /** View. */
  private final View view;

  /** Saved rectangles. */
  private TreeRect[][][] rects;
  /** Displayed nods. */
  DBNodes nodes;
  /** Graphics reference. */
  Graphics g;

  /**
   * Constructor.
   * @param view view
   */
  TreeRects(final View view) {
    this.view = view;
  }

  /**
   * Create new rectangles and set subtree borders.
   * @param sub subtree
   * @param ds draw start
   * @param dw draw width
   * @param slim slim to text
   * @return tree distance
   */
  double generateRects(final TreeSubtree sub, final int ds, final int dw, final boolean slim) {
    final int[] roots = nodes.pres();
    final int rl = roots.length;
    if(rl == 0) return 0;
    final double w = (dw - BORDER_PADDING - ds) / (double) rl;
    if(w < 2) return -1;

    rects = new TreeRect[rl][][];
    for(int i = 0; i < rl; ++i) generateRects(sub, i, ds, w, slim);
    return w;
  }

  /**
   * Generates cached rectangles.
   *
   * @param sub subtree
   * @param rn root number
   * @param ds draw start
   * @param dw draw width
   * @param slim slim to text
   */
  private void generateRects(final TreeSubtree sub, final int rn, final int ds, final double dw,
      final boolean slim) {

    final int h = sub.subtreeHeight(rn);
    rects[rn] = new TreeRect[h][];

    for(int lv = 0; lv < h; ++lv) {
      final double w = dw / sub.levelSize(rn, lv);
      if(w < 2) {
        bigRectangle(rn, lv, ds, dw);
      } else {
        normalRectangle(sub, rn, lv, ds, w, slim);
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
   * @param rn root
   * @param lv level
   * @param ds draw start
   * @param w width
   * @param slim slim to text
   */
  private void normalRectangle(final TreeSubtree sub, final int rn, final int lv, final int ds,
      final double w, final boolean slim) {

    final int subSi = sub.levelSize(rn, lv);
    // new array, to be filled with the rectangles of the current level
    rects[rn][lv] = new TreeRect[subSi];

    double xx = rn * w * subSi + ds, ww = w;
    for(int i = 0; i < subSi; ++i) {
      if(slim) {
        final double boxMiddle = xx + ww / 2.0f;
        final byte[] text = text(sub.prePerIndex(rn, lv, i));
        int o = calcOptimalRectWidth(text) + 10;
        if(o < MIN_TXT_SPACE) o = MIN_TXT_SPACE;
        if(w > o) {
          xx = boxMiddle - o / 2.0d;
          ww = o;
        }
      }
      rects[rn][lv][i] = new TreeRect((int) xx + BORDER_PADDING, (int) ww - BORDER_PADDING);

      xx += w;
    }
  }

  /**
   * Returns TreeRects in given level.
   * @param rn root number
   * @param lv level
   * @return TreeRect array
   */
  TreeRect[] treeRectsPerLevel(final int rn, final int lv) {
    return rects[rn][lv];
  }

  /**
   * Returns TreeRect at given index position.
   * @param rn root number
   * @param lv level
   * @param ix index
   * @return tree rectangle
   */
  TreeRect treeRectPerIndex(final int rn, final int lv, final int ix) {
    return rects[rn][lv][ix];
  }

  /**
   * Returns node text.
   * @param pre pre
   * @return text
   */
  byte[] text(final int pre) {
    return ViewData.namedText(view.gui.gopts, nodes.data(), pre);
  }

  /**
   * Calculates optimal rectangle width.
   * @param string string
   * @return optimal rectangle width
   */
  private int calcOptimalRectWidth(final byte[] string) {
    return BaseXLayout.width(g, string);
  }

  /**
   * Returns true if big rectangle, false else.
   * @param sub subtree
   * @param rn root
   * @param lv level
   * @return boolean
   */
  boolean bigRect(final TreeSubtree sub, final int rn, final int lv) {
    return sub.levelSize(rn, lv) != rects[rn][lv].length;
  }

  /**
   * Returns pre value for given x position of a big rectangle.
   * @param sub subtree
   * @param rn root
   * @param lv level
   * @param x x position
   * @return pre value
   */
  int prePerXPos(final TreeSubtree sub, final int rn, final int lv, final int x) {
    final TreeRect r = treeRectsPerLevel(rn, lv)[0];
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
    return i < 0 ? null : rects[rn][lv][bigRect(sub, rn, lv) ? 0 : i];
  }
}
