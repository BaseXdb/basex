package org.basex.query.item;

import org.basex.data.Data;
import org.basex.data.FTMatches;
import org.basex.query.QueryContext;

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
    if(score == -1) score = all.match() ? 1 : 0;
    return score;
  }

  @Override
  public String toString() {
    return data != null ? super.toString() + " (" + all.size + ")" :
      name() + " (" + score + ")";
  }



  // [CG] FT: to be revised...

  /**
   * Merges the current item with an other node. Called by the index variant.
   * @param ctx query context
   * @param i1 second node
   * @param w number of words
   */
  public void union(final QueryContext ctx, final FTItem i1, final int w) {
    // [CG] dummy
    if(w != w) return;

    // [CG] FT: check pre != n.pre before call
    //matches[0].union(i1.matches[0], w);
    score = ctx.score.or(score, i1.score);
  }
}
