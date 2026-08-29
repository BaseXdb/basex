package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionClientId extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = request(qc).getRequestedSessionId();
    return id != null ? Str.get(id) : Empty.VALUE;
  }
}
