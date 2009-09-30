package org.basex.query.item;

import java.util.Iterator;

import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.data.FTStringMatch;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTItem extends DBNode {
  /** Full-text matches. */
  public FTMatches all;
  /** Length of the full-text token. */
  private int tl;
  /** Total number of indexed results. */
  private int is;
  /** Flag for indexed score values. */
  private boolean ids;

  /**
   * Constructor, called by the sequential variant.
   * @param a matches
   * @param s scoring
   */
  public FTItem(final FTMatches a, final double s) {
    all = a;
    score = s;
  }

  /**
   * Constructor, called by the index variant.
   * @param a full-text matches
   * @param d data reference
   * @param p pre value
   * @param tis total size indexed results
   * @param tol token length
   * @param indexedScore flag for usage of indexed score values
   */
  public FTItem(final FTMatches a, final Data d, final int p, final int tol, 
      final int tis, final boolean indexedScore) {
    super(d, p, null, Type.TXT);
    all = a;
    score = -1;
    tl = tol;
    is = tis;
    ids = indexedScore;
  }

  @Override
  public double score() {
    if(score == -1) {
      if(ids) {
        Iterator<FTStringMatch> i = all.match[0].iterator();
        if(i.hasNext()) {
          score = i.next().e / 1000d;
        } else {
          score = Math.max((double) all.size / (double) is,
              Math.log(tl * all.size + 1) / Math.log(data.textLen(pre) + 1));
        }
      } else {
        // [SG] rewritten to get score values <= 1
        score = Math.max((double) all.size / (double) is,
            Math.log(tl * all.size + 1) / Math.log(data.textLen(pre) + 1));
      }
      //score = (double) ((tl + 1) * all.match.length - 1) /
      //  (double) data.textLen(pre);
      // [SG] default scoring
      //score = all.matches() ? 1 : 0;
    }
    return score;
  }

  @Override
  public String toString() {
    return super.toString() + (all != null ? " (" + all.size + ")" : "");
    /*return data != null ? super.toString() + " (" + all.size + ")" :
      name() + " (" + all.size + ")";*/
  }
}
