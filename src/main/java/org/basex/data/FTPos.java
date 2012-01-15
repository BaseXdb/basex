package org.basex.data;

import java.util.Arrays;

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
  int[] pos;
  /** Sorted flag. */
  private boolean sorted = true;

  /**
   * Constructor.
   * @param p pre value
   * @param ps positions
   */
  FTPos(final int p, final int[] ps) {
    pre = p;
    pos = ps;
    int x = -1;
    for(final int i : ps) {
      sorted = sorted && i >= x;
      x = i;
      if(!sorted) break;
    }
  }

  /**
   * Merges the specified position arrays.
   * @param ps positions
   */
  void union(final int[] ps) {
    // skip existing values
    if(Arrays.equals(pos, ps)) return;

    // merge entries with the same pre value
    final int psl = ps.length;
    final int pol = pos.length;
    final int[] ts = new int[psl + pol];
    for(int i = 0, si = 0, oi = 0; i < ts.length; ++i) {
      final boolean s = si == psl || oi < pol && pos[oi] < ps[si];
      ts[i] = s ? pos[oi++] : ps[si++];
    }
    pos = ts;
    int x = -1;
    for(final int i : pos) {
      sorted = sorted && i >= x;
      x = i;
      if(!sorted) break;
    }
  }

  /**
   * Checks if the specified position is found.
   * @param p position to be found
   * @return result of check
   */
  public boolean contains(final int p) {
    if(sorted) return Arrays.binarySearch(pos, p) >= 0;
    for(final int i : pos) if(i == p) return true;
    return false;
  }

  /**
   * Returns the number of positions.
   * @return number of positions
   */
  public int size() {
    return pos.length;
  }
}
