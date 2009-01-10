package org.basex.query.xquery;

import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * This class provides a container for XQuery fulltext data 
 * for any visualization.
 * The data is stored as follows:
 * prepos[i][pre, pos0, ..., posn]
 * poi[i][poiMax, poi0, ..., poin]
 * For each pos values, theres a poi value stored. poiMax is the
 * max value of all pos0, ..., poin
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */

public final class XQFTVisData {
  /** Number of pre values. */
  public static int size = 0;
  /** Prevalues and its posvalues. */
  public static int[][] prepos = new int[1][];
  /** Pointer values for the pos values.*/
  public static int[][] poi = new int[1][];
  
  /**
   * Constructor.
   */
  private XQFTVisData() {
    init();
  }
  
  /**
   * Init container for new results.
   */
  public static void init() {
    size = 0;
    prepos = new int[1][];
    poi = new int[1][];
  }
  
  /**
   * Add a node.
   * pp : [pre, pos0, ..., posn]
   * p : [poiMax, poi0, ..., poin]
   * @param pp int[] pre and positions
   * @param p int[] pointer
   */
  public static void add(final int[] pp, final int[] p) {
    if (pp == null || p == null) return;
    if (size + 1 > prepos.length) {
      prepos = Array.extend(prepos);
      poi = Array.extend(poi);
    }
    
    int i = 0;
    while (i < size) {
      if (prepos[i][0] < pp[0]) i++;
      else {
        add(i, pp, p);
        return;
      }
    }
    
    prepos[i] = pp;
    poi[i] = p;
    size++;
  }
  
  
  /**
   * Add pp and p at position i.
   * @param i int position where to add
   * @param pp int[] pre and position values
   * @param p int[] pointer values
   */
  private static void add(final int i, final int[] pp, final int[] p) {
    if(prepos[i][0] == pp[0]) {
      // check if equal or merge
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
   * Get fulltextdata from the containser.
   * If no data is stored for a pre value,
   * null is returned.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin] 
   * 
   * @param pre int pre value
   * @return int[2][n] fulltext data or null
   */
  public static int[][] get(final int pre) {
    if (size == 0) return null;
    int l = 0, r = size;
    // binary search
    while(l < r) {
      final int m = l + (r - l) / 2;
      final int c = prepos[m][0] - pre;
      if(c == 0)  return getFTData(m);
      else if(c < 0) l = m + 1;
      else r = m - 1;
    }
    return r != size && l == r && prepos[l][0] == pre ? getFTData(l) : null;
  }

  /**
   * Convert data to.
   * int[0] : [pos0, ..., posn]
   * int[1] : [poi0, ..., poin] 
   * 
   * @param i pointer on the data
   * @return int[2][n] fulltext data or null
   */
  private static int[][] getFTData(final int i) {
    final int[][] r = new int[2][prepos[i].length - 1];
    System.arraycopy(prepos[i], 1, r[0], 0, r[0].length);
    System.arraycopy(poi[i], 1, r[1], 0, r[1].length);
    return r;
  }
  
  /**
   * Converts data from sequential ftcontanins processing.
   * 
   * @param d IntList[] ftcontains data
   * @param pre int pre value
   */
  public static void addConvSeqData(final IntList[] d, final int pre) {
    int[] pp;
    int[] p;
    pp = new int[d[0].size + 1];
    p = new int[pp.length];
    pp[0] = pre;
    final int[] tmp = d[0].finish();
    System.arraycopy(tmp, 0, pp, 1, tmp.length);
    for (int i = 0; i < p.length; i++) p[i] = 1;
    
    for(int c = 1; c < d.length; c++) {
      int p0 = 1, p1 = 0, p2 = 1;
      int[] ppn = new int[d[c].size + pp.length];
      int[] pn = new int[d[c].size + p.length];
      
      ppn[0] = pp[0];
      pn[0] = p[0];
      
      while(p0 < pp.length && p1 < d[c].size) {
        if(d[c].list[p1] < pp[p0]) {
          ppn[p2] = d[c].list[p1++];
          pn[p2++] = c + 1;
        } else {
          ppn[p2] = pp[p0];
          pn[p2++] = p[p0++];    
        }
      }
      while(p1 < d[c].size) {
        ppn[p2] = d[c].list[p1++];
        pn[p2++] = c + 1;
      }    
      while(p0 < pp.length) {
        ppn[p2] = pp[p0];
        pn[p2++] = pp[p0++];
      }    
      
      pp = ppn;
      p = pn;
      c++;
    }
    add(pp, p);
  }
}
