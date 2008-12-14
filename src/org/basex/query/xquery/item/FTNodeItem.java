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
public final class FTNodeItem extends Item {
  /** FTNode object. */
  public final FTNode ftn;
  /** Data. **/
  public final Data data;
  
  /**
   * Constructor.
   */
  public FTNodeItem() {
    this(new FTNode(), null);
  }

  /**
   * Constructor.
   * @param ftnode FTNode
   * @param dat Data reference
   */
  public FTNodeItem(final FTNode ftnode, final Data dat) {
    super(Type.FTN);
    ftn = ftnode;
    data = dat;
    score = -1;
  }

  @Override
  public boolean eq(final Item i) {
    return i instanceof FTNodeItem && 
      ((FTNodeItem) i).ftn.getPre() == ftn.getPre();
  }
  
  @Override 
  public double dbl() {
    if(score == -1)
      score = ftn.size > 0 ? Scoring.word(ftn.p.size - 1, ftn.getLength()) : 0;
    return score;
  }
  
  /**
   * Set new double value.
   * @param dbl new double values. 
   */
  public void setDbl(final double dbl) {
    score = dbl;
  }
  
  /**
   * Convert the current object to a DNode.
   * @return DNode 
   */
  public DBNode dbNode() {
    if(ftn.size == 0) return null;
    final DBNode dn = new DBNode(data, ftn.getPre(), null, Type.TXT);
    dn.parent();
    return dn;
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
