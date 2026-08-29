package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreRemove extends StoreFn {
  @Override
  public Empty value(final QueryContext qc) throws QueryException {
    final String key = toString(arg(0), qc);
    final String name = toName(arg(1), qc);
    stores(qc).remove(key, name, info, qc);
    return Empty.VALUE;
  }
}
