package org.basex.http.ws;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.http.ws.adapter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Pool for Websockets. It manages all Websockets which are connected.
 *
 * @author BaseX Team 2005-18, BSD License
 */

public class WsPool {
  /**
   * The Pool-Singleton.
   */
  private static final WsPool INSTANCE = new WsPool();

  /**
   * Returns the Pool instance.
   * @return INSTANCE Pool
   */
  public static WsPool getInstance() {
    return INSTANCE;
  }

  /**
   * The members of the Pool. ID -> WsAdapter.
   */
  private HashMap<String, WsAdapter> members = new HashMap<>();

  /**
   * The members of the Pool. WsAdapter -> List<ID>.
   */
  private HashMap<WsAdapter, List<String>> membersInv = new HashMap<>();

  /**
   * The List of Members per Channel.
   */
  private HashMap<String, List<WsAdapter>> channels = new HashMap<>();

  /**
   * Updates a Attribute in the WebsocketClient with the id id.
   * @param id String id of the WebsocketClient
   * @param key String key of the Attribute
   * @param value Value value of the Attribute
   * */
  public void setAttribute(final String id, final String key, final Value value) {
    members.get(id).setAttribute(key, value);
  }

  /**
   * Returns a Attribute of the WebsocketClient with the id id.
   * @param id String id of the WebsocketClient
   * @param key String key of the Attribute.
   * @return Value the requested Value
   * */
  public Value getAttribute(final String id, final String key) {
    return members.get(id).getAttribute(key);
  }

  /**
   * Deletes a Attribute of the WebsocketClient with the id id.
   * @param id String id of the WebsocketClient
   * @param key String key of the Attribute
   * */
  public void delete(final String id, final String key) {
    members.get(id).delete(key);
  }

  /**
   * Returns the String path of a WebsocketClient with the Id id.
   * @param id String id of the WebsocketClient
   * @return String the WebsocketPathString
   * */
  public String path(final String id) {
    return members.get(id).getPath();
  }

  /**
   * Returns the IDs of all connected WebSocketClients.
   * @return IDs of connected WebSocketClients
   * */
  public Value ids() {
    byte[][] theids = new byte[members.size()][];
    int idx = 0;
    for(String key : members.keySet()) {
      theids[idx] = key.getBytes(StandardCharsets.UTF_8);
      idx++;
    }
    return StrSeq.get(theids);
  }

  /**
   * Adds a WebSocket to the membersList.
   * @param socket WsAdapter
   * @return string The Unique Id of the WebSocketInstance
   */
  public String join(final WsAdapter socket) {
    String uniqueID = UUID.randomUUID().toString();
    members.put(uniqueID, socket);

    List<String> membersinvids = membersInv.get(socket);
    if(membersinvids == null) {
      membersinvids = new ArrayList<>();
    }
    membersinvids.add(uniqueID);
    membersInv.put(socket, membersinvids);
    return uniqueID;
  }

  /**
   * Adds a Websocket to a specific Channel.
   * @param socket WsAdapter
   * @param channel String
   * @return string The Unique ID of the WebSocketInstance
   */
  public String joinChannel(final WsAdapter socket, final String channel) {
    String uniqueID = UUID.randomUUID().toString();
    members.put(uniqueID, socket);
    // Add uniqueId to membersinvid-list
    List<String> membersinvids = membersInv.get(socket);
    if(membersinvids == null) {
      membersinvids = new ArrayList<>();
    }
    membersinvids.add(uniqueID);
    membersInv.put(socket, membersinvids);

    List<WsAdapter> channelMembers = channels.get(channel);
    if(channelMembers == null) {
      channelMembers = new ArrayList<>();
    }
    channelMembers.add(socket);
    channels.put(channel, channelMembers);
    return uniqueID;
  }

  /**
   * Removes a Websocket from the MembersList.
   * @param id String
   */
  public void remove(final String id) {
    WsAdapter socket = members.get(id);
    members.remove(id);
    membersInv.remove(socket);
  }

  /**
   * Removes a Member from the ChannelMemberList.
   * @param socket WsAdapter
   * @param channel String
   * @param id String
   */
  public void removeFromChannel(final WsAdapter socket, final String channel,
      final String id) {
    WsAdapter socketM = members.get(id);
    members.remove(id);

    List<String> membersinvids = membersInv.get(socketM);
    if(membersinvids == null) {
      membersinvids = new ArrayList<>();
    }
    membersinvids.remove(id);
    if(membersinvids.isEmpty()) {
      membersInv.remove(socketM);
    }
    membersInv.put(socketM, membersinvids);

    List<WsAdapter> channelMembers = channels.get(channel);
    // Channel list doesnt exist, return
    if(channelMembers == null) {
      return;
    }
    channelMembers.remove(socket);
    channels.put(channel, channelMembers);
  }

  /**
   * Sends a Message to all connected Members.
   * @param message Object
   */
  public void emit(final Object message) {
    if(message == null) return;
    try {
      members.forEach((k, v) -> {
        if((v.getSession() == null) || (!v.getSession().isOpen())) {
          membersInv.remove(v);
          members.remove(k);
        } else {
          RemoteEndpoint reMember = v.getSession().getRemote();
          checkAndSend(message, reMember);
        }
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends a Message to all connected Members except the one with the id given.
   * @param message Object
   * @param pId The ID
   */
  public void broadcast(final Object message, final Str pId) {
    if(message == null || pId == null) return;
    members.forEach((id, member) -> {
      if(!id.equals(pId.toJava())) {
        if(!member.getSession().isOpen()) {
          membersInv.remove(member);
          members.remove(id);
        } else {
          RemoteEndpoint reMember = member.getSession().getRemote();
          checkAndSend(message, reMember);
        }
      }
    });
  }

  /**
   * Sends a Message to a specific Member.
   * @param message Object
   * @param id String
   */
  public void sendTo(final Object message, final Str id) {
    WsAdapter member = members.get(id.toJava());
    if(member == null) return;
    checkAndSend(message, member.getSession().getRemote());
  }

  /**
   * Checks the Message-Type and sends the Message.
   * @param message Object
   * @param member the RemoteEndpoint
   */
  private void checkAndSend(final Object message, final RemoteEndpoint member) {
    if(message instanceof String) {
      member.sendStringByFuture((String) message);
    } else {
      byte[] bytes = (byte[]) message;
      member.sendBytesByFuture(ByteBuffer.wrap(bytes));
    }
  }
}
