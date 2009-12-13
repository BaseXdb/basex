package org.basex.data;

import java.util.Arrays;
import org.basex.core.Main;

/**
 * This class contains full-text positions.
 * For each position, a pointer is stored.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTPos {
  /** Sorted flag. */
  boolean sorted = true;
  /** Positions. */
  public int[] pos;
  /** Pre value. */
  int pre;

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
    for(int i = 0, si = 0, oi = 0; i < ts.length; i++) {
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Main.name(this));
    sb.append("[" + pre + ": ");
    for(int i = 0; i < pos.length; i++) sb.append((i != 0 ? "," : "") + pos[i]);
    return sb.append("]").toString();
  }
}
