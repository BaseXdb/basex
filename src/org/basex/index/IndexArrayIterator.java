package org.basex.index;

import org.basex.ft.Tokenizer;

/**
 * This interface provides methods for returning index results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class IndexArrayIterator implements IndexIterator {
  /** Counter. */
  private int d = -1;
  /** Pre and Pos values. */
  private int[][] ftdata;
  /** Each token in the query has a number. */
  int toknum;
  /** Token from query. */
  Tokenizer[] tok;
  /** Number of results. */
  int size;

  /** Empty iterator. */
  public static final IndexArrayIterator EMP = new IndexArrayIterator(0) {
    @Override
    public boolean more() {
      return false;
    };

    @Override
    public int next() {
      return 0;
    };

    @Override
    public FTNode nextFTNode() {
      return new FTNode();
    };
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
   * [SG] only needed for non-iterative approach
   * @param res pre array
   * @param rs result size
   */
  public IndexArrayIterator(final int[][] res, final int rs) {
    ftdata = res;
    size = rs;
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

  /**
   * Sets the unique token number. Used for visualization.
   * @param tn number of tokens
   */
  public void setTokenNum(final int tn) {
    toknum = tn;
  }

  /**
   * Sets the tokenizer.
   * @param token FTTokenizer
   */
  public void setToken(final Tokenizer[] token) {
    tok = token;
  }

  /**
   * Merges two index array iterators.
   * @param iai1 first index array iterator to merge
   * @param iai2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  public static IndexArrayIterator union(final IndexArrayIterator iai1,
      final IndexArrayIterator iai2) {
    return IndexArrayIterator.union(iai1, iai2, 0);
  }

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @param w distance between two pos values
   * @return IndexArrayIterator
   */
  private static IndexArrayIterator union(final IndexArrayIterator i1,
      final IndexArrayIterator i2, final int w) {
    if(i1 == EMP) return i2;
    if(i2 == EMP) return i1;

    return new IndexArrayIterator(1) {
      final FTNode[] n = new FTNode[2];
      FTNode r;
      int c = -1;

      @Override
      public boolean more() {
        r = null;
        n[0] = c == 0 || c == -1 ? i1.more() ? i1.nextFTNode() : null : n[0];
        n[1] = c == 1 || c == -1 ? i2.more() ? i2.nextFTNode() : null : n[1];
        if(n[0] != null) {
          if(n[1] != null) {
            final int dis = n[0].getPre() - n[1].getPre();
            if(dis < 0) {
              r = n[0];
              c = 0;
            } else if(dis > 0) {
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
        } else if(n[1] != null) {
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
        i1.setTokenNum(t);
        i2.setTokenNum(t);
      }

      @Override
      public void setToken(final Tokenizer[] token) {
        i1.setToken(token);
        i2.setToken(token);
      }
    };
  }

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @param w distance between two pos values
   * @return IndexArrayIterator
   */
  public static IndexArrayIterator intersect(final IndexArrayIterator i1,
      final IndexArrayIterator i2, final int w) {

    if(i1 == EMP) return i2;
    if(i2 == EMP) return i1;

    return new IndexArrayIterator(1) {
      FTNode[] n = new FTNode[2];
      FTNode r;
      int c = -1;

      @Override
      public boolean more() {
        while(true) {
          n[0] = c == 0 || c == -1 ? i1.more() ? i1.nextFTNode() : null : n[0];
          n[1] = c == 1 || c == -1 ? i2.more() ? i2.nextFTNode() : null : n[1];
          if(n[0] == null || n[1] == null) return false;

          final int dis = n[0].getPre() - n[1].getPre();
          if(dis < 0) {
            c = 0;
          } else if(dis > 0) {
            c = 1;
          } else {
            c = -1;
            if(n[0].merge(n[1], w)) {
              n[0].reset();
              r = n[0];
              return true;
            }
          }
        }
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
        i1.setTokenNum(t);
        i2.setTokenNum(t);
      }

      @Override
      public void setToken(final Tokenizer[] token) {
        i1.setToken(token);
        i2.setToken(token);
      }
    };
  }
}
