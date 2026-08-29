package org.basex.query.func.rest;

import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestInit extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final boolean update = toBooleanOrFalse(arg(0), qc);

    WebModules.get(qc.context).init(update);
    return Empty.VALUE;
  }
}
