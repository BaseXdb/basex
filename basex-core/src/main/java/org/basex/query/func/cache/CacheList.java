package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CacheList extends CacheFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return cache(qc).list();
  }
}
