package org.basex.http.ws;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import org.basex.http.ws.stomp.*;
import org.basex.http.ws.stomp.frames.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
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

  /** Clients of the channels. channel -> list of websocketids */
  private final ConcurrentHashMap<String, List<String>> channelWebsocketid = new ConcurrentHashMap<>();

  /** Open Messages -> Messages which want to be acknolodget but havnt yet */
  private final Map<String,SortedSet<MessageObject>> notAckedMessages = new HashMap<>();
  /** Acked Messages */
  private final Map<String,SortedSet<MessageObject>> ackedMessages = new HashMap<>();
  /** Map of MessageIds with their stompid */
  private final Map<String,String> messageIdStompId = new HashMap<>();

  /**
   * Joins a Channel.
   * @param channel channel
   * @param websocketid websocketid
   */
  public void joinChannel(final String channel, final String websocketid) {
    List<String> websocketids = channelWebsocketid.get(channel);
    if(websocketids == null) {
      websocketids = new ArrayList<>();
    }
    websocketids.add(websocketid);
    channelWebsocketid.put(channel, websocketids);
  }

  /**
   * Leaves a Channel.
   * @param channel channel
   * @param websocketid websocketid
   */
  public void leaveChannel(final String channel, final String websocketid) {
    List<String> websocketids = channelWebsocketid.get(channel);
    if(websocketids == null) return;
    websocketids.remove(websocketid);
    channelWebsocketid.put(channel, websocketids);
  }
  /**
   * Closes the WebsocketConnection.
   * @param id The id of the Websocket.
   * @param reason The String reason
   * */
  public void closeWebsocket(final String id, final String reason) {
    clients.get(id).closeWebsocket(reason);
  }
  /**
   * Returns the pool instance.
   * @return instance
   */
  public static synchronized WsPool get() {
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
  public StringList ids() {
    final StringList ids = new StringList(clients.size());
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
  public void emit(final Item message) throws QueryException, IOException {
    broadcast(message, null);
  }

  /**
   * Sends a message to all connected clients except to the one with the given id.
   * @param message message
   * @param client The client id (can be {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void broadcast(final Item message, final String client)
      throws QueryException, IOException {

    final ArrayList<WebSocket> list = new ArrayList<>();
    for(final Entry<String, WebSocket> entry : clients.entrySet()) {
      final String id = entry.getKey();
      if(client == null || !client.equals(id)) list.add(entry.getValue());
    };
    send(message, list);
  }

  /**
   * Removes a Message from the not acked-list and puts it to the acked list.
   * @param wsId Id of the Websocket
   * @param messageId Id of the Message
   * */
  public void ackMessage(final String wsId, final String messageId) {
    SortedSet<MessageObject> messages = notAckedMessages.get(wsId);
    if(messages == null) return;
    Iterator<MessageObject> it = messages.iterator();
    while(it.hasNext()) {
      MessageObject object = it.next();
      if(object.getMessageId().equals(messageId)) {
        SortedSet<MessageObject> toAck = new TreeSet<>();
        toAck.add(object);
        ackedMessages.put(wsId, ackMessages(toAck,messages,wsId));
      }
    }
  }

  /** Removes the Message with the id and all older messages from the not acked list and add
   * it to the acked list.
   * @param wsId Id of the Websocket
   * @param messageId Id of the message
   * */
  public void ackMessages(final String wsId, final String messageId) {
    SortedSet<MessageObject> messages = notAckedMessages.get(wsId);
    if(messages == null) return;
    Iterator<MessageObject> it = messages.iterator();
    while(it.hasNext()) {
      MessageObject object = it.next();
      if(object.getMessageId().equals(messageId)) {
        SortedSet<MessageObject> toAck = messages.subSet(messages.first(), object);
        toAck.add(object);
        ackedMessages.put(wsId, ackMessages(toAck, messages, wsId));
      }
    }
  }

  /**
   * Discards a Message from the not-Acked-List and returns the messageobject
   * @param wsId Id of the Websocket
   * @param messageId Id of the message
   * @return MessageObject the message object
   * */
  public MessageObject discardMessage(final String wsId, final String messageId) {
    SortedSet<MessageObject> messages = notAckedMessages.get(wsId);
    if(messages == null) return null;
    Iterator<MessageObject> it = messages.iterator();
    while(it.hasNext()) {
      MessageObject object = it.next();
      if(object.getMessageId().equals(messageId)) {
        messages.remove(object);
        return object;
      }
    }
    return null;
  }
  /**
   * Deletes the messages to ack from the not acked set and adds it to the acked set
   * @param toAck SortedSet of Messages to Ack
   * @param notAcked SortedSet of Messages which are not acked yet
   * @param wsId The WsID
   * @return the new notAcked-SortedSet
   */
  private SortedSet<MessageObject> ackMessages(SortedSet<MessageObject> toAck,
                                               SortedSet<MessageObject> notAcked,
                                               String wsId) {
    Iterator<MessageObject> it = toAck.iterator();
    while(it.hasNext()) {
      // Get the message to Ack
      MessageObject ackme = it.next();
      // Remote the message to ack from the not acked set
      notAcked.remove(ackme);
      // Add the Message to the ackedMessages
      SortedSet<MessageObject> ackMsg = ackedMessages.get(wsId);
      if(ackMsg == null) ackMsg = new TreeSet<>();
      ackMsg.add(ackme);
      ackedMessages.put(wsId, ackMsg);
    }
    return notAcked;
  }

  /**
   * Returns the stompId to the messageId
   * @param messageId the Messageid
   * @return the stompid
   * */
  public String getStompIdToMessageId(final String messageId) {
    return messageIdStompId.get(messageId);
  }
  /**
   * Sends a Message to a Channel
   * @param message message
   * @param channel channel
   */
  public void sendChannel(final Str message, final Str channel) {
     final List<String> listWebsocketids = channelWebsocketid.get(channel.toJava());
     for(final String id : listWebsocketids) {
       final WebSocket ws = clients.get(id);
       final StompV12WebSocket sws = (StompV12WebSocket) ws;
       // Send the message here because each message has another messageid
       if(sws != null) {
         String messageUID = UUID.randomUUID().toString();
         String stompId = sws.getStompId(channel.toJava());
         String ackmode = sws.getAckMode(stompId);
         String wsId = sws.id;
         // Set the headers of the MessageFrame
         Map<String, String> headers = new HashMap<>();
         headers.put("destination", channel.toJava());
         headers.put("message-id", messageUID);
         headers.put("subscription", stompId);
         headers.put("content-length", "" + message.toJava().length());
         headers.put("content-type", "text/plain");
         // Check if ACK Required, add it to notAckedMessages if required
         if(ackmode != null && !ackmode.equals("auto")) {
           SortedSet<MessageObject> messages = notAckedMessages.get(wsId);
           if(messages == null) messages = new TreeSet<>();
           messages.add(new MessageObject(messageUID, message.toJava(),wsId));
           notAckedMessages.put(wsId, messages);
         }
         // Add the Messageid with its stompId to the messageIdStompId-Map
         messageIdStompId.put(messageUID, stompId);

         // Create MessageFrame and send it to the RemoteEndpoint
         MessageFrame mf = new MessageFrame(Commands.MESSAGE, headers, message.toJava());
         final RemoteEndpoint remote = sws.getSession().getRemote();
         remote.sendStringByFuture(mf.serializedFrame());
       }
     }
  };

  /**
   * Sends a message to a specific clients.
   * @param message message
   * @param ids client ids
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void send(final Item message, final String... ids) throws QueryException, IOException {
    final ArrayList<WebSocket> list = new ArrayList<>();
    for(final String id : ids) {
      final WebSocket ws = clients.get(id);
      if(ws != null) list.add(ws);
    }
    send(message, list);
  }

  /**
   * Sends a message to the specified clients.
   * @param message message
   * @param websockets clients
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void send(final Item message, final ArrayList<WebSocket> websockets)
      throws QueryException, IOException {

    /* [JF] We need to check what happens if a client says goodbye while we send data.
     *  if this happens, other clients must still receive their message.
     *  this could be tested by calling Performance.sleep(...) before sending the data. */
    final ArrayList<Object> values = WsResponse.serialize(message.iter(), new SerializerOptions());
    for(final WebSocket ws : websockets) {
      if(!ws.isConnected()) continue;
      final RemoteEndpoint remote = ws.getSession().getRemote();
      for(final Object value : values) {
        if(value instanceof byte[]) {
          remote.sendBytesByFuture(ByteBuffer.wrap((byte[]) value));
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
