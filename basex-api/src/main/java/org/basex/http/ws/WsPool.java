package org.basex.http.ws;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.Map.*;

import org.basex.http.ws.adapter.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a pool for WebSockets. It manages all connected WebSockets.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */

public class WsPool {
  /** Singleton pool. */
  private static WsPool instance;
  /** WebSocket prefix. */
  private static final String PREFIX = "websocket";
  /** Incrementing id. */
  private static long websocketId = -1;

  /** Clients of the pool. id -> adapter. */
  private final HashMap<String, WsAdapter> clients = new HashMap<>();

  /**
   * Returns the pool instance.
   * @return instance
   */
  public static WsPool get() {
    if(instance == null) instance = new WsPool();
    return instance;
  }

  /**
   * Returns the path of the specified client.
   * @param id client id
   * @return path
   */
  public String path(final String id) {
    return clients.get(id).getPath();
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
   * Returns a new, unused WebSocket id.
   * @return String WebSocket id
   */
  private synchronized String getId() {
      websocketId = Math.max(0, websocketId + 1);
      return PREFIX + websocketId;
  }

  /**
   * Adds a WebSocket to the clients list.
   * @param socket WebSocket
   * @return client id
   * @throws IllegalStateException If HttpSession is not availiable
   */
  public String join(final WsAdapter socket) throws IllegalStateException {
    final String id = getId();
    clients.put(id, socket);
    return id;
  }

  /**
   * Removes a WebSocket from the clients list.
   * @param id client id
   */
  public void remove(final String id) {
    clients.remove(id);
  }

  /**
   * Sends a message to all connected clients.
   * @param message message
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void emit(final Item message) throws QueryException, IOException {
    broadcast(message, null);
  }

  /**
   * Sends a message to all connected clients except to the one with the given id.
   * @param message message
   * @param pId The ID
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void broadcast(final Item message, final Str pId) throws QueryException, IOException {
    for(final Entry<String, WsAdapter> entry : clients.entrySet()) {
      final String id = entry.getKey();
      if(pId == null || !id.equals(pId.toJava())) {
        final WsAdapter ws = entry.getValue();
        final Session s = ws.getSession();
        if(s == null || !s.isOpen()) {
          clients.remove(id);
        } else {
          checkAndSend(message, ws);
        }
      }
    };
  }

  /**
   * Sends a message to a specific client.
   * @param message message
   * @param id client id
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void send(final Item message, final Str id) throws QueryException, IOException {
    final WsAdapter client = clients.get(id.toJava());
    if(client != null) checkAndSend(message, client);
  }

  /**
   * Checks the message type and sends the message.
   * @param message Object
   * @param ws target WebSocket
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void checkAndSend(final Item message, final WsAdapter ws)
      throws QueryException, IOException {

    final RemoteEndpoint remote = ws.getSession().getRemote();
    for(final Object value : serialize(message.iter(), new SerializerOptions())) {
      if(value instanceof byte[]) {
        remote.sendBytesByFuture(ByteBuffer.wrap((byte[]) value));
      } else {
        remote.sendStringByFuture((String) value);
      }
    }
  }

  /**
   * Serializes an XQuery value.
   * @param iter value iterator
   * @param opts serializer options
   * @return serialized values (byte arrays and strings)
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public static ArrayList<Object> serialize(final Iter iter, final SerializerOptions opts)
      throws QueryException, IOException {

    final ArrayList<Object> list = new ArrayList<>();
    final ArrayOutput ao = new ArrayOutput();
    final Serializer ser = Serializer.get(ao, opts);
    for(Item item; (item = iter.next()) != null;) {
      ser.reset();
      ser.serialize(item);
      if(item instanceof Bin) {
        list.add(ao.toArray());
      } else {
        list.add(ao.toString());
      }
      ao.reset();
    }
    return list;
  }
}
