package org.basex.index;

import org.basex.query.xpath.values.FTNode;
import org.basex.util.FTTokenizer;
import org.basex.util.IntArrayList;
import org.basex.util.Array;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IndexArrayIterator extends IndexIterator {
  /** Result array. */
  private int[] pres = null;
  /** Number of results. */
  private int size;
  /** Counter. */
  private int d = -1;
  /** Pre and Pos values. **/
  private int[][] ftdata = null;
  /** Each token in the query has a number. */
  private int toknum = 0;
  /** Token from query. */
  private FTTokenizer[] tok;

  /**
   * Constructor.
   */
  public IndexArrayIterator() {
    size = 0;
  }
  
  /**
   * Constructor.
   * @param res pres array
   */
  public IndexArrayIterator(final int[] res) {
    this(res, res.length);
  }
  
  /**
   * Constructor.
   * @param ftd ftdata array
   */
/*  public IndexArrayIterator(final int[][] ftd) {
    ftdata = ftd;
    size = ftd[0].length;
  }
*/
  /**
   * Constructor.
   * @param ftd ftdata array
   * @param s size
   */
/*  public IndexArrayIterator(final int[][] ftd, final int s) {
    ftdata = ftd;
    size = s;
  }
*/
  
  /**
   * Constructor.
   * @param res pres array
   * @param s number of results
   */
  public IndexArrayIterator(final int[] res, final int s) {
    pres = res;
    size = s;
  }
  
  
  /**
   * Constructor.
   * @param res pres array
   * @param c Flag for data converting
   */
  public IndexArrayIterator(final int[][] res, final boolean c) {
    this (res, res[0].length, c);
  }
  
  /**
   * Constructor.
   * @param res pres array
   * @param s size
   * @param c Flag for data converting
   */
  public IndexArrayIterator(final int[][] res, final int s, final boolean c) {
    if (c) convertData(res[0], res[1], s);
    else {
      ftdata = Array.finish(res, s);
      size = s;
    }
  }

  
  /**
   * Checks if more results are available.
   * @return boolean
   */
  public boolean more() {
    return ++d < size;
  }
  
  @Override
  public int next() {
    return (pres != null) ? pres[d] : ftdata[d][0];
  }

  /**
   * Get next ftnode.
   * @return ftnode next ftnode
   */
  public FTNode nextFTNode() {
    FTNode n = (pres != null) ? new FTNode(pres[d]) 
      : new FTNode(ftdata[d], toknum);
    if (tok != null) n.setToken(tok);
    return n;  
  }

  @Override
  public int size() {
    return size;
  }
  
  /**
   * Each token in the query has a number.
   * Used for visualization.
   * 
   * @param tn number of tokens
   */
  public void setTokenNum(final int tn) {
    toknum = tn;
  }
  
  /**
   * Set FTTokinzer.
   * @param token FTTokenizer
   */
  public void setToken(final FTTokenizer[] token) {
    tok = token;
  }
  
  /**
   * Getter FTTokenizer.
   * @return FTTokenizer
   */
  public FTTokenizer[] getToken() {
    return tok;
  }
  
  /**
   * Get number of tokens.
   * @return number of tokens
   */
  public int getTokenNum() {
    return toknum;
  }
  
  /**
   * Get fulltext data.
   * @return int [][] fulltext data
   */
  public int[][] getFTData() {
    return ftdata;
  }
  
  /**
   * Get current pre-value.
   * @param p int pointer
   * @return pre-value
   */
  public int getPre(final int p) {
    return (pres != null) ? pres[p] : ftdata[p][0];
  }
  
  /**
   * Converts the ftdata in the style:.
   * pre1, pos0, ..., posn, pre2, ....
   * 
   * @param r1 pre-values
   * @param r2 pos-values
   * @param siz number of pre/pos values
   */
  private void convertData(final int[] r1, final int[] r2, final int siz) {
    IntArrayList ia = new IntArrayList(siz);
    int i = 0, s;
    int[] t;
    while (i < siz) {
      s = i;
      while (i < siz && r1[s] == r1[i]) i++;
      t = new int[i - s + 1];
      t[0] = r1[s];
      System.arraycopy(r2, s, t, 1, i - s);
      ia.add(t);
    }
    ftdata = ia.finish();
    size = ftdata.length;
  }
}
