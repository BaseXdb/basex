package org.basex.query.func.sessions;

import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.func.session.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SessionsGet extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc);
    final byte[] name = toToken(exprs[1], qc);
    final Value dflt = exprs.length == 2 ? Empty.VALUE : exprs[2].value(qc);

    final Object object = session.get(name);
    return object != null ? JavaCall.toValue(object, qc, sc) : dflt;
  }
}
