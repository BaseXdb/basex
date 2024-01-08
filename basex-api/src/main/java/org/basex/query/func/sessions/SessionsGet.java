package org.basex.query.func.sessions;

import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.func.session.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class SessionsGet extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc);
    final String name = toString(arg(1), qc);

    final Object object = session.get(name);
    if(object != null) return JavaCall.toValue(object, qc, info);

    return defined(2) ? arg(2).value(qc) : Empty.VALUE;
  }
}
