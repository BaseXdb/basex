package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestParameter extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final AStr name = toStr(arg(0), qc);

    final Value value = new ValueBuilder(qc).add(queryValues(qc).get(name)).
        add(formValues(qc).get(name)).value();
    return value.isEmpty() && defined(1) ? arg(1).value(qc) : value;
  }
}
