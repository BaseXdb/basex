package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestQuery extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String query = state(qc).query();
    return query == null ? Empty.VALUE : Str.get(query);
  }
}
