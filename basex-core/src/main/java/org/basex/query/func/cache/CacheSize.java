package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheSize extends CacheFn {
  @Override
  public Itr value(final QueryContext qc) throws QueryException {
    final String name = toZeroString(arg(0), qc);

    return Itr.get(caches(qc).size(name));
  }
}
