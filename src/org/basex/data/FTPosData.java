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

  /**
   * Adds position data. Sequential variant.
   * @param d IntList[] ftcontains data
   * @param pre pre value
   * @param div div to add for each pointer
   */
  public void add(final IntList[] d, final int pre, final int div) {
    int[] pp = new int[d[0].size + 1];
    int[] p = new int[pp.length];
    pp[0] = pre;
    System.arraycopy(d[0].list, 0, pp, 1, d[0].size);
    for(int i = 0; i < pp.length; i++) p[i] = div;

    for(int c = 1; c < d.length; c++) {
      // [SG] doc('wiki1')//*[text() ftcontains 'a' ftand 'the' ftand 'of']
      if(d[c] == null) continue;

      int p0 = 1, p1 = 0, p2 = 1;
      final int[] ppn = new int[d[c].size + pp.length];
      final int[] pn = new int[d[c].size + p.length];

      ppn[0] = pp[0];
      pn[0] = Math.max(c + div, p[0]);

      while(p0 < pp.length && p1 < d[c].size) {
        if(d[c].list[p1] < pp[p0]) {
          ppn[p2] = d[c].list[p1++];
          pn[p2++] = c + div;
        } else {
          ppn[p2] = pp[p0];
          pn[p2++] = p[p0++];
        }
      }
      while(p1 < d[c].size) {
        ppn[p2] = d[c].list[p1++];
        pn[p2++] = c + div;
      }
      while(p0 < pp.length) {
        ppn[p2] = pp[p0];
        pn[p2++] = p[p0++];
      }
      pp = ppn;
      p = pn;
      c++;
    }
    add(pp, p);
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
   * Adds a node.
   * pp : [pre, pos0, ..., posn]
   * p : [poiMax, poi0, ..., poin]
   * @param pp int[] pre and positions
   * @param p int[] pointer
   */
  public void add(final int[] pp, final int[] p) {
    if(size == prepos.length) {
      prepos = Array.extend(prepos);
      poi = Array.extend(poi);
    }

    final int i = find(pp[0]);
    add(i >= 0 ? i : -i - 1, pp, p);
    
    /* find insertion position for new values or append them at the end
    if(size == 0) {
      prepos[size] = pp;
      poi[size++] = p;      
    } else if (prepos[size - 1][0] == pp[0]) {
      add(size - 1, pp, p);
    } else {
      final int i = find(pp[0]);
      add(i >= 0 ? i : -i - 1, pp, p);
    }*/
  }

  /**
   * Adds pp and p at position i.
   * @param i int position where to add
   * @param pp int[] pre and position values
   * @param p int[] pointer values
   */
  private void add(final int i, final int[] pp, final int[] p) {
    if(prepos[i] != null && prepos[i][0] == pp[0]) {
      // check if equal or merge
      if(Array.eq(pp, prepos[i])) return;
      final int[] tpp = new int[pp.length + prepos[i].length - 1];
      final int[] tp = new int[tpp.length];
      tpp[0] = pp[0];
      tp[0] = Math.max(p[0], poi[i][0]);
      int p0 = 1, p1 = 1;
      for(int j = 1; j < tpp.length; j++) {
        if(p1 >= pp.length || p0 < prepos[i].length && prepos[i][p0] < pp[p1]) {
          tpp[j] = prepos[i][p0];
          tp[j] = poi[i][p0++];
        } else {
          tpp[j] = pp[p1];
          tp[j] = p[p1++];
        }
      }
      prepos[i] = tpp;
      poi[i] = tp;
      return;
    }

    if(i < size) {
      Array.move(prepos, i, 1, size - i);
      Array.move(poi, i, 1, size - i);
    }
    prepos[i] = pp;
    poi[i] = p;
    size++;
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
