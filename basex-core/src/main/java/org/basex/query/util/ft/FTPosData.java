package org.basex.query.util.ft;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTPosData {
  /** Position references. */
  private FTPos[] pos = new FTPos[1];
  /** Data reference. */
  private Data dt;
  /** Number of values. */
  private int size;

  /**
   * Adds position data.
   *
   * @param data data reference
   * @param pre pre value
   * @param all full-text matches
   */
  public void add(final Data data, final int pre, final FTMatches all) {
    if(dt == null) dt = data;
    else if(dt != data) return;

    // cache all positions
    final IntSet set = new IntSet();
    for(final FTMatch ftm : all) {
      for(final FTStringMatch sm : ftm) {
        for(int s = sm.start; s <= sm.end; ++s) set.add(s);
      }
    }

    // sort and store all positions
    final IntList il = new IntList(set.toArray()).sort();
    int c = find(pre);
    if(c < 0) {
      c = -c - 1;
      final int sz = size;
      if(sz == pos.length) pos = Arrays.copyOf(pos, Array.newCapacity(sz));
      Array.insert(pos, c, 1, sz, null);
      pos[c] = new FTPos(pre, il);
      size++;
    } else {
      pos[c].union(il);
    }
  }

  /**
   * Gets full-text data from the container.
   * If no data is stored for a pre value, {@code null} is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   * @param data data reference
   * @param pre int pre value
   * @return int[2][n] full-text data or {@code null}
   */
  public FTPos get(final Data data, final int pre) {
    final int p = find(pre);
    return p < 0 || dt != data ? null : pos[p];
  }

  /**
   * Returns the number of entries.
   * @return size
   */
  public int size() {
    int c = 0;
    for(int i = 0; i < size; ++i) c += pos[i].size();
    return c;
  }

  /**
   * Returns the index of the specified pre value.
   * @param pre int pre value
   * @return index, or negative index - 1 if pre value is not found
   */
  private int find(final int pre) {
    // binary search
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = pos[m].pre - pre;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTPosData)) return false;
    final FTPosData ft = (FTPosData) obj;
    if(size != ft.size) return false;
    for(int p = 0; p < size; p++) {
      if(pos[p].pre != ft.pos[p].pre || !pos[p].equals(ft.pos[p])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int p = 0; p < size; p++) {
      if(sb.length() > 0) sb.append('\n');
      sb.append(pos[p]);
    }
    return sb.toString();
  }
}
