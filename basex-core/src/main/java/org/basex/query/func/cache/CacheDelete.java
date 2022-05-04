package org.basex.query.func.cache;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CacheDelete extends CacheFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(0, qc);
    if(!cache(qc).delete(name)) throw CACHE_NOTFOUND_X.get(info, name);
    return Empty.VALUE;
  }
}
