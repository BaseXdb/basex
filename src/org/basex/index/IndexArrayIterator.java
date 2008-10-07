package org.basex.index;

import org.basex.util.Array;
import org.basex.util.IntArrayList;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IndexArrayIterator extends IndexIterator {
  /** Result array. */
  private int[] pres = null;
  /** Number of results. */
  private int size;
  /** Counter. */
  private int d = -1;
  /** Pre and Pos values. */
  private int[][] ftdata = null;
  /** Each token in the query has a number. */
  private int toknum = 0;
  /** Token from query. */
  private FTTokenizer[] tok;

  /**
   * Constructor.
   * @param res pres array
   */
  public IndexArrayIterator(final int[] res) {
    this(res, res.length);
  }

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

  @Override
  public boolean more() {
    return ++d < size;
  }

  @Override
  public int next() {
    return pres != null ? pres[d] : ftdata[d][0];
  }

  /**
   * Get next ftnode.
   * @return ftnode next ftnode
   */
  public FTNode nextFTNode() {
    final FTNode n = pres != null ? new FTNode(pres[d])
      : new FTNode(ftdata[d], toknum);
    if(tok != null) n.setToken(tok);
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
   * Converts the ftdata in the style:.
   * pre1, pos0, ..., posn, pre2, ....
   *
   * @param r1 pre-values
   * @param r2 pos-values
   * @param siz number of pre/pos values
   */
  private void convertData(final int[] r1, final int[] r2, final int siz) {
    final IntArrayList ia = new IntArrayList(siz);
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
