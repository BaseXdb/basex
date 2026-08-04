package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreClear extends StoreFn {
  @Override
  protected Empty item(final QueryContext qc) throws QueryException {
    stores(qc).clear(info);
    return Empty.VALUE;
  }
}
