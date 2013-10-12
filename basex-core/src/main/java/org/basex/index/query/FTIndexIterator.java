package org.basex.index.query;

import org.basex.data.*;

/**
 * This interface provides methods for returning index results.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FTIndexIterator extends IndexIterator {
  /** Empty iterator. */
  public static final FTIndexIterator FTEMPTY = new FTIndexIterator() {
    @Override
    public boolean more() { return false; }
    @Override
    public int pre() { return 0; }
    @Override
    public FTMatches matches() { return null; }
    @Override
    public int size() { return 0; }
    @Override
    public void pos(final int p) { }
  };

  /**
   * Returns the next match.
   * @return next match
   */
  public abstract FTMatches matches();

  /**
   * Sets the position of the token in the query.
   * @param p query position
   */
  public abstract void pos(final int p);

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @return IndexArrayIterator
   */
  public static FTIndexIterator union(final FTIndexIterator i1, final FTIndexIterator i2) {
    return new FTIndexIterator() {
      FTIndexIterator ii1, ii2, next;
      int diff;

      @Override
      public boolean more() {
        if(diff <= 0) ii1 = i1.more() ? i1 : null;
        if(diff >= 0) ii2 = i2.more() ? i2 : null;
        diff = ii1 != null ? ii2 != null ? ii1.pre() - ii2.pre() : -1 : 1;
        next = diff <= 0 ? ii1 : ii2;
        return next != null;
      }

      @Override
      public FTMatches matches() {
        final FTMatches all = next.matches();
        if(diff == 0) for(final FTMatch m : ii2.matches())
          all.add(m);
        return all;
      }

      @Override
      public int pre() {
        return next.pre();
      }

      @Override
      public void pos(final int p) {
        i1.pos(p);
        i2.pos(p);
      }

      @Override
      public synchronized int size() {
        return i1.size() + i2.size();
      }

      @Override
      public String toString() {
        return "(" + i1 + " | " + i2 + ')';
      }
    };
  }

  /**
   * Merges two index array iterators.
   * @param i1 first index array iterator to merge
   * @param i2 second index array iterator to merge
   * @param dis word distance. Ignored if {@code 0}
   * @return IndexArrayIterator
   */
  public static FTIndexIterator intersect(final FTIndexIterator i1, final FTIndexIterator i2,
      final int dis) {

    return new FTIndexIterator() {
      private FTIndexIterator ii1, ii2;
      private FTMatches all;

      @Override
      public boolean more() {
        int d = 0;
        while(true) {
          if(d <= 0) ii1 = i1.more() ? i1 : null;
          if(d >= 0) ii2 = i2.more() ? i2 : null;
          if(ii1 == null || ii2 == null) return false;
          d = ii1.pre() - ii2.pre();
          if(d != 0) continue;
          all = ii1.matches();
          final FTMatches all2 = ii2.matches();
          if(dis == 0) {
            for(final FTMatch m1 : all) {
              for(final FTMatch m2 : all2) m1.add(m2);
            }
            return true;
          } else if(all.phrase(all2, dis)) {
            return true;
          }
        }
      }

      @Override
      public FTMatches matches() {
        return all;
      }

      @Override
      public int pre() {
        return ii1.pre();
      }

      @Override
      public void pos(final int p) {
        i1.pos(p);
        i2.pos(p);
      }

      @Override
      public synchronized int size() {
        return Math.min(i1.size(), i2.size());
      }

      @Override
      public String toString() {
        return "(" + i1 + " & " + i2 + ')';
      }
    };
  }
}
