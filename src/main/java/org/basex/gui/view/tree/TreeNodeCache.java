package org.basex.gui.view.tree;

import org.basex.data.Data;
import org.basex.util.IntList;

/**
 * This class determines nodes per level and caches them.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
final class TreeNodeCache implements TreeViewOptions {
  /** Document depth. */
  private final int maxLevel;
  /** All nodes document nodes per level. */
  private final IntList[] nodes;

  /**
   * This constructor invokes methods to cache all document nodes.
   * @param data data reference
   */
  TreeNodeCache(final Data data) {
    maxLevel = data.meta.height + 1;

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
      final int[] roots = data.doc();
      for(int i = 0; i < roots.length; i++) {
        final int root = roots[i];
        li[0].add(root);
        final int sh = i + 1 == roots.length ? ts : roots[i + 1];
        for(int p = root + 1; p < sh; p++) {
          final int k = data.kind(p);
          if((!SHOW_ATTR && k == Data.ATTR)
              || (ONLY_ELEMENT_NODES & k != Data.ELEM)) continue;
          int lv = 0;
          final int par = data.parent(p, k);
          while(par != li[lv].get(li[lv].size() - 1))
            lv++;
          li[lv + 1].add(p);
        }
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
        if(data.kind(pre) == Data.ELEM ||
            !ONLY_ELEMENT_NODES) line.add(pre);
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
   * Generates subtree borders.
   * @param d data reference
   * @param pre pre value
   * @return borders array
   */
  TreeBorder[] subtree(final Data d, final int pre) {

    final TreeBorder[] bo = new TreeBorder[maxLevel];
    if(pre == 0 && d.meta.ndocs == 1) {
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
   * Finds pre value in cached nodes and returns level and index position.
   * @param pre pre value
   * @return level and position pair
   */
  private int[] findPre(final int pre) {
    int pos = -1;
    int l;
    for(l = 0; l < maxLevel; l++) {
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
   * Returns pre by given index.
   * @param bo border
   * @param ix index
   * @return pre
  int getPrePerIndex(final TreeBorder bo, final int ix) {
    return nodes[bo.level].get(bo.start + ix);
  }
   */

  /**
   * Searches for pre value or pre range.
   * @param lv level
   * @param lb left TreeBorder
   * @param rb right TreeBorder
   * @param l left array TreeBorder
   * @param r right array TreeBorder
   * @return result index
   */
  int searchPreIndex(final int lv, final int lb, final int rb, final int l,
      final int r) {

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
