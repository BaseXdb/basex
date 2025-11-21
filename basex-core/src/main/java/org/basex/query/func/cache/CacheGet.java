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
    final byte[] key = toToken(arg(0), qc);
    final Value value = cache(qc).get(key);
    return value != null ? value : Empty.VALUE;
  }
}
