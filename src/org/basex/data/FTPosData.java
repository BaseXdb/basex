package org.basex.data;

import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * This class provides a container for query full-text positions,
 * which is evaluated by the visualizations.
 * 
 * The data is stored as follows:
 * prepos[i][pre, pos0, ..., posn]
 * poi[i][poiMax, poi0, ..., poin]
 * For each pos values, theres a poi value stored. poiMax is the
 * max value of all pos0, ..., poin
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTPosData {
  /** Array for ftand colors. */
  public TokenList col = new TokenList();
  /** Pre values and its pos values. */
  private int[][] prepos = new int[1][];
  /** Pointer values for the pos values.*/
  private int[][] poi = new int[1][];
  /** Number of pre values. */
  private int size;

  // [SG] what is poiMax needed for?
  
  /**
   * Adds position data. Sequential variant.
   * @param pre pre value
   * @param pos positions
   * @param ftc token counter
   */
  public void add(final int pre, final IntList[] pos, final int ftc) {
    int[] pp = new int[pos[0].size + 1];
    int[] pi = new int[pp.length];
    pp[0] = pre;
    System.arraycopy(pos[0].list, 0, pp, 1, pos[0].size);
    for(int i = 0; i < pp.length; i++) pi[i] = ftc;

    for(int c = 1; c < pos.length; c++) {
      final int prs = pp.length;
      final int pss = pos[c].size;
      final int[] tp = new int[prs + pss];
      final int[] ti = new int[tp.length];

      tp[0] = pre;
      ti[0] = ftc + c;
      for(int p = 1, p0 = 1, p1 = 0; p < tp.length; p++) {
        final boolean s = p0 == prs || p1 < pss && pos[c].list[p1] < pp[p0];
        tp[p] = s ? pos[c].list[p1++] : pp[p0];
        ti[p] = s ? c + ftc : pi[p0++];
      }
      pp = tp;
      pi = ti;
    }
    add(pp, pi);
  }

  /**
   * Removes position data for the specified node.
   * Called by And expression.
   * @param pre int pre value
   */
  public void remove(final int pre) {
    final int p = find(pre);
    if(p < 0) return;

    System.arraycopy(prepos, p, prepos, p, size - p - 1);
    System.arraycopy(poi, p, poi, p, size - p - 1);
    size--;
  }

  /**
   * Adds an ftand color result. Index variant (FTIntersection).
   * @param c int[] ftand color result
   */
  public void addCol(final byte[] c) {
    col.add(c);
  }
  
  /**
   * Adds or merges position arrays.
   * pp : [pre, pos0, ..., posn]
   * p : [poiMax, poi0, ..., poin]
   * @param pp int[] pre and positions
   * @param pi int[] pointer
   */
  public void add(final int[] pp, final int[] pi) {
    if(size == prepos.length) {
      prepos = Array.extend(prepos);
      poi = Array.extend(poi);
    }

    int c = find(pp[0]);
    if(c < 0) { 
      // new pre value...
      c = -c - 1;
      if(c < size) {
        Array.move(prepos, c, 1, size - c);
        Array.move(poi, c, 1, size - c);
      }
      prepos[c] = pp;
      poi[c] = pi;
      size++;
    } else {
      if(Array.eq(pp, prepos[c])) return;

      // merge entries with the same pre value
      final int prs = pp.length;
      final int pss = prepos[c].length;
      final int[] tp = new int[prs + pss - 1];
      final int[] ti = new int[tp.length];

      tp[0] = pp[0];
      ti[0] = Math.max(pi[0], poi[c][0]);
      for(int p = 1, p0 = 1, p1 = 1; p < tp.length; p++) {
        final boolean s = p1 == prs || p0 < pss && prepos[c][p0] < pp[p1];
        tp[p] = s ? prepos[c][p0] : pp[p1];
        ti[p]  = s ? poi[c][p0++] : pi[p1++];
      }
      prepos[c] = tp;
      poi[c] = ti;
    }
  }

  /**
   * Returns the position of the specified pre value.
   * @param pre int pre value
   * @return position or negative insertion value - 1
   */
  private int find(final int pre) {
    // binary search
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int c = prepos[m][0] - pre;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }

  /**
   * Gets full-text data from the container.
   * If no data is stored for a pre value, null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param pre int pre value
   * @return int[2][n] full-text data or null
   */
  public int[][] get(final int pre) {
    final int p = find(pre);
    if(p < 0) return null;
    
    final int[][] r = new int[2][prepos[p].length - 1];
    System.arraycopy(prepos[p], 1, r[0], 0, r[0].length);
    System.arraycopy(poi[p], 1, r[1], 0, r[1].length);
    return r;
  }

  /**
   * Compares full-text data for equality.
   * @param ft reference to compare to
   * @return boolean same()
   */
  boolean same(final FTPosData ft) {
    if(size != ft.size) return false;
    for(int i = 0; i < size; i++) {
      if(!Array.eq(prepos[i], ft.prepos[i]) || !Array.eq(poi[i], ft.poi[i]))
        return false;
    }
    return true;
  }
}
