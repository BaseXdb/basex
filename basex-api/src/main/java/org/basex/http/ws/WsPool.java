package org.basex.http.ws;

import java.nio.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;
import org.eclipse.jetty.ee9.websocket.api.*;

/**
 * This class defines a pool for WebSockets. It manages all connected WebSockets.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WsPool {
  /** Clients of the pool. ID -> adapter. */
  private static final ConcurrentHashMap<String, WebSocket> CLIENTS = new ConcurrentHashMap<>();
  /** WebSocket prefix. */
  private static final String PREFIX = "websocket";
  /** Incrementing ID. */
  private static long websocketId = -1;

  /** Private constructor. */
  private WsPool() { }

  /**
   * Returns the IDs of all connected clients.
   * @return client IDs
   */
  public static TokenList ids() {
    final TokenList ids = new TokenList(CLIENTS.size());
    for(final String key : CLIENTS.keySet()) ids.add(key);
    return ids;
  }

  /**
   * Adds a WebSocket to the clients list.
   * @param socket WebSocket
   * @return client ID
   */
  static String add(final WebSocket socket) {
    final String id = createId();
    CLIENTS.put(id, socket);
    return id;
  }

  /**
   * Removes a WebSocket from the clients list.
   * @param id client ID
   */
  static void remove(final String id) {
    CLIENTS.remove(id);
  }

  /**
   * Sends a message to all connected clients.
   * @param message message
   * @throws QueryException query exception
   */
  public static void emit(final Item message) throws QueryException {
    send(message, new ArrayList<>(CLIENTS.values()));
  }

  /**
   * Sends a message to all connected clients except to the one with the given ID.
   * @param message message
   * @param client client ID
   * @throws QueryException query exception
   */
  public static void broadcast(final Item message, final String client) throws QueryException {
    final List<WebSocket> list = new ArrayList<>();
    for(final Entry<String, WebSocket> entry : CLIENTS.entrySet()) {
      if(!client.equals(entry.getKey())) list.add(entry.getValue());
    }
    send(message, list);
  }

  /**
   * Sends a message to a specific clients.
   * @param message message
   * @param ids client IDs
   * @throws QueryException query exception
   */
  public static void send(final Value message, final String... ids) throws QueryException {
    final List<WebSocket> list = new ArrayList<>(ids.length);
    for(final String id : ids) {
      final WebSocket ws = CLIENTS.get(id);
      if(ws != null) list.add(ws);
    }
    send(message, list);
  }

  /**
   * Returns the client with the specified ID.
   * @param id client ID
   * @return client
   */
  public static WebSocket get(final String id) {
    return CLIENTS.get(id);
  }

  /**
   * Sends a message to the specified clients.
   * @param message message
   * @param websockets clients
   * @throws QueryException query exception
   */
  private static void send(final Value message, final List<WebSocket> websockets)
      throws QueryException {

    // serialize contents once
    final List<Object> values;
    try {
      values = WsResponse.serialize(message.iter(), new SerializerOptions());
    } catch(final QueryIOException ex) {
      throw ex.getCause();
    }

    // send result to all clients
    for(final WebSocket ws : websockets) {
      if(!ws.isConnected()) continue;
      final RemoteEndpoint remote = ws.getSession().getRemote();
      for(final Object value : values) {
        if(value instanceof ByteBuffer) {
          remote.sendBytes((ByteBuffer) value, WriteCallback.NOOP);
        } else {
          remote.sendString((String) value, WriteCallback.NOOP);
        }
      }
    }
  }

  /**
   * Creates a new, unused WebSocket ID.
   * @return new ID
   */
  private static synchronized String createId() {
    websocketId = Math.max(0, websocketId + 1);
    return PREFIX + websocketId;
  }
}
