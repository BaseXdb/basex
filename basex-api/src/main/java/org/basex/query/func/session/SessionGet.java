package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionGet extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, false);
    final String name = toString(arg(0), qc);

    final Value value = session != null ? session.get(name, qc, info) : null;
    return value != null ? value : defined(1) ? arg(1).value(qc) : Empty.VALUE;
  }
}
