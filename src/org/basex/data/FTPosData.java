package org.basex.data;

import org.basex.index.FTEntry;
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
  /** Pre values. */
  private int[] pre = new int[1];
  /** Position values. */
  private int[][] pos = new int[1][];
  /** Position pointers. */
  private int[][] poi = new int[1][];
  /** Number of pre values. */
  private int size;

  /**
   * Adds position data. Sequential variant.
   * @param p pre value
   * @param ps positions
   * @param tn token number
   */
  public void add(final int p, final IntList[] ps, final int tn) {
    int[] pp = ps[0].finish();
    int[] pi = new int[pp.length];
    for(int i = 0; i < pp.length; i++) pi[i] = tn;

    for(int c = 1; c < ps.length; c++) {
      final int prs = pp.length;
      final int pss = ps[c].size;
      final int[] tp = new int[prs + pss];
      final int[] ti = new int[tp.length];
      for(int i = 0, p0 = 0, p1 = 0; i < tp.length; i++) {
        final boolean s = p0 == prs || p1 < pss && ps[c].list[p1] < pp[p0];
        tp[i] = s ? ps[c].list[p1++] : pp[p0];
        ti[i] = s ? c + tn : pi[p0++];
      }
      pp = tp;
      pi = ti;
    }
    add(p, pp, pi);
  }

  /**
   * Adds a full-text entry. Index variant.
   * @param fte full-text entry reference.
   */
  public void add(final FTEntry fte) {
    add(fte.pre, fte.pos.finish(), fte.poi.finish());
  }
  
  /**
   * Removes position data for the specified node.
   * Called by And expression.
   * @param p int pre value
   */
  public void remove(final int p) {
    final int i = find(p);
    if(i < 0) return;

    System.arraycopy(pre, i, pre, i, size - i - 1);
    System.arraycopy(pos, i, pos, i, size - i - 1);
    System.arraycopy(poi, i, poi, i, size - i - 1);
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
   * Gets full-text data from the container.
   * If no data is stored for a pre value, null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param p int pre value
   * @return int[2][n] full-text data or null
   */
  public int[][] get(final int p) {
    final int i = find(p);
    return i < 0 ? null : new int[][] { pos[i], poi[i] };
  }

  /**
   * Compares full-text data for equality.
   * @param ft reference to compare to
   * @return boolean same()
   */
  boolean same(final FTPosData ft) {
    if(size != ft.size) return false;
    for(int i = 0; i < size; i++) {
      if(pre[i] != ft.pre[i] || !Array.eq(pos[i], ft.pos[i]) ||
          !Array.eq(poi[i], ft.poi[i])) return false;
    }
    return true;
  }

  /**
   * Adds or merges position arrays.
   * pp : [pre, pos0, ..., posn]
   * p : [poiMax, poi0, ..., poin]
   * @param p pre value
   * @param pp int[] pre and positions
   * @param pi int[] pointer
   */
  private void add(final int p, final int[] pp, final int[] pi) {
    if(size == pre.length) {
      pre = Array.extend(pre);
      pos = Array.extend(pos);
      poi = Array.extend(poi);
    }

    int c = find(p);
    if(c < 0) { 
      // new pre value...
      c = -c - 1;
      if(c < size) {
        Array.move(pre, c, 1, size - c);
        Array.move(pos, c, 1, size - c);
        Array.move(poi, c, 1, size - c);
      }
      pre[c] = p;
      pos[c] = pp;
      poi[c] = pi;
      size++;
    } else {
      if(Array.eq(pp, pos[c])) return;

      // merge entries with the same pre value
      final int prs = pp.length;
      final int pss = pos[c].length;
      final int[] tp = new int[prs + pss];
      final int[] ti = new int[tp.length];
      for(int i = 0, p0 = 0, p1 = 0; i < tp.length; i++) {
        final boolean s = p0 == prs || p1 < pss && pos[c][p1] < pp[p0];
        tp[i] = s ? pos[c][p1] : pp[p0];
        ti[i] = s ? poi[c][p1++] : pi[p0++];
      }
      pre[c] = p;
      pos[c] = tp;
      poi[c] = ti;
    }
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
      final int c = pre[m] - p;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }
}
