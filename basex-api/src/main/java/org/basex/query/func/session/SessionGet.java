package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SessionGet extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, false);
    final byte[] name = toToken(exprs[0], qc);
    final Value dflt = exprs.length == 1 ? Empty.VALUE : exprs[1].value(qc);

    if(session != null) {
      final Object object = session.get(name);
      if(object != null) return JavaCall.toValue(object, qc, sc);
    }
    return dflt;
  }
}
