package org.basex.query.item;

import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.query.ft.Scoring;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
public final class FTItem extends DBNode {
  /** Full-text matches. */
  public FTMatches all;
  /** Length of the full-text token. */
  private int tl;
  /** Total number of indexed results. */
  private int is;

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
   * @param s score value out of the index
   */
  public FTItem(final FTMatches a, final Data d, final int p, final int tol,
      final int tis, final double s) {
    super(d, p, null, Type.TXT);
    all = a;
    tl = tol;
    is = tis;
    score = s;
  }

  @Override
  public double score() {
    if(score == -1)
      score = Scoring.textNode(all.size, is, tl, data.textLen(pre, true));
    return score;
  }

  @Override
  public String toString() {
    return super.toString() + (all != null ? " (" + all.size + ")" : "");
  }
}
