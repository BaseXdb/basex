package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class SessionGet extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] name = toToken(exprs[0], qc);
    final Value dflt = exprs.length == 1 ? Empty.VALUE : exprs[1].value(qc);

    final Object object = session(qc).get(name);
    return object != null ? JavaCall.toValue(object, qc, sc) : dflt;
  }
}
