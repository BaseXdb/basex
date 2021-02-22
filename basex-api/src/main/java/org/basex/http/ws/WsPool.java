package org.basex.http.ws;

import java.nio.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.list.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a pool for WebSockets. It manages all connected WebSockets.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public final class WsPool {
  /** Clients of the pool. id -> adapter. */
  private static final ConcurrentHashMap<String, WebSocket> CLIENTS = new ConcurrentHashMap<>();
  /** WebSocket prefix. */
  private static final String PREFIX = "websocket";
  /** Incrementing id. */
  private static long websocketId = -1;

  /** Private constructor. */
  private WsPool() { }

  /**
   * Returns the ids of all connected clients.
   * @return client ids
   */
  public static TokenList ids() {
    final TokenList ids = new TokenList(CLIENTS.size());
    for(final String key : CLIENTS.keySet()) ids.add(key);
    return ids;
  }

  /**
   * Adds a WebSocket to the clients list.
   * @param socket WebSocket
   * @return client id
   */
  static String add(final WebSocket socket) {
    final String id = createId();
    CLIENTS.put(id, socket);
    return id;
  }

  /**
   * Removes a WebSocket from the clients list.
   * @param id client id
   */
  static void remove(final String id) {
    CLIENTS.remove(id);
  }

  /**
   * Sends a message to all connected clients.
   * @param message message
   * @throws QueryException query exception
   */
  public static void emit(final Value message) throws QueryException {
    send(message, new ArrayList<>(CLIENTS.values()));
  }

  /**
   * Sends a message to all connected clients except to the one with the given id.
   * @param message message
   * @param client client id
   * @throws QueryException query exception
   */
  public static void broadcast(final Value message, final String client) throws QueryException {
    final List<WebSocket> list = new ArrayList<>();
    for(final Entry<String, WebSocket> entry : CLIENTS.entrySet()) {
      if(!client.equals(entry.getKey())) list.add(entry.getValue());
    }
    send(message, list);
  }

  /**
   * Sends a message to a specific clients.
   * @param message message
   * @param ids client ids
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
   * Returns the client with the specified id.
   * @param id client id
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
          remote.sendBytesByFuture((ByteBuffer) value);
        } else {
          remote.sendStringByFuture((String) value);
        }
      }
    }
  }

  /**
   * Creates a new, unused WebSocket id.
   * @return new id
   */
  private static synchronized String createId() {
    websocketId = Math.max(0, websocketId + 1);
    return PREFIX + websocketId;
  }
}
