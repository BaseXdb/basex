package org.basex.query.xquery.iter;

import org.basex.index.FTNode;
import org.basex.index.IndexArrayIterator;
import org.basex.query.xquery.item.FTIndexItem;

/**
 * Simple node Iterator, ignoring duplicates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class FTNodeIter extends Iter {
  /** IndexArrayIterator collects indexresults. */
  public IndexArrayIterator iai;
  
  /**
   * Constructor.
   * @param iter iterator 
   */
  public FTNodeIter(final IndexArrayIterator iter) {
    iai = iter;
  }

  /**
   * Constructor.
   */
  public FTNodeIter() {
  }

 @Override
 public FTIndexItem next() {
   return new FTIndexItem(iai.more() ? iai.nextFTNode() : new FTNode());
 }
  
  @Override
  public long size() {
    return iai.size();
  }

}
