package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.node.*;

/**
 * ANode iterator interface.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class NodeIter extends Iter {
  @Override
  public abstract ANode next() throws QueryException;

  @Override
  public ANode get(final long i) {
    return null;
  }

  @Override
  public long size() {
    return -1;
  }
}
