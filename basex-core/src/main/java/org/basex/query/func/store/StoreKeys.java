package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreKeys extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toName(arg(0), qc);
    return stores(qc).keys(name, info, qc);
  }
}
