package org.basex.query.iter;

import org.basex.query.QueryException;
import org.basex.query.item.ANode;

/**
 * Node iterator interface.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class NodeIter extends Iter {
  @Override
  public abstract ANode next() throws QueryException;
}
