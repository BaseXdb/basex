package org.basex.index;

import org.basex.util.Array;
import org.basex.util.IntArrayList;

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
  /** Pre and Pos values. */
  private int[][] ftdata = null;
  /** Each token in the query has a number. */
  protected int toknum = 0;
  /** Token from query. */
  protected FTTokenizer[] tok;
  
  /** Empty iterator. */
  public static final IndexArrayIterator EMP = new IndexArrayIterator(0) {
    @Override
    public boolean more() { return false; };
    @Override
    public int next() { return 0; };
    @Override
    public int size() { return 0; }; 
    @Override 
    public FTNode nextFTNodeFD() { return new FTNode(); };
    @Override 
    public FTNode nextFTNode() { return new FTNode(); };
  };
  
  
  /**
   * Constructor.
   * @param s size
   */
  public IndexArrayIterator(final int s) {
    size = s;
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

  @Override
  public FTNode nextFTNodeFD() {
    return new FTNode();
  }
  
  /**
   * Get next FTNode.
   * @return FTNode next FTNode
   */
  public FTNode nextFTNode() {
    if (d == size) return new FTNode();
    final FTNode n = pres != null ? new FTNode(pres[d])
      : (ftdata == null ? nextFTNodeFD() : 
        new FTNode(ftdata[d], toknum));
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
  
  /**
   * Get all FTData from index.
   * @return int[][] ftdata
   */
  public int[][] getFTData() {
    return ftdata;
  }
  
  /**
   * Merge to indexarrayiterator.
   * @param iai1 first indexarrayiterator to merge
   * @param iai2 second indexarrayiterator to merge
   * @return IndexArrayIterator
   */
  public static IndexArrayIterator merge(final IndexArrayIterator iai1, 
      final IndexArrayIterator iai2) {
    return new IndexArrayIterator(1) {
      FTNode[] n = new FTNode[2];
      FTNode r;
      int c = -1;
      
      @Override
      public boolean more() {
        r = null;
        n[0] = (c == 0 || c == -1) ? iai1.more() 
            ? iai1.nextFTNode() : null : n[0];
        n[1] = (c == 1 || c == -1) ? iai2.more() 
            ? iai2.nextFTNode() : null : n[1];
        if (n[0] != null) {
          if (n[1] != null) {
            final int dis = n[0].getPre() - n[1].getPre();
            if (dis < 0) {
              r = n[0];
              c = 0;
            } else if (dis > 0) {
              r = n[1];
              c = 1;
            } else {
              n[0].merge(n[1], 0);
              n[0].reset();
              r = n[0];
              c = -1;
            }
          } else {
            c = 0;
            r = n[0];
          }
        } else if (n[1] != null) {
          c = 1;
          r = n[1];
        }
        return r != null;
      }
      
      @Override
      public FTNode nextFTNode() {
        return r;
      }
      @Override
      public int next() {
        return r.getPre();
      }

      
      @Override
      public void setTokenNum(final int t) {
        iai1.setTokenNum(t);
        iai2.setTokenNum(t);
      }
    };
  }
}
