package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheDelete extends CacheFn {
  @Override
  protected Empty item(final QueryContext qc) throws QueryException {
    final String name = toZeroString(arg(0), qc);

    caches(qc).delete(name);
    return Empty.VALUE;
  }
}
