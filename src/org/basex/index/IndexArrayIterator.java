package org.basex.index;

import org.basex.ft.Tokenizer;

/**
 * This interface provides methods for returning index results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class IndexArrayIterator extends IndexIterator {
  /** Each token in the query has a number. */
  int toknum;
  /** Token from query. */
  Tokenizer[] tok;
  /** Number of results. */
  int size;

  /** Empty iterator. */
  static final IndexArrayIterator EMP = new IndexArrayIterator() {
    @Override
    public boolean more() { return false; };
    @Override
    public int next() { return 0; };
    @Override
    public FTEntry node() { return null; };
  };

  /**
   * Returns the next node.
   * @return FTNode next node
   */
  public abstract FTEntry node();

  /**
   * Sets the unique token number. Used for visualization.
   * @param tn number of tokens
   */
  public void setTokenNum(final int tn) {
    toknum = tn;
  }

  /**
   * Sets the tokenizer.
   * @param token tokenizer
   */
  public void setToken(final Tokenizer[] token) {
    tok = token;
  }

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  static IndexArrayIterator union(final IndexArrayIterator i1,
      final IndexArrayIterator i2) {

    return new IndexArrayIterator() {
      FTEntry r, s;
      int c;

      @Override
      public boolean more() {
        if(c <= 0) r = i1.more() ? i1.node() : null;
        if(c >= 0) s = i2.more() ? i2.node() : null;
        if(r != null && s != null) {
          c = r.pre() - s.pre();
          if(c == 0) {
            r.union(s, 0);
            r.reset();
          }
        } else {
          c = r != null ? -1 : 1;
        }
        return node() != null;
      }

      @Override
      public FTEntry node() {
        return c <= 0 ? r : s;
      }

      @Override
      public int next() {
        return r.pre();
      }

      @Override
      public void setTokenNum(final int tn) {
        i1.setTokenNum(tn);
        i2.setTokenNum(tn);
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

    return new IndexArrayIterator() {
      FTEntry r, s;

      @Override
      public boolean more() {
        int c = 0;
        while(true) {
          if(c <= 0) r = i1.more() ? i1.node() : null;
          if(c >= 0) s = i2.more() ? i2.node() : null;
          if(r == null || s == null) return false;

          c = r.pre() - s.pre();
          if(c == 0 && r.union(s, w)) {
            r.reset();
            return true;
          }
        }
      }

      @Override
      public FTEntry node() {
        return r;
      }

      @Override
      public int next() {
        return r.pre();
      }

      @Override
      public void setTokenNum(final int tn) {
        i1.setTokenNum(tn);
        i2.setTokenNum(tn);
      }

      @Override
      public void setToken(final Tokenizer[] token) {
        i1.setToken(token);
        i2.setToken(token);
      }
    };
  }
}
