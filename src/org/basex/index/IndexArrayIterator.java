package org.basex.index;

import org.basex.util.IntArrayList;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IndexArrayIterator implements IndexIterator {
  /** Number of results. */
  private int size;
  /** Counter. */
  private int d = -1;
  /** Pre and Pos values. */
  private int[][] ftdata;
  /** Each token in the query has a number. */
  protected int toknum;
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
   * @param res pre array
   * @param c Flag for data converting
   */
  public IndexArrayIterator(final int[][] res, final boolean c) {
    final int s = res[0].length;
    if (c) convertData(res[0], res[1], s);
    else {
      ftdata = res;
      size = s;
    }
  }

  /**
   * Checks if more results are available.
   * @return result
   */
  public boolean more() {
    return ++d < size;
  }

  /**
   * Returns the next FTNode.
   * @return FTNode next FTNode
   */
  public FTNode nextFTNode() {
    final FTNode n = ftdata == null ? new FTNode() :
      new FTNode(ftdata[d], toknum);
    if(tok != null) n.setToken(tok);
    return n;
  }

  public int next() {
    return ftdata[d][0];
  }

  public int size() {
    return size;
  }

  /**
   * Sets the unique token number.
   * Used for visualization.
   * @param tn number of tokens
   */
  public void setTokenNum(final int tn) {
    toknum = tn;
  }

  /**
   * Sets the tokenizer.
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
    ftdata = ia.list;
    size = ia.size;
  }

  /**
   * Merges two index array iterators.
   * @param iai1 first index array iterator to merge
   * @param iai2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  public static IndexArrayIterator merge(final IndexArrayIterator iai1,
      final IndexArrayIterator iai2) {
    return IndexArrayIterator.merge(iai1, iai2, 0);
  }

  /**
   * Merges two index array iterators.
   * @param iai1 first index array iterator to merge
   * @param iai2 second index array iterator to merge
   * @param w distance between two pos values
   * @return IndexArrayIterator
   */
  private static IndexArrayIterator merge(final IndexArrayIterator iai1,
      final IndexArrayIterator iai2, final int w) {
    if (iai1 == EMP) return iai2;
    if (iai2 == EMP) return iai1;

    return new IndexArrayIterator(1) {
      FTNode[] n = new FTNode[2];
      FTNode r;
      int c = -1;

      @Override
      public boolean more() {
        r = null;
        n[0] = c == 0 || c == -1 ? iai1.more()
            ? iai1.nextFTNode() : null : n[0];
        n[1] = c == 1 || c == -1 ? iai2.more()
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
              n[0].merge(n[1], w);
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

      @Override
      public void setToken(final FTTokenizer[] token) {
        iai1.setToken(token);
        iai2.setToken(token);
      }
    };
  }

  /**
   * Merges two index array iterators.
   * @param iai1 first index array iterator to merge
   * @param iai2 second index array iterator to merge
   * @param w distance between two pos values
   * @return IndexArrayIterator
   */
  public static IndexArrayIterator and(final IndexArrayIterator iai1,
      final IndexArrayIterator iai2, final int w) {
    if (iai1 == EMP) return iai2;
    if (iai2 == EMP) return iai1;

    return new IndexArrayIterator(1) {
      FTNode[] n = new FTNode[2];
      FTNode r;
      int c = -1;

      @Override
      public boolean more() {
        r = null;
        n[0] = c == 0 || c == -1 ? iai1.more()
            ? iai1.nextFTNode() : null : n[0];
        n[1] = c == 1 || c == -1 ? iai2.more()
            ? iai2.nextFTNode() : null : n[1];
        if (n[0] != null && n[1] != null) {
          final int dis = n[0].getPre() - n[1].getPre();
          if (dis < 0) {
            c = 0;
            return more();
          } else if (dis > 0) {
            c = 1;
            return more();
          } else {
            c = -1;
            if (n[0].merge(n[1], w)) {
              n[0].reset();
              r = n[0];
              return true;
            } else {
              return more();
            }
          }
        }
        return false;
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

      @Override
      public void setToken(final FTTokenizer[] token) {
        iai1.setToken(token);
        iai2.setToken(token);
      }
    };
  }
}
