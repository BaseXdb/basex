package org.basex.http.ws;

import java.io.*;
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
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class WsPool {
  /** Singleton pool. */
  private static WsPool instance;
  /** WebSocket prefix. */
  private static final String PREFIX = "websocket";
  /** Incrementing id. */
  private static long websocketId = -1;

  /** Clients of the pool. id -> adapter. */
  private final ConcurrentHashMap<String, WebSocket> clients = new ConcurrentHashMap<>();

  /**
   * Returns the pool instance.
   * @return instance
   */
  public static synchronized WsPool get() {
    if(instance == null) instance = new WsPool();
    return instance;
  }

  /**
   * Returns the ids of all connected clients.
   * @return client ids
   */
  public TokenList ids() {
    final TokenList ids = new TokenList(clients.size());
    for(final String key : clients.keySet()) ids.add(key);
    return ids;
  }

  /**
   * Adds a WebSocket to the clients list.
   * @param socket WebSocket
   * @return client id
   */
  public String add(final WebSocket socket) {
    final String id = createId();
    clients.put(id, socket);
    return id;
  }

  /**
   * Removes a WebSocket from the clients list.
   * @param id client id
   */
  void remove(final String id) {
    clients.remove(id);
  }

  /**
   * Sends a message to all connected clients.
   * @param message message
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void emit(final Value message) throws QueryException, IOException {
    broadcast(message, null);
  }

  /**
   * Sends a message to all connected clients except to the one with the given id.
   * @param message message
   * @param client The client id (can be {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void broadcast(final Value message, final String client)
      throws QueryException, IOException {

    final ArrayList<WebSocket> list = new ArrayList<>();
    for(final Entry<String, WebSocket> entry : clients.entrySet()) {
      final String id = entry.getKey();
      if(client == null || !client.equals(id)) list.add(entry.getValue());
    }
    send(message, list);
  }

  /**
   * Sends a message to a specific clients.
   * @param message message
   * @param ids client ids
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void send(final Value message, final String... ids) throws QueryException, IOException {
    final ArrayList<WebSocket> list = new ArrayList<>();
    for(final String id : ids) {
      final WebSocket ws = clients.get(id);
      if(ws != null) list.add(ws);
    }
    send(message, list);
  }

  /**
   * Returns the client with the specified id.
   * @param id client id
   * @return client
   */
  public WebSocket client(final String id) {
    return clients.get(id);
  }

  /**
   * Sends a message to the specified clients.
   * @param message message
   * @param websockets clients
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private static void send(final Value message, final ArrayList<WebSocket> websockets)
      throws QueryException, IOException {

    final ArrayList<Object> values = WsResponse.serialize(message.iter(), new SerializerOptions());
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
