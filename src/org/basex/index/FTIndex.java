package org.basex.index;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.io.DataAccess;

/**
 * This abstract class defines methods for the available full-text indexes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class FTIndex implements Index {
  /** Cache for number of hits and data reference per token. */
  final FTTokenMap cache = new FTTokenMap();
  /** Values file. */
  final Data data;
  /** Scoring mode. 1 = document based, 2 = textnode based .*/
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
    max = Math.log(data.meta.ftscmax + 1);
    min = Math.log(data.meta.ftscmin - 1);
  }

  /**
   * Returns an iterator for an index entry.
   * @param p pointer on data
   * @param s number of pre/pos entries
   * @param da data source
   * @param fast fast evaluation
   * @return iterator
   */
  FTIndexIterator iter(final long p, final int s, final DataAccess da,
      final boolean fast) {

    return new FTIndexIterator() {
      final FTMatches all = new FTMatches(toknum);
      double sc = -1, lsc = -1;
      int lpre, pre, c;
      long pos = p;

      @Override
      public boolean more() {
        if(c == s) return false;

        if(lpre == 0) {
          int n = da.readNum(pos);
          if(scm > 0) {
            lsc = (Math.log(n) - min) / (max - min);
            n = da.readNum();
          }
          size = s;
          lpre = n;
          pos = da.pos();
        }
        pre = lpre;
        sc = lsc;

        all.reset(toknum);
        all.or(da.readNum(pos));
        while(++c < s && (lpre = da.readNum()) == pre) {
          final int n = da.readNum();
          if(!fast) all.or(n);
        }
        pos = da.pos();
        return true;
      }

      @Override
      public FTMatches matches() {
        return all;
      }

      @Override
      public int next() {
        return pre;
      }

      @Override
      public double score() {
        return sc;
      }
    };
  }
}
