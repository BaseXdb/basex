package org.basex.query.item;

import org.basex.data.Data;
import org.basex.data.FTMatches;

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
   * @param tol token length
   */
  public FTItem(final FTMatches a, final Data d, final int p, final int tol) {
    super(d, p, null, Type.TXT);
    all = a;
    score = -1;
    tl = tol;
  }

  @Override
  public double score() {
    if(score == -1) {
      // [SG] rewritten to get score values <= 1
      score = Math.log(tl * all.size + 1) / Math.log(data.textLen(pre) + 1);
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
