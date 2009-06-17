package org.basex.index;

import org.basex.data.FTMatches;

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
    public FTMatches matches() { return null; };
  };

  /**
   * Returns the next match.
   * @return next match
   */
  public abstract FTMatches matches();

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
      FTIndexIterator n, r, s;
      int c;

      @Override
      public boolean more() {
        if(c <= 0) r = i1.more() ? i1 : null;
        if(c >= 0) s = i2.more() ? i2 : null;
        if(r != null && s != null) {
          c = r.next() - s.next();
          if(c == 0) r.matches().or(s.matches());
        } else {
          c = r != null ? -1 : 1;
        }
        n = c <= 0 ? r : s;
        return n != null;
      }

      @Override
      public FTMatches matches() {
        return n.matches();
      }

      @Override
      public int next() {
        return n.next();
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
   * @return IndexArrayIterator
   */
  public static FTIndexIterator intersect(final FTIndexIterator i1,
      final FTIndexIterator i2) {

    return new FTIndexIterator() {
      FTIndexIterator r, s;

      @Override
      public boolean more() {
        int c = 0;
        while(true) {
          if(c <= 0) r = i1.more() ? i1 : null;
          if(c >= 0) s = i2.more() ? i2 : null;
          if(r == null || s == null) return false;
          c = r.next() - s.next();
          if(c == 0 && r.matches().phrase(s.matches())) return true;
        }
      }

      @Override
      public FTMatches matches() {
        return r.matches();
      }

      @Override
      public int next() {
        return r.next();
      }

      @Override
      public void setTokenNum(final byte tn) {
        i1.toknum = tn;
        i2.toknum = tn;
      }
    };
  }
}
