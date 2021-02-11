package org.basex.query.func.sessions;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SessionsIds extends SessionsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    check(qc);

    return StrSeq.get(SessionListener.ids());
  }
}
