package org.basex.index;

import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.io.DataAccess;

/**
 * This abstract class defines methods for the available full-text indexes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class FTIndex extends Index {
  /** Cache for number of hits and data reference per token. */
  final FTTokenMap cache = new FTTokenMap();
  /** Values file. */
  final Data data;
  /** Scoring mode. 1 = document based, 2 = textnode based .*/
  final int scm;

  /**
   * Constructor.
   * @param d data reference
   */
  FTIndex(final Data d) {
    data = d;
    scm = d.meta.prop.num(Prop.FTSCTYPE);
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
      boolean f = true;
      long pos = p;
      double score = -1, nscore = -1;
      int lpre, c;
      int pre;

      @Override
      public boolean more() {
        if(c == s) return false;

        if(f) {
          if (scm > 0) {
            nscore = -da.readNum(pos) / 1000d;            
            nscore = nscore / (data.meta.ftmaxscore / 1000d);
            pos = da.pos();
          }
          lpre = da.readNum(pos);
          pos = da.pos();
          f = false;
          size = s;
        }
        pre = lpre;
        score = nscore;

        all.reset(toknum);
        all.or(da.readNum(pos));
        while(++c < s && (lpre = da.readNum()) == pre) {
          final int n = da.readNum();
          if(!fast) all.or(n);
        }
        if (scm > 0 && lpre < 0) {
          nscore = -lpre / 1000d;
          nscore = nscore / (data.meta.ftmaxscore / 1000d);
          lpre =  da.readNum();          
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
        return score;
      }
    };
  }
}
