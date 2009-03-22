package org.basex.data;

import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * This class provides a container for query fulltext positions
 * for any visualization.
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
   * Add a ftand color result.
   * @param c int[] ftand color result
   */
  public void addFTAndCol(final byte[] c) {
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

    // find insertion position for new values or append them at the end
    final int i = size > 0 && prepos[size - 1][0] > pp[0] ? find(pp[0]) : 0;
    if(i >= 0) {
      prepos[size] = pp;
      poi[size++] = p;
    } else {
      add(-i - 1, pp, p);
    }
  }

  /**
   * Adds pp and p at position i.
   * @param i int position where to add
   * @param pp int[] pre and position values
   * @param p int[] pointer values
   */
  private void add(final int i, final int[] pp, final int[] p) {
    if(prepos[i][0] == pp[0]) {
      // check if equal or merge
      if (Array.eq(pp, prepos[i])) return;
      final int[] tpp = new int[pp.length + prepos[i].length - 1];
      final int[] tp = new int[tpp.length];
      tpp[0] = pp[0];
      tp[0] = Math.max(p[0], poi[i][0]);
      int p0 = 1, p1 = 1;
      for (int j = 1; j < tpp.length; j++) {
        if (p1 >= pp.length || p0 < prepos[i].length &&
            prepos[i][p0] < pp[p1]) {
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

    if (i < size) {
      Array.move(prepos, i, 1, size - i);
      Array.move(poi, i, 1, size - i);
    }
    prepos[i] = pp;
    poi[i] = p;
    size++;
  }

  /**
   * Gets fulltext data from the container.
   * If no data is stored for a pre value,
   * null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param pre int pre value
   * @return int[2][n] fulltext data or null
   */
  private int find(final int pre) {
    // binary search
    int l = 0;
    int h = size - 1;
    while(l <= h) {
      final int m = (l + h) >>> 1;
      final int c = prepos[m][0] - pre;
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -l - 1;
  }

  /**
   * Gets fulltext data from the container.
   * If no data is stored for a pre value,
   * null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param pre int pre value
   * @return int[2][n] fulltext data or null
   */
  public int[][] get(final int pre) {
    final int p = find(pre);
    return p >= 0 ? getFTData(p) : null;
  }

  /**
   * Converts data to integer arrays.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin]
   *
   * @param i pointer on the data
   * @return int[2][n] fulltext data or null
   */
  private int[][] getFTData(final int i) {
    final int[][] r = new int[2][prepos[i].length - 1];
    System.arraycopy(prepos[i], 1, r[0], 0, r[0].length);
    System.arraycopy(poi[i], 1, r[1], 0, r[1].length);
    return r;
  }

  /**
   * Removes ftdata for this node.
   *
   * @param pre int pre value
   */
  public void remove(final int pre) {
    if (size == 0) return;
    int l = 0, r = size;
    // binary search
    while(l < r) {
      final int m = l + (r - l) / 2;
      final int c = prepos[m][0] - pre;
      if(c == 0) {
        removeFTData(m);
        return;
      } else if(c < 0) l = m + 1;
      else r = m - 1;
    }
    if(r != size && l == r && prepos[l][0] == pre) removeFTData(l);
  }

  /**
   * Removes ftdata for this node.
   * @param i pointer on node entry
   */
  private void removeFTData(final int i) {
    System.arraycopy(prepos, i, prepos, i, size - i - 1);
    System.arraycopy(poi, i, poi, i, size - i - 1);
    size--;
  }

  /**
   * Converts data from sequential ftcontains processing.
   *
   * @param d IntList[] ftcontains data
   * @param pre int pre value
   * @param div int div to add for each pointer
   */
  public void addConvSeqData(final IntList[] d, final int pre, final int div) {
    if (d.length == 0) return;

    int[] pp;
    int[] p;
    pp = new int[d[0].size + 1];
    p = new int[pp.length];
    pp[0] = pre;
    final int[] tmp = d[0].finish();
    System.arraycopy(tmp, 0, pp, 1, tmp.length);
    for (int i = 0; i < p.length; i++) p[i] = div;

    for(int c = 1; c < d.length; c++) {
      int p0 = 1, p1 = 0, p2 = 1;
      int[] ppn = new int[d[c].size + pp.length];
      int[] pn = new int[d[c].size + p.length];

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
   * Compares fulltext data for equality.
   * @param ft reference to compare to
   * @return boolean same()
   */
  boolean same(final FTPosData ft) {
    if(size != ft.size) return false;
    for(int i = 0; i < size; i++) {
      if(!Array.eq(prepos[i], ft.prepos[i])
          || Array.eq(poi[i], ft.poi[i])) return false;
    }
    return true;
  }
}
