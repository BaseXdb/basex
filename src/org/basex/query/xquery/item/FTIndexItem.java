package org.basex.query.xquery.item;

import org.basex.index.FTNode;

/**
 * Full text index item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTIndexItem extends Item {
  /** Fulltext node. **/
  public FTNode ftnode;
  
  /**
   * Constructor, adding a full-text score.
   * @param ftn FTNode
   */
  public FTIndexItem(final FTNode ftn) {
    super(Type.ITEM);
    ftnode = ftn;
  }

  @Override
  public boolean eq(final Item it) {
    return false;
 }
  
  @Override
  public boolean bool() {
    return ftnode.size > 0;
  }
  @Override
  public double dbl() {
    return ftnode.getTokens().length;
  }
}
