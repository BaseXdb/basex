package org.basex.index;

import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.query.ft.Scoring;

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
    @Override
    public double score() { return -1; };
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
   * Get number of indexed results.
   * @return number of indexed results
   */
  public final int indexSize() {
    return size;
  }
  
  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  static FTIndexIterator union(final FTIndexIterator i1,
      final FTIndexIterator i2) {

    return new FTIndexIterator() {
      FTIndexIterator n, r, s;
      int c;

      @Override
      public boolean more() {
        if(c <= 0) r = i1.more() ? i1 : null;
        if(c >= 0) s = i2.more() ? i2 : null;
        c = r != null && s != null ? r.next() - s.next() : r != null ? -1 : 1;
        n = c <= 0 ? r : s;
        return n != null;
      }

      @Override
      public FTMatches matches() {
        final FTMatches m = n.matches();
        if(c == 0) for(final FTMatch sm : s.matches()) m.add(sm);
        return m;
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
      
      @Override 
      public double score() {
        return Math.max(i1.score(), i2.score());
      }

    };
  }

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  public static FTIndexIterator phrase(final FTIndexIterator i1,
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
      
      @Override 
      public double score() {
        return Scoring.phrase(i1.score(), i2.score());
      }
    };
  }
}
