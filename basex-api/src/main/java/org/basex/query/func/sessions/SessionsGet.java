package org.basex.query.func.sessions;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
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
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = session(qc).get(toToken(exprs[1], qc),
        exprs.length == 2 ? Empty.SEQ : exprs[2].value(qc));
    if(value == null) throw SESSIONS_GET.get(info);
    return value;
  }
}
