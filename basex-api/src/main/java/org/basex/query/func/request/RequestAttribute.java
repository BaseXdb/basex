package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class RequestAttribute extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toString(exprs[0], qc);
    final Object object = request(qc).getAttribute(name);
    if(object != null) return JavaCall.toValue(object, qc, info);

    return defined(1) ? exprs[1].value(qc) : Empty.VALUE;
  }
}
