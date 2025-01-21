package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StorePut extends StoreFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] key = toKey(qc);
    final Value value = arg(1).value(qc);

    store(key, value, qc);
    return Empty.VALUE;
  }
}
