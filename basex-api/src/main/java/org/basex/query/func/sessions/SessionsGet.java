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
public final class SessionsGet extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc);
    final String name = toString(arg(1), qc);

    final Value value = session.get(name, qc, info);
    return value != null ? value : defined(2) ? arg(2).value(qc) : Empty.VALUE;
  }
}
