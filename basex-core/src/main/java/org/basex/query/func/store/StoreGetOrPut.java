package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreGetOrPut extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String key = toString(arg(0), qc);
    final FItem put = toFunction(arg(1), 0, qc);
    final String name = toName(arg(2), qc);

    Value value = stores(qc).get(key, name, info, qc);
    if(value.isEmpty()) {
      value = invoke(put, new HofArgs(), qc);
      store(key, value, name, qc);
    }
    return value;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
