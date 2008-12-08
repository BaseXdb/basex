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
public class FTNodeItem extends Item {
  /** FTNode object. */
  public FTNode ftn;
  /** Data. **/
  public Data data;
  /** Double value. */
  private double d = -1;
  
  /**
   * Constructor.
   */
  public FTNodeItem() {
    super(Type.FTN);
    ftn = new FTNode();
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
  }

  @Override
  public boolean eq(final Item i) {
    return i instanceof FTNodeItem && 
      ((FTNodeItem) i).ftn.getPre() == ftn.getPre();
  }
  @Override
  public String toString() {
    return ftn.toString();
  }
  
  @Override 
  public double dbl() {
    d = d == -1 ? ftn.size > 0 ? 
        Scoring.word(ftn.p.size - 1, ftn.getTokens().length) : 0 : d;
    return d;
  }
  
  /**
   * Set new double value.
   * @param dbl new double values. 
   */
  public void setDbl(final double dbl) {
    d = dbl;
  }
  
  /**
   * Convert the current object to a DNode.
   * 
   * @return DNode 
   */
  public DNode getDNode() {
    if (ftn.size == 0) return null; 
    final DNode dn = new DNode(data, ftn.getPre(), null, Type.TXT);
    dn.parent();
    return dn;
  }

  /**
   * Merge current item with an other FTNodeItem.
   * 
   * @param i1 other FTNodeItem
   * @param w number of words
   */
  public void merge(final FTNodeItem i1, final int w) {
    ftn.merge(i1.ftn, w);
    d = Scoring.and(d, i1.d);
  }
}
