package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StorePut extends StoreFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] key = toKey(qc);
    final Value value = arg(1).value(qc);

    store(qc).put(key, value.materialize(n -> false, ii, qc));
    return Empty.VALUE;
  }
}
