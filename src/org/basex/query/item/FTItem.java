package org.basex.query.item;

import org.basex.data.Data;
import org.basex.index.FTEntry;
import org.basex.query.QueryContext;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTItem extends DBNode {
  /** Full-text entry. Only used by the index variant. */
  public FTEntry fte;
  /** Position lists. Needed for position filters. */
  public IntList[] pos = {};

  /**
   * Constructor, called by the index variant. 
   */
  public FTItem() {
    fte = new FTEntry();
  }

  /**
   * Constructor, called by the sequential variant.
   * @param s scoring
   */
  public FTItem(final double s) {
    score = s;
  }

  /**
   * Constructor, called by the index variant.
   * @param f full-text entry
   * @param d data reference
   */
  public FTItem(final FTEntry f, final Data d) {
    super(d, f.pre(), null, Type.TXT);
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
   * Merges the position lists of two nodes. Called by the sequential variant.
   * @param n second node instance
   * @return self reference
   */
  public FTItem union(final FTItem n) {
    if(n != null) {
      final IntList[] tmp = new IntList[pos.length + n.pos.length];
      Array.copy(n.pos, tmp, 0);
      Array.copy(pos, tmp, n.pos.length);
      pos = tmp;
    }
    return this;
  }

  /**
   * Merges the current item with an other node. Called by the index variant.
   * @param ctx query context
   * @param i1 second node
   * @param w number of words
   */
  public void union(final QueryContext ctx, final FTItem i1, final int w) {
    fte.union(i1.fte, w);
    score = ctx.score.or(score, i1.score);
  }
  
  /**
   * Converts the index positions. Called by the index variant.
   */
  public void convertPos() {
    pos = new IntList[fte.getTokenNum()];
    for(int p = 0; p < pos.length; p++) pos[p] = new IntList();
    while(fte.morePos()) pos[fte.nextPoi() - 1].add(fte.nextPos());
    fte.reset();
  }

  @Override
  public double score() {
    // default score for index results
    if(score == -1) score = empty() ? 0 : 1;
    return score;
  }

  @Override
  public String toString() {
    return data != null ? super.toString() + " (" + fte + ")" :
      name() + " (" + score + ")";
  }
}
