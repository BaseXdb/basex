package org.basex.query.iter;

import org.basex.query.QueryException;
import org.basex.query.item.Nod;

/**
 * Node iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class NodeIter extends Iter {
  @Override
  public abstract Nod next() throws QueryException;
}
