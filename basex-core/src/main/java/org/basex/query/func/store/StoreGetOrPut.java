package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class StoreGetOrPut extends StoreFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toKey(qc);
    final FItem func = toFunction(exprs[1], 0, false, qc);

    Value value = store(qc).get(key);
    if(value == Empty.VALUE) {
      value = func.invoke(qc, info);
      store(qc).put(key, value);
    }
    return value;
  }
}
