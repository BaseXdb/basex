package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StorePut extends StoreFn {
  @Override
  protected Empty item(final QueryContext qc) throws QueryException {
    final String key = toString(arg(0), qc);
    final Value value = arg(1).value(qc);
    final String name = toName(arg(2), qc);

    store(key, value, name, qc);
    return Empty.VALUE;
  }
}
