package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreGet extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String key = toString(arg(0), qc);
    final String name = toName(arg(1), qc);
    return store(qc).get(key, name, info, qc);
  }
}
