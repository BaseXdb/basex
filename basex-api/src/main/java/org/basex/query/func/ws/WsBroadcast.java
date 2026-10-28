package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsBroadcast extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    WsPool.broadcast(message(qc), ws(qc).id, info);
    return Empty.VALUE;
  }
}
