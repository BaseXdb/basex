package org.basex.query.func.store;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StoreDelete extends StoreFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(0, qc);
    if(!store(qc).delete(name)) throw STORE_NOTFOUND_X.get(info, name);
    return Empty.VALUE;
  }
}
