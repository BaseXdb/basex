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
public final class SessionDelete extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, false);
    final String key = toString(arg(0), qc);

    if(session != null) session.delete(key);
    return Empty.VALUE;
  }
}
