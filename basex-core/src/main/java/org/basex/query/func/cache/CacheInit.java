package org.basex.query.func.cache;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheInit extends CacheFn {
  @Override
  public Empty value(final QueryContext qc) throws QueryException {
    final CacheOptions options = toOptions(arg(0), new CacheOptions(), qc);
    final String name = toZeroString(arg(1), qc);

    final StaticOptions soptions = qc.context.soptions;
    final Integer entries = options.get(CacheOptions.MAX_ENTRIES);
    final Integer ttl = options.get(CacheOptions.TTL);
    final int max = entries != null ? entries : soptions.get(StaticOptions.CACHEMAX);
    final int lifetime = ttl != null ? ttl : soptions.get(StaticOptions.CACHETTL);
    if(max < 1) throw BASEX_OPTIONS_X.get(info, "Invalid number of entries: " + max + '.');
    if(lifetime < 0) throw BASEX_OPTIONS_X.get(info, "Invalid lifetime: " + lifetime + '.');

    caches(qc).init(max, lifetime, name);
    return Empty.VALUE;
  }
}
