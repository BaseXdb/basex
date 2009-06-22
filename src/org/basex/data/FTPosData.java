package org.basex.data;

import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTPosData {
  /** Position references. */
  private FTPos[] pos = new FTPos[1];
  /** Number of values. */
  private int size;

  /**
   * Adds position data.
   * @param pre pre value
   * @param all full-text matches
   */
  public void add(final int pre, final FTMatches all) {
    final IntList ps = new IntList();
    final TokenBuilder pi = new TokenBuilder();
    for(final FTMatch m : all) {
      for(final FTStringMatch sm : m) {
        for(int s = sm.start; s <= sm.end; s++) {
          ps.add(s);
          pi.add(sm.queryPos);
        }
      }
    }

    int c = find(pre);
    if(c < 0) { 
      c = -c - 1;
      if(size == pos.length) pos = Array.extend(pos);
      Array.move(pos, c, 1, size++ - c);

      pos[c] = new FTPos(pre, ps.finish(), pi.finish());
    } else {
      pos[c].union(ps.finish(), pi.finish());
    }
  }
  
  /**
   * Gets full-text data from the container.
   * If no data is stored for a pre value, null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param p int pre value
   * @return int[2][n] full-text data or null
   */
  public FTPos get(final int p) {
    final int i = find(p);
    return i < 0 ? null : pos[i];
  }

  /**
   * Compares full-text data for equality.
   * @param ft reference to compare to
   * @return boolean same()
   */
  boolean same(final FTPosData ft) {
    if(size != ft.size) return false;
    for(int i = 0; i < size; i++) {
      if(pos[i].pre != ft.pos[i].pre || !Array.eq(pos[i].pos, ft.pos[i].pos) ||
          !Array.eq(pos[i].poi, ft.pos[i].poi)) return false;
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
}
