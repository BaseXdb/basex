package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheRemove extends CacheFn {
  @Override
  public Empty value(final QueryContext qc) throws QueryException {
    final String key = toString(arg(0), qc);
    final String name = toZeroString(arg(1), qc);

    caches(qc).remove(key, name);
    return Empty.VALUE;
  }
}
