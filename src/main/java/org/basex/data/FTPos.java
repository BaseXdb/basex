package org.basex.data;

import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains full-text positions for a single database node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTPos {
  /** Pre value. */
  final int pre;
  /** Positions. */
  IntList pos;

  /**
   * Constructor.
   * @param p pre value
   * @param ps sorted positions
   */
  FTPos(final int p, final IntList ps) {
    pre = p;
    pos = ps;
  }

  /**
   * Merges the specified position arrays.
   * @param ps sorted positions
   */
  void union(final IntList ps) {
    final IntSet set = new IntSet();
    for(int p = 0, s = pos.size(); p < s; p++) set.add(pos.get(p));
    for(int p = 0, s = ps.size(); p < s; p++) set.add(ps.get(p));
    pos = new IntList(set.toArray()).sort();
  }

  /**
   * Checks if the specified position is found.
   * @param p position to be found
   * @return result of check
   */
  public boolean contains(final int p) {
    return pos.sortedIndexOf(p) >= 0;
  }

  /**
   * Returns the number of positions.
   * @return number of positions
   */
  public int size() {
    return pos.size();
  }

  /**
   * Creates a copy.
   * @return the copy
   */
  public FTPos copy() {
    return new FTPos(pre, new IntList(pos.toArray()));
  }
}
