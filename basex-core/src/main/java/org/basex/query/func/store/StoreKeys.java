package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class StoreKeys extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return store(qc).keys();
  }
}
