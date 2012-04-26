package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.item.*;

/**
 * Node iterator interface.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 */
public abstract class FTIter extends Iter {
  @Override
  public abstract FTNode next() throws QueryException;
}
