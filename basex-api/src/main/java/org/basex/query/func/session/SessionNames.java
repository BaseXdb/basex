package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SessionNames extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, false);

    return session != null ? session.names() : Empty.VALUE;
  }
}
