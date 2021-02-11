package org.basex.gui.view.tree;

import java.util.*;

import org.basex.data.*;
import org.basex.util.list.*;

/**
 * This class determines nodes per level and caches them.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Wolfgang Miller
 */
final class TreeNodeCache {
  /** Document depth. */
  private final int maxLevel;
  /** All nodes document nodes per level. */
  private final IntList[] nodes;

  /**
   * This constructor invokes methods to cache all document nodes.
   * @param data data reference
   * @param atts show attributes
   */
  TreeNodeCache(final Data data, final boolean atts) {
    final ArrayList<IntList> alil = new ArrayList<>();

    final int ts = data.meta.size;
    final IntList roots = data.resources.docs();
    alil.add(new IntList());
    final int rs = roots.size();
    for(int i = 0; i < rs; ++i) {
      final int root = roots.get(i);
      alil.get(0).add(root);
      final int sh = i + 1 == rs ? ts : roots.get(i + 1);
      for(int p = root + 1; p < sh; ++p) {
        final int k = data.kind(p);
        if(!atts && k == Data.ATTR) continue;
        final int par = data.parent(p, k);
        int lv = -1;
        final int is = alil.size();
        while(++lv < is && par != alil.get(lv).peek());
        for(int j = is; j <= lv + 1; ++j) alil.add(new IntList());
        alil.get(lv + 1).add(p);
      }
    }
    maxLevel = alil.size();
    nodes = alil.toArray(new IntList[0]);
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
    if(min == -1) return -1;
    final int[] n = nodes[lv].toArray();
    while(min-- > 0 && n[min] > lp);
    return min + 1;
  }

  /**
   * Generates subtree borders.
   * @param d data reference
   * @param pre pre value
   * @return borders array
   */
  TreeBorder[] subtree(final Data d, final int pre) {
    final TreeBorder[] bo = new TreeBorder[maxLevel];
    if(pre == 0 && d.meta.ndocs == 1) {
      for(int i = 0; i < maxLevel; ++i) bo[i] = new TreeBorder(i, 0, nodes[i].size());
      return bo;
    }

    final int[] rlp = findPre(pre);
    final int rl = rlp[0], ri = rlp[1];

    // level pair
    bo[rl] = new TreeBorder(rl, ri, 1);
    final int np = pre + d.size(pre, d.kind(pre));
    int h = 1;

    for(int i = rl + 1; i < maxLevel; ++i) {
      final int min = getMinIndex(i, pre, np);
      if(min == -1) break;
      int c = 0;
      final int ns = nodes[i].size();
      for(int j = min; j < ns; ++j) {
        if(nodes[i].get(j) < np) ++c;
        else break;
      }
      bo[i] = new TreeBorder(i, min, c);
      ++h;
    }
    return Arrays.copyOfRange(bo, rl, rl + h);
  }

  /**
   * Finds pre value in cached nodes and returns level and index position.
   * @param pre pre value
   * @return level and position pair, or {@code null} if it is not available
   */
  private int[] findPre(final int pre) {
    int pos = -1;
    int l;
    for(l = 0; l < maxLevel; ++l) {
      pos = searchPreArrayPos(l, 0, nodes[l].size() - 1, pre);
      if(pos > -1) break;
    }
    return pos > -1 ? new int[] { l, pos} : null;
  }

  /**
   * Determines the index position of given pre value.
   * @param lv level to be searched
   * @param l left array border
   * @param r right array border
   * @param pre pre value
   * @return the determined index position
   */
  int searchPreArrayPos(final int lv, final int l, final int r, final int pre) {
    return searchPreIndex(lv, pre, pre, l, r);
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
  int searchPreIndex(final int lv, final int lb, final int rb, final int l, final int r) {
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
   * Returns pre value at given level and index.
   * @param lv level
   * @param i index
   * @return pre value
   */
  int getPrePerLevelAndIndex(final int lv, final int i) {
    return nodes[lv].get(i);
  }
}
