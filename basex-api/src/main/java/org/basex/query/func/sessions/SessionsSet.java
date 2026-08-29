package org.basex.query.func.sessions;

import org.basex.query.*;
import org.basex.query.func.session.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionsSet extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc);
    final String key = toString(arg(1), qc);
    final Value value = arg(2).value(qc);

    session.set(key, value.materialize(n -> false, info, qc));
    return Empty.VALUE;
  }
}
