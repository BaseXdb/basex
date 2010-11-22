package org.basex.query.iter;

import org.basex.query.QueryException;
import org.basex.query.item.FTNode;

/**
 * Node iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
public abstract class FTIter extends Iter {
  @Override
  public abstract FTNode next() throws QueryException;
}
