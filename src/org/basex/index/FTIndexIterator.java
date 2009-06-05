package org.basex.index;

/**
 * This interface provides methods for returning index results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class FTIndexIterator extends IndexIterator {
  /** Each token in the query has a number. */
  byte toknum;
  /** Number of results. */
  int size;

  /** Empty iterator. */
  static final FTIndexIterator EMP = new FTIndexIterator() {
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
  public void setTokenNum(final byte tn) {
    toknum = tn;
  }

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  public static FTIndexIterator union(final FTIndexIterator i1,
      final FTIndexIterator i2) {

    return new FTIndexIterator() {
      FTEntry r, s;
      int c;

      @Override
      public boolean more() {
        if(c <= 0) r = i1.more() ? i1.node() : null;
        if(c >= 0) s = i2.more() ? i2.node() : null;
        if(r != null && s != null) {
          c = r.pre() - s.pre();
          r.union(s, 0);
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
      public void setTokenNum(final byte tn) {
        i1.toknum = tn;
        i2.toknum = tn;
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
  public static FTIndexIterator intersect(final FTIndexIterator i1,
      final FTIndexIterator i2, final int w) {

    return new FTIndexIterator() {
      FTEntry r, s;

      @Override
      public boolean more() {
        int c = 0;
        while(true) {
          if(c <= 0) r = i1.more() ? i1.node() : null;
          if(c >= 0) s = i2.more() ? i2.node() : null;
          if(r == null || s == null) return false;
          c = r.pre() - s.pre();
          if(r.union(s, w)) return true;
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
      public void setTokenNum(final byte tn) {
        i1.toknum = tn;
        i2.toknum = tn;
      }
    };
  }
}
