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
    WsPool.broadcast(arg(0).item(qc, info), ws(qc).id);
    return Empty.VALUE;
  }
}
