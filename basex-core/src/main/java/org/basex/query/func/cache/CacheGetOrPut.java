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
    final String key = toString(arg(0), qc);
    final FItem put = toFunction(arg(1), 0, qc);
    final String name = toZeroString(arg(2), qc);

    Value value = caches(qc).get(key, name);
    if(value == null) {
      value = invoke(put, new HofArgs(), qc);
      cache(key, value, name, qc);
    }
    return value;
  }
}
