package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CacheRemove extends CacheFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] key = toKey(qc);
    cache(qc).remove(key);
    return Empty.VALUE;
  }
}
