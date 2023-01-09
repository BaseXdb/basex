package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StoreGet extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toKey(qc);
    return store(qc).get(key);
  }
}
