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
   */
  public FTItem(final FTMatches a, final Data d, final int p) {
    super(d, p, null, Type.TXT);
    all = a;
    score = -1;
  }

  @Override
  public double score() {
    // default score for index results
    if(score == -1) score = all.matches() ? 1 : 0;
    return score;
  }

  @Override
  public String toString() {
    return data != null ? super.toString() + " (" + all.size + ")" :
      name() + " (" + all.size + ")";
  }
}
