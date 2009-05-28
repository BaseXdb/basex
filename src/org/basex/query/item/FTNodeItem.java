package org.basex.query.item;

import org.basex.data.Data;
import org.basex.index.FTNode;
import org.basex.query.QueryContext;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTNodeItem extends DBNode {
  /** FTNode object. */
  public FTNode ftn;

  /**
   * Constructor.
   * @param s scoring
   */
  public FTNodeItem(final double s) {
    ftn = new FTNode();
    score = s;
  }

  /**
   * Constructor.
   * @param ftnode FTNode
   * @param dat Data reference
   */
  public FTNodeItem(final FTNode ftnode, final Data dat) {
    super(dat, ftnode.pre());
    ftn = ftnode;
    score = -1;
  }

  @Override
  public double score() {
    if(score == -1) score = !ftn.empty() && ftn.getToken() != null ? 1 : 0;
    return score;
  }

  /**
   * Merges the current item with an other node.
   * @param ctx query context
   * @param i1 second node
   * @param w number of words
   */
  public void union(final QueryContext ctx, final FTNodeItem i1, final int w) {
    ftn.union(i1.ftn, w);
    score = ctx.score.or(score, i1.score);
  }

  @Override
  public String toString() {
    return super.toString() + " (" + ftn + ")";
  }
}
