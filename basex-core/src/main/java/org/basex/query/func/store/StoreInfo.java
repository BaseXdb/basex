package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreInfo extends StoreFn {
  @Override
  protected XQMap item(final QueryContext qc) throws QueryException {
    final String name = toName(arg(0), qc);

    return stores(qc).info(name, info);
  }
}
