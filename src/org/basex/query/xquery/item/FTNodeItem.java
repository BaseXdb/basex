package org.basex.query.xquery.item;

import org.basex.data.Data;
import org.basex.index.FTNode;
import org.basex.query.xquery.util.Scoring;

/**
 * XQuery item representing a full-text Node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTNodeItem extends DBNode {
  /** FTNode object. */
  public final FTNode ftn;
  
  /**
   * Constructor.
   */
  public FTNodeItem() {
    super();
    ftn = new FTNode();
    score = -1;
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
  
  @Override 
  public double score() {
    // [SG] old scoring routine was returning irritating results..
    //   using default 0 and 1, until better solution is available
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
