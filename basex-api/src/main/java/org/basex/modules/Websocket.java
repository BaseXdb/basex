package org.basex.modules;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.http.ws.*;
import org.basex.http.ws.adapter.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for processing lokal WebSockets.
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
    System.out.println("Websocket broadcast");
    WsPool.get().broadcast(message, id());
  }

  /**
   * Returns the attribute value for the specified key and the current client.
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException query exception
   */
  public Value get(final Str key) throws QueryException {
    return WsPool.get().getAttribute(id().toJava(), key.toJava());
  }

  /**
   * Assigns an attribute to the current client.
   * @param key key of the attribute
   * @param value value to be stored
   * @throws QueryException query exception
   */
  public void set(final Str key, final Value value) throws QueryException {
    WsPool.get().setAttribute(id().toJava(), key.toJava(), value);
  }

  /**
   * Removes an attribute from the current client.
   * @param key key of the attribute
   * @throws QueryException query exception
   */
  public void delete(final Str key) throws QueryException {
    WsPool.get().delete(id().toJava(), key.toJava());
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
