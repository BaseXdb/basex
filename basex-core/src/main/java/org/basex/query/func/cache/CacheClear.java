package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheClear extends CacheFn {
  @Override
  public Empty value(final QueryContext qc) throws QueryException {
    caches(qc).clear();
    return Empty.VALUE;
  }
}
