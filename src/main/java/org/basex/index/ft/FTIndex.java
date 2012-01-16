package org.basex.index.ft;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.index.Index;
import org.basex.index.IndexCache;
import org.basex.io.random.DataAccess;
import org.basex.util.list.IntList;

/**
 * This abstract class defines methods for the available full-text indexes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class FTIndex implements Index {
  /** Cache for number of hits and data reference per token. */
  final IndexCache cache = new IndexCache();
  /** Data reference. */
  final Data data;
  /** Scoring mode. 1 = document based, 2 = text-node based .*/
  final int scm;
  /** Minimum scoring value. */
  final double max;
  /** Minimum scoring value. */
  final double min;

  /**
   * Returns a new full-text index instance.
   * @param d data reference
   * @param wild wildcard index
   * @return index instance
   * @throws IOException IOException
   */
  public static FTIndex get(final Data d, final boolean wild)
      throws IOException {
    return wild ? new FTTrie(d) : new FTFuzzy(d);
  }

  /**
   * Constructor.
   * @param d data reference
   */
  FTIndex(final Data d) {
    data = d;
    scm = d.meta.scoring;
    max = Math.log(data.meta.maxscore + 1);
    min = Math.log(data.meta.minscore - 1);
  }

  /**
   * Returns an iterator for an index entry.
   * @param off offset on entries
   * @param size number of pre/pos entries
   * @param da data source
   * @param fast fast evaluation
   * @return iterator
   */
  final synchronized FTIndexIterator iter(final long off, final int size,
      final DataAccess da, final boolean fast) {

    // cache results
    da.cursor(off);
    final IntList vals = new IntList();
    for(int c = 0, lp = 0; c < size;) {
      if(lp == 0) {
        if(scm > 0) vals.add(da.readNum());
        lp = da.readNum();
        vals.add(lp);
      }
      final int pr = lp;
      vals.add(da.readNum());
      while(++c < size) {
        lp = da.readNum();
        vals.add(lp);
        if(lp != pr) break;
        vals.add(da.readNum());
      }
    }

    return new FTIndexIterator() {
      final FTMatches all = new FTMatches(toknum);
      int c, pre, lpre;
      double sc = -1;

      @Override
      public synchronized boolean more() {
        if(c == vals.size()) return false;
        if(lpre == 0) {
          if(scm > 0) sc = (Math.log(vals.get(c++)) - min) / (max - min);
          lpre = vals.get(c++);
        }
        pre = lpre;

        all.reset(toknum);
        all.or(vals.get(c++));

        while(c < vals.size() && (lpre = vals.get(c++)) == pre) {
          final int n = vals.get(c++);
          if(!fast) all.or(n);
        }
        return true;
      }

      @Override
      public synchronized FTMatches matches() {
        return all;
      }

      @Override
      public synchronized int next() {
        return pre;
      }

      @Override
      public synchronized int size() {
        return size;
      }

      @Override
      public synchronized double score() {
        return sc;
      }

      @Override
      public String toString() {
        return Integer.toString(size);
      }
    };
  }
}
