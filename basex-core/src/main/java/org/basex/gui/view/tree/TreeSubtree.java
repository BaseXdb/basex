package org.basex.gui.view.tree;

import org.basex.data.*;

/**
 * This class stores the subtrees.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Wolfgang Miller
 */
final class TreeSubtree {
  /** TreeNodeCache Object, contains cached document. */
  private final TreeNodeCache nc;
  /** Subtree borders. */
  private TreeBorder[][] border;

  /**
   * Creates new subtree.
   * @param d data
   * @param atts show attributes
   */
  TreeSubtree(final Data d, final boolean atts) {
    nc = new TreeNodeCache(d, atts);
  }

  /**
   * Generates subtree borders.
   * @param d data reference
   * @param roots root nodes
   */
  void generateBorders(final Data d, final int[] roots) {
    final int rl = roots.length;
    if(rl == 0) return;
    border = new TreeBorder[rl][];

    for(int i = 0; i < rl; ++i) {
      border[i] = nc.subtree(d, roots[i]);
    }
  }

  /**
   * Returns pre by given index.
   * @param rn root
   * @param lv level
   * @param ix index
   * @return pre
   */
  int prePerIndex(final int rn, final int lv, final int ix) {
    return prePerIndex(treeBorder(rn, lv), ix);
  }

  /**
   * Returns pre by given index.
   * @param bo border
   * @param ix index
   * @return pre
   */
  private int prePerIndex(final TreeBorder bo, final int ix) {
    final int start = bo.start + ix;
    if(start < 0) return -1;
    return nc.getPrePerLevelAndIndex(bo.level, start);
  }

  /**
   * Returns index of pre.
   * @param rn root
   * @param lv level
   * @param pre pre
   * @return index
   */
  int preIndex(final int rn, final int lv, final int pre) {
    return preIndex(treeBorder(rn, lv), pre);
  }

  /**
   * Returns index of pre.
   * @param bo border
   * @param pre pre
   * @return index
   */
  private int preIndex(final TreeBorder bo, final int pre) {
    return nc.searchPreIndex(bo.level, pre, pre, bo.start, bo.getEnd()) - bo.start;
  }

  /**
   * Returns level size.
   * @param rn root
   * @param lv level
   * @return size
   */
  int levelSize(final int rn, final int lv) {
    return treeBorder(rn, lv).size;
  }

  /**
   * Returns TreeBorder.
   * @param rn root
   * @param lv level
   * @return TreeBorder
   */
  TreeBorder treeBorder(final int rn, final int lv) {
    return border[rn][lv];
  }

  /**
   * Returns subtree height.
   * @param rn root
   * @return height
   */
  int subtreeHeight(final int rn) {
    return rn >= 0 && rn < border.length ? border[rn].length : -1;
  }

  /**
   * Returns maximum subtree height.
   * @return max height
   */
  int maxSubtreeHeight() {
    int h = 0;
    for(final TreeBorder[] b : border) {
      final int hh = b.length;
      if(hh > h) h = hh;
    }
    return h;
  }

  /**
   * Determines the index position of given pre value.
   * @param rn root
   * @param lv level
   * @param pre pre value
   * @return the determined index position
   */
  int searchPreArrayPos(final int rn, final int lv, final int pre) {
    return searchPreArrayPos(treeBorder(rn, lv), pre);
  }

  /**
   * Determines the index position of given pre value.
   * @param bo border
   * @param pre pre value
   * @return the determined index position
   */
  private int searchPreArrayPos(final TreeBorder bo, final int pre) {
    return nc.searchPreArrayPos(bo.level, bo.start, bo.getEnd(), pre) - bo.start;
  }

  /**
   * Returns subtree borders.
   * @param d data
   * @param pre pre
   * @return subtree borders
   */
  TreeBorder[] subtree(final Data d, final int pre) {
    return nc.subtree(d, pre);
  }
}
