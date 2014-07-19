package org.basex.data;

import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains full-text positions for a single database node.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTPos {
  /** Pre value. */
  final int pre;
  /** Positions. */
  IntList poss;

  /**
   * Constructor.
   * @param pre pre value
   * @param list sorted positions
   */
  FTPos(final int pre, final IntList list) {
    this.pre = pre;
    this.poss = list;
  }

  /**
   * Merges the specified position arrays.
   * @param list sorted positions
   */
  void union(final IntList list) {
    final IntSet set = new IntSet(poss.size() + list.size());
    for(int p = 0, s = poss.size(); p < s; p++) set.add(poss.get(p));
    for(int p = 0, s = list.size(); p < s; p++) set.add(list.get(p));
    poss = new IntList(set.toArray()).sort();
  }

  /**
   * Checks if the specified position is found.
   * @param pos position to be found
   * @return result of check
   */
  public boolean contains(final int pos) {
    return poss.sortedIndexOf(pos) >= 0;
  }

  /**
   * Returns the number of positions.
   * @return number of positions
   */
  public int size() {
    return poss.size();
  }

  /**
   * Creates a copy.
   * @return the copy
   */
  public FTPos copy() {
    return new FTPos(pre, new IntList(poss.toArray()));
  }
}
