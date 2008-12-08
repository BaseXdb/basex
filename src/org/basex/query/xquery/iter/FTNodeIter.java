package org.basex.query.xquery.iter;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FTNodeItem;

/**
 * Node iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class FTNodeIter extends Iter {
  /** Empty node iterator. */
  public static final FTNodeIter NONE = new FTNodeIter() {
    @Override
    public FTNodeItem next() { return null; }
    @Override
    public long size() { return 0; }
    @Override
    public String toString() { return "()"; }
  };

  @Override
  public abstract FTNodeItem next() throws XQException;
}
