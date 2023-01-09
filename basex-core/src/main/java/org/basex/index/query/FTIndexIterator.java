package org.basex.index.query;

import org.basex.query.util.ft.*;
import org.basex.util.list.*;

/**
 * This interface provides methods for returning index results.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class FTIndexIterator implements IndexIterator {
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
   * @return next match (can be {@code null})
   */
  public abstract FTMatches matches();

  /**
   * Sets the position of the token in the query.
   * @param p query position
   */
  public abstract void pos(int p);

  /**
   * Merges two index iterators for unions.
   * @param iters index iterators to merge
   * @return new iterator
   */
  public static FTIndexIterator union(final FTIndexIterator... iters) {
    return new FTIndexIterator() {
      final IntList curr = new IntList();
      FTIndexIterator[] ir;

      @Override
      public boolean more() {
        final int il = iters.length;
        if(ir == null) {
          ir = new FTIndexIterator[il];
          for(int i = 0; i < il; i++) {
            ir[i] = iters[i].more() ? iters[i] : null;
          }
        } else {
          final int cs = curr.size();
          for(int c = 0; c < cs; c++) {
            final int i = curr.get(c);
            if(!ir[i].more()) ir[i] = null;
          }
        }
        int pre = Integer.MAX_VALUE;
        for(int i = 0; i < il; i++) {
          if(ir[i] == null) continue;
          final int p = ir[i].pre();
          if(pre < p) continue;
          if(pre > p) {
            pre = p;
            curr.reset();
          }
          curr.add(i);
        }
        return pre < Integer.MAX_VALUE;
      }

      @Override
      public FTMatches matches() {
        final FTMatches all = ir[curr.get(0)].matches();
        final int cs = curr.size();
        for(int c = 1; c < cs; c++) {
          for(final FTMatch match : ir[curr.get(c)].matches()) {
            final int s = match.list[0].start;
            int i = all.size();
            while(--i >= 0 && s < all.get(i).list[0].start);
            all.insert(++i, match);
          }
        }
        return all;
      }

      @Override
      public int pre() {
        return ir[curr.peek()].pre();
      }

      @Override
      public void pos(final int p) {
        for(final FTIndexIterator iter : iters) iter.pos(p);
      }

      @Override
      public synchronized int size() {
        int c = 0;
        for(final FTIndexIterator iter : iters) c += iter.size();
        return c;
      }

      @Override
      public String toString() {
        final StringBuilder sb = new StringBuilder().append('(');
        for(final FTIndexIterator iter : iters) {
          if(sb.length() > 1) sb.append(" | ");
          sb.append(iter);
        }
        return sb.append(')').toString();
      }
    };
  }

  /**
   * Merges two index iterators for intersections.
   * @param i1 first index iterator to merge
   * @param i2 second index iterator to merge
   * @param dis word distance (ignored if {@code 0})
   * @return index iterator
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
