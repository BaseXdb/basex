package org.basex.query.func.cache;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Cache function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class CacheFn extends StandardFunc {
  /**
   * Returns the cache.
   * @param qc query context
   * @return cache
   */
  static Cache cache(final QueryContext qc) {
    return qc.context.cache;
  }

  /**
   * Caches a materialized, compact version of the specified value.
   * @param key key
   * @param value value
   * @param name name of cache (empty string for default cache)
   * @param qc query context
   * @throws QueryException query exception
   */
  final void cache(final String key, final Value value, final String name, final QueryContext qc)
      throws QueryException {
    cache(qc).put(key, value.materialize(n -> false, info, qc).shrink(qc), name);
  }
}
