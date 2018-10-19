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

  /** Sends a message to a channel.
   * @param message message
   * @param channel channel
   */
  public void sendchannel(final Str message, final Str channel) {
    pool().sendChannel(message, channel);
  }
  
  /**
   * Returns the path of the current client.
   * @param id session id
   * @param key key to be requested
   * @return session attribute or {@code null}
   * @throws QueryException query exception
   */
  public Value get(final Str id, final Str key) throws QueryException {
    return get(id, key, null);
  }

  /**
   * Returns the specified WebSocket attribute.
   * @param id session id
   * @param key key to be requested
   * @param def default value
   * @return session attribute or {@code null}
   * @throws QueryException query exception
   */
  public Value get(final Str id, final Str key, final Value def) throws QueryException {
    final Value value = client(id).atts.get(key.toJava());
    return value != null ? value : def;
  }

  /**
   * Updates a WebSocket attribute.
   * @param id session id
   * @param key key of the attribute
   * @param value item to be stored
   * @throws QueryException query exception
   */
  public void set(final Str id, final Str key, final Value value) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(queryContext);
    for(final Item item : value) {
      final Item it = item.materialize(queryContext, item.persistent());
      if(it == null) throw WS_SET_X.get(null, item);
      vb.add(it);
    }
    client(id).atts.put(key.toJava(), vb.value());
  }

  /**
   * Removes a session attribute.
   * @param id session id
   * @param key key of the attribute
   * @throws QueryException query exception
   */
  public void delete(final Str id, final Str key) throws QueryException {
    client(id).atts.remove(key.toJava());
  }

  /**
   * Returns the path of the specified client.
   * @param id client id
   * @return path
   * @throws QueryException query exception
   */
  public String path(final Str id) throws QueryException {
    return client(id).path.toString();
  }

  /**
   * Closes the connection of the specified client.
   * @param id client id
   * @throws QueryException query exception
   */
  public void close(final Str id) throws QueryException {
    client(id).close();
  }

  /**
   * Returns a reference to the WebSocket pool.
   * @return pool
   */
  private static WsPool pool() {
    return WsPool.get();
  }

  /**
   * Returns the specified client from the WebSocket pool.
   * @param id client id
   * @return client
   * @throws QueryException query exception
   */
  private static WebSocket client(final Str id) throws QueryException {
    final WebSocket ws = pool().client(id.toJava());
    if(ws == null) throw WS_NOTFOUND_X.get(null, id);
    return ws;
  }
}
