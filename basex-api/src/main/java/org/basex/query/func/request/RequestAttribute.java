package org.basex.query.func.request;

import org.basex.http.*;
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
public final class RequestAttribute extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toString(arg(0), qc);
    final Object object = HTTPConnection.getAttribute(request(qc), name);
    if(object instanceof Value value) return value;

    return defined(1) ? arg(1).value(qc) : Empty.VALUE;
  }
}
