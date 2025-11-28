package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheGet extends CacheFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String key = toString(arg(0), qc);
    final String name = toZeroString(arg(1), qc);

    final Value value = caches(qc).get(key, name);
    return value != null ? value : Empty.VALUE;
  }
}
