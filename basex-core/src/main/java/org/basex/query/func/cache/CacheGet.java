package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CacheGet extends CacheFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] key = toKey(qc);
    return cache(qc).get(key);
  }
}
