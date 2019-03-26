package org.basex.query.func.session;

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
public final class SessionGet extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = session(qc).get(toToken(exprs[0], qc),
        exprs.length == 1 ? Empty.VALUE : exprs[1].value(qc));
    if(value == null) throw SESSION_GET.get(info);
    return value;
  }
}
