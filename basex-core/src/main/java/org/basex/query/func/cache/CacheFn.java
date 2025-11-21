package org.basex.query.func.cache;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

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
   * @param expires expiration value (dayTimeDuration, dateTime, time, minutes/integer)
   * @param qc query context
   * @throws QueryException query exception
   */
  final void cache(final byte[] key, final Value value, final Item expires, final QueryContext qc)
      throws QueryException {

    final String time = expires.isEmpty() ? qc.context.soptions.get(
        StaticOptions.CACHEEXPIRY) : Token.string(expires.string(info));
    final long ms = QueryJob.toDelay(QueryJob.toTime(time, info), 0, info);
    cache(qc).put(key, value.materialize(n -> false, info, qc).shrink(qc), ms);
  }
}
