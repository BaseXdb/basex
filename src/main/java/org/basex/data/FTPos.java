package org.basex.data;

import org.basex.util.list.*;

/**
 * This class contains full-text positions.
 * For each position, a pointer is stored.
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
    pos = ps.sort().unique();
  }

  /**
   * Merges the specified position arrays.
   * @param ps sorted positions
   */
  void union(final IntList ps) {
    // merge entries with the same pre value
    ps.sort().unique();
    final int psl = ps.size();
    final int pol = pos.size();
    final int tl = psl + pol;
    final IntList ts = new IntList(tl);
    final IntList pos0 = pos;
    for(int i = 0, si = 0, oi = 0; i < tl; ++i) {
      final boolean s = si == psl || oi < pol && pos0.get(oi) < ps.get(si);
      ts.add(s ? pos0.get(oi++) : ps.get(si++));
    }
    pos = ts.unique();
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
