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
public final class SessionsClose extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc);

    session.close();
    return Empty.VALUE;
  }
}
