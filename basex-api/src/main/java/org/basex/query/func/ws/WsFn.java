package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * WebSocket function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class WsFn extends StandardFunc {
  /** QName. */
  static final QNm Q_WEBSOCKET = new QNm("websocket");
  /** QName. */
  static final QNm Q_ID = new QNm("id");
  /** QName. */
  static final QNm Q_PATH = new QNm("path");
  /** QName. */
  static final QNm Q_ADDRESS = new QNm("address");
  /** QName. */
  static final QNm Q_USER = new QNm("user");
  /** QName. */
  static final QNm Q_SESSION = new QNm("session");
  /** QName. */
  static final QNm Q_SUBPROTOCOL = new QNm("subprotocol");
  /** QName. */
  static final QNm Q_CREATED = new QNm("created");
  /** QName. */
  static final QNm Q_ACCESSED = new QNm("accessed");

  /**
   * Returns the current WebSocket.
   * @param qc query context
   * @return client ID
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
    final String id = toString(arg(0), qc);
    final WebSocket ws = WsPool.get(id);
    if(ws == null) throw WS_NOTFOUND_X.get(null, id);
    return ws;
  }
}
