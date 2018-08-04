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
import org.basex.query.value.*;
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

  /** Members of the pool. id -> adapter. */
  private final HashMap<String, WsAdapter> members = new HashMap<>();
  /** Members of the pool. adapter -> ids. */
  private final HashMap<WsAdapter, List<String>> membersInv = new HashMap<>();
  /** Members per channel. */
  private final HashMap<String, List<WsAdapter>> channels = new HashMap<>();

  /**
   * Returns the pool instance.
   * @return instance
   */
  public static WsPool get() {
    if(instance == null) instance = new WsPool();
    return instance;
  }

  /**
   * Assigns an attribute to the specified client.
   * @param id client id
   * @param key key of the attribute
   * @param value value to be stored
   */
  public void setAttribute(final String id, final String key, final Value value) {
    members.get(id).setAttribute(key, value);
  }

  /**
   * Returns the attribute value for the specified key and the current client.
   * @param id client id
   * @param key key to be requested
   * @return attribute value
   */
  public Value getAttribute(final String id, final String key) {
    return members.get(id).getAttribute(key);
  }

  /**
   * Removes a session attribute.
   * @param id client id
   * @param key key of the attribute
   */
  public void delete(final String id, final String key) {
    members.get(id).delete(key);
  }

  /**
   * Returns the path of the specified client.
   * @param id client id
   * @return path
   */
  public String path(final String id) {
    return members.get(id).getPath();
  }

  /**
   * Returns the ids of all connected clients.
   * @return client ids
   */
  public TokenList ids() {
    final TokenList ids = new TokenList(members.size());
    for(final String key : members.keySet()) ids.add(key);
    return ids;
  }

  /**
   * Adds a WebSocket to the members list.
   * @param socket WebSocket
   * @return client id
   * @throws IllegalStateException If HttpSession is not availiable
   */
  public String join(final WsAdapter socket) throws IllegalStateException {
    final String id = getHttpSessionId(socket);
    members.put(id, socket);
    membersInv.computeIfAbsent(socket, k -> new ArrayList<>(1)).add(id);
    return id;
  }

  /**
   * Adds a WebSocket to a specific channel.
   * @param socket WebSocket
   * @param channel name of channel
   * @return client id
   * @throws IllegalStateException If HttpSession is not availiable
   */
  public String joinChannel(final WsAdapter socket, final String channel)
      throws IllegalStateException {
    final String id = getHttpSessionId(socket);
    members.put(id, socket);
    membersInv.computeIfAbsent(socket, k -> new ArrayList<>(1)).add(id);
    channels.computeIfAbsent(channel, k -> new ArrayList<>(1)).add(socket);
    return id;
  }

  /**
   * Removes a WebSocket from the members list.
   * @param id client id
   */
  public void remove(final String id) {
    final WsAdapter ws = members.get(id);
    members.remove(id);
    membersInv.remove(ws);
  }

  /**
   * Removes a member from a channel.
   * @param socket WebSocket
   * @param channel name of channel
   * @param id client id
   */
  public void removeFromChannel(final WsAdapter socket, final String channel,
      final String id) {
    final WsAdapter ws = members.get(id);
    members.remove(id);

    final List<String> inv = membersInv.get(ws);
    if(inv != null) {
      inv.remove(id);
      if(inv.isEmpty()) membersInv.remove(ws);
    }
    final List<WsAdapter> list = channels.get(channel);
    if(list != null) list.remove(socket);
  }

  /**
   * Sends a message to all connected members.
   * @param message message
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void emit(final Item message) throws QueryException, IOException {
    for(final Entry<String, WsAdapter> entry : members.entrySet()) {
      final String id = entry.getKey();
      final WsAdapter ws = entry.getValue();
      if(!ws.getSession().isOpen()) {
        membersInv.remove(ws);
        members.remove(id);
      } else {
        checkAndSend(message, ws);
      }
    }
  }

  /**
   * Sends a message to all connected members except to the one with the given id.
   * @param message message
   * @param pId The ID
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void broadcast(final Item message, final Str pId) throws QueryException, IOException {
    for(final Entry<String, WsAdapter> entry : members.entrySet()) {
      final String id = entry.getKey();
      if(!id.equals(pId.toJava())) {
        final WsAdapter ws = entry.getValue();
        final Session s = ws.getSession();
        if(s == null || !s.isOpen()) {
          membersInv.remove(ws);
          members.remove(id);
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
    final WsAdapter member = members.get(id.toJava());
    if(member != null) checkAndSend(message, member);
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

  /**
   * Returns the HttpSessionId if the HttpSession is set and alive.
   * @param socket The WsAdapter
   * @return The HttpSessionId as String
   * @throws IllegalStateException If HttpSession is not availiable
   */
  private String getHttpSessionId(final WsAdapter socket) throws IllegalStateException {
    String id = null;
    if(socket.httpsession != null) {
      try {
        id = socket.httpsession.getId();
      } catch(IllegalStateException ex) {
        throw ex;
      }
    }
    return id;
  }
}
