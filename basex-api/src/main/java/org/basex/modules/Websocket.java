package org.basex.modules;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.http.ws.*;
import org.basex.http.ws.adapter.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for processing local WebSockets.
 * The class name is {@code Websocket} instead of {@code WebSocket}.
 * Otherwise, it would be resolved to {@code web-socket} in XQuery.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class Websocket extends QueryModule {
  /**
   * Returns the id of the current client.
   * @return client id
   * @throws QueryException QueryException
   */
  public Str id() throws QueryException {
    final Object ws = queryContext.getProperty(HTTPText.WEBSOCKET);
    if(ws == null) throw BASEX_WS.get(null);
    return Str.get(((WsAdapter) ws).id);
  }

  /**
   * Broadcasts a message to all connected members without the sender.
   * @param message message
   * @throws QueryException Query Exception
   * @throws IOException I/O exception
   */
  public void broadcast(final Item message) throws QueryException, IOException {
    WsPool.get().broadcast(message, id());
  }

  /**
   * Returns the path of the current client.
   * @throws QueryException query exception
   * @return path
   */
  public Str path() throws QueryException {
    return Str.get(WsPool.get().path(id().toJava()));
  }

}
