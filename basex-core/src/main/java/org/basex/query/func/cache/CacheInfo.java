package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheInfo extends CacheFn {
  @Override
  protected XQMap item(final QueryContext qc) throws QueryException {
    final String name = toZeroString(arg(0), qc);

    return caches(qc).info(name);
  }
}
