package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTPosData {
  /** Position references. */
  private FTPos[] pos = new FTPos[1];
  /** Data reference. */
  private Data data;
  /** Number of values. */
  private int size;

  /**
   * Adds position data.
   *
   * @param d data reference
   * @param pre pre value
   * @param all full-text matches
   */
  public void add(final Data d, final int pre, final FTMatches all) {
    if(data == null) data = d;
    else if(data != d) return;

    final IntList ps = new IntList();
    for(final FTMatch m : all) {
      for(final FTStringMatch sm : m) {
        for(int s = sm.s; s <= sm.e; ++s) ps.add(s);
      }
    }

    int c = find(pre);
    if(c < 0) {
      c = -c - 1;
      if(size == pos.length) pos = Arrays.copyOf(pos, size << 1);
      Array.move(pos, c, 1, size++ - c);
      pos[c] = new FTPos(pre, ps.toArray());
    } else {
      pos[c].union(ps.toArray());
    }
  }

  /**
   * Gets full-text data from the container.
   * If no data is stored for a pre value, {@code null} is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   * @param d data reference
   * @param p int pre value
   * @return int[2][n] full-text data or {@code null}
   */
  public FTPos get(final Data d, final int p) {
    final int i = find(p);
    return i < 0 || data != d ? null : pos[i];
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
   * Compares full-text data for equality.
   * @param ft reference to compare to
   * @return boolean same()
   */
  boolean sameAs(final FTPosData ft) {
    if(size != ft.size) return false;
    for(int i = 0; i < size; ++i) {
      if(pos[i].pre != ft.pos[i].pre ||
          !Arrays.equals(pos[i].pos, ft.pos[i].pos)) return false;
    }
    return true;
  }

  /**
   * Returns the position of the specified pre value.
   * @param p int pre value
   * @return position or negative insertion value - 1
   */
  private int find(final int p) {
    // binary search
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = pos[m].pre - p;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }

  /**
   * Creates a copy.
   * @return copy
   */
  public FTPosData copy() {
    final FTPosData ftpos = new FTPosData();
    ftpos.data = data;
    ftpos.size = size;
    ftpos.pos = pos.clone();
    for(int i = 0; i < ftpos.pos.length; i++)
      if(ftpos.pos[i] != null) ftpos.pos[i] = ftpos.pos[i].copy();
    return ftpos;
  }
}
