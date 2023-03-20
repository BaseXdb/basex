package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StoreGetOrPut extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toKey(qc);
    final FItem put = toFunction(arg(1), 0, qc);

    Value value = store(qc).get(key);
    if(value.isEmpty()) {
      value = put.invoke(qc, info);
      store(qc).put(key, value);
    }
    return value;
  }
}
