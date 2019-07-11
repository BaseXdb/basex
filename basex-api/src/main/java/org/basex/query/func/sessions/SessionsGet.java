package org.basex.query.func.sessions;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class SessionsGet extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] name = toToken(exprs[1], qc);
    final Value dflt = exprs.length == 2 ? Empty.VALUE : exprs[2].value(qc);

    final Value value = session(qc).get(name, dflt);
    if(value == null) throw SESSIONS_GET.get(info);
    return value;
  }
}
