package org.basex.modules;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * This module contains functions for processing WebSockets.
 * The class name is {@code Websocket} instead of {@code WebSocket}.
 * Otherwise, it would be resolved to {@code web-socket} in XQuery.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class Ws extends QueryModule {
  /**
   * Returns the id of the current client.
   * @return client id
   * @throws QueryException QueryException
   */
  public String id() throws QueryException {
    final Object ws = queryContext.getProperty(HTTPText.WEBSOCKET);
    if(ws == null) throw BASEX_WS.get(null);
    return ((WebSocket) ws).id;
  }

  /**
   * Returns the ids of all connected clients.
   * @return client ids
   */
  public String[] ids() {
    return pool().ids().toArray();
  }

  /**
   * Emits the message to all connected clients.
   * @param message message
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void emit(final Item message) throws QueryException, IOException {
    pool().emit(message);
  }


  /**
   * Broadcasts a message to all connected clients without the sender.
   * @param message message
   * @throws QueryException Query Exception
   * @throws IOException I/O exception
   */
  public void broadcast(final Item message) throws QueryException, IOException {
    pool().broadcast(message, id());
  }

  /**
   * Sends a message to the clients with the specified ids.
   * @param message message
   * @param ids client ids
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void send(final Item message, final Value ids) throws QueryException, IOException {
    final StringList list = new StringList();
    for(final Item id : ids) {
      list.add(id.toString().replace("\"", ""));
    }
    pool().send(message, list.finish());
  }

  /**
   * Sends a message to a channel.
   * @param message message
   * @param channel channel
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void sendchannel(final Item message, final Str channel) throws QueryException, IOException {
    pool().sendChannel(message, channel);
  }
  /**
   * Returns the path of the current client.
   * @throws QueryException query exception
   * @return path
   */
  public String path() throws QueryException {
    return pool().path(id());
  }

  /**
   * Returns the path of the specified client.
   * @param id id of client
   * @return path
   */
  public String path(final Str id) {
    return pool().path(id.toJava());
  }

  /**
   * Closes the WebSocketConnection of the Current client.
   * @param reason The string reason
   * @throws QueryException query exception
   * */
  public void closewebsocket(final Str reason) throws QueryException {
    pool().closeWebsocket(id(), reason.toJava());
  }

  /**
   * Closes the WebSocketConnection of a client.
   * @param id The id of the WebSocket Connection.
   * @param reason The string reason
   * */
  public void closewebsocket(final Str id, final Str reason) {
    pool().closeWebsocket(id.toJava(), reason.toJava());
  }

  /**
   * Returns a reference to the WebSocket pool.
   * @return pool
   */
  private static WsPool pool() {
    return WsPool.get();
  }
}
