package org.basex.query.func.cache;

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
public final class CacheGetOrPut extends CacheFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toToken(arg(0), qc);
    final FItem put = toFunction(arg(1), 0, qc);
    final Item expires = arg(2).atomItem(qc, info);

    Value value = cache(qc).get(key);
    if(value.isEmpty()) {
      value = invoke(put, new HofArgs(), qc);
      cache(key, value, expires, qc);
    }
    return value;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
