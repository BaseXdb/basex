package org.basex.query.item;

import org.basex.data.Data;
import org.basex.index.FTEntry;
import org.basex.query.QueryContext;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTNode extends DBNode {
  /** Full-text entry. Only used by the index variant. */
  public FTEntry fte;

  /**
   * Constructor, called by the sequential variant.
   * @param s scoring
   */
  public FTNode(final double s) {
    score = s;
  }

  /**
   * Constructor, called by the index variant. 
   */
  public FTNode() {
    fte = new FTEntry();
  }

  /**
   * Constructor, called by the index variant.
   * @param f full-text entry
   * @param d data reference
   */
  public FTNode(final FTEntry f, final Data d) {
    super(d, f.pre());
    fte = f;
    score = -1;
  }

  /**
   * Returns true if no values are stored for this node.
   * @return result of check
   */
  public boolean empty() {
    return fte.pos.size == 0;
  }

  /**
   * Merges the current item with an other node. Called by the index variant.
   * @param ctx query context
   * @param i1 second node
   * @param w number of words
   */
  public void union(final QueryContext ctx, final FTNode i1, final int w) {
    fte.union(i1.fte, w);
    score = ctx.score.or(score, i1.score);
  }

  @Override
  public double score() {
    if(score == -1) score = !empty() && fte.getToken() != null ? 1 : 0;
    return score;
  }

  @Override
  public String toString() {
    return super.toString() + " (" + fte + ")";
  }
}
