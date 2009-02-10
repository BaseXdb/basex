package org.basex.query.item;

import org.basex.data.Data;
import org.basex.index.FTNode;
import org.basex.query.util.Scoring;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNodeItem extends DBNode {
  /** FTNode object. */
  private static final FTNodeItem ZERO = new FTNodeItem(0);
  /** FTNode object. */
  public FTNode ftn;

  /**
   * Constructor.
   */
  public FTNodeItem() {
    ftn = new FTNode();
    score = -1;
  }

  /**
   * Constructor.
   * @param s score value
   */
  public FTNodeItem(final double s) {
    score = s;
  }

  /**
   * Constructor.
   * @param ftnode FTNode
   * @param dat Data reference
   */
  public FTNodeItem(final FTNode ftnode, final Data dat) {
    super(dat, ftnode.getPre());
    ftn = ftnode;
    score = -1;
  }

  /**
   * Constructor.
   * @param s score value
   * @return instance
   */
  public static FTNodeItem get(final double s) {
    return s == 0 ? ZERO : new FTNodeItem(s);
  }

  @Override 
  public double score() {
    if(score == -1) score = ftn.size > 0 && ftn.getToken() != null ? 1 : 0;
    return score;
  }

  /**
   * Merge current item with an other FTNodeItem.
   * @param i1 other FTNodeItem
   * @param w number of words
   */
  public void merge(final FTNodeItem i1, final int w) {
    ftn.merge(i1.ftn, w);
    score = Scoring.and(score, i1.score);
  }

  @Override
  public String toString() {
    return ftn.toString();
  }
}
