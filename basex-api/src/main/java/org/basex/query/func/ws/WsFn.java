package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * WebSocket function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
abstract class WsFn extends StandardFunc {
  /**
   * Returns the current WebSocket.
   * @param qc query context
   * @return client id
   * @throws QueryException QueryException
   */
  final WebSocket ws(final QueryContext qc) throws QueryException {
    final WebSocket ws = (WebSocket) qc.context.getExternal(WebSocket.class);
    if(ws == null) throw BASEX_WS.get(info);
    return ws;
  }

  /**
   * Returns the specified client from the WebSocket pool.
   * @param qc query context
   * @return client
   * @throws QueryException query exception
   */
  final WebSocket client(final QueryContext qc) throws QueryException {
    final String id = toString(exprs[0], qc);
    final WebSocket ws = WsPool.get(id);
    if(ws == null) throw WS_NOTFOUND_X.get(null, id);
    return ws;
  }
}
