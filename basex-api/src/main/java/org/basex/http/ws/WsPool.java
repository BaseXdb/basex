package org.basex.http.ws;

import java.nio.*;
import java.util.*;

import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Room for Websockets. It manages all Websockets which are connected.
 *
 * @author BaseX Team 2005-18, BSD License
 */

public class WsPool {
  /**
   * The Room-Singleton.
   */
  private static final WsPool INSTANCE = new WsPool();

  /**
   * Returns the Room instance.
   * @return INSTANCE Room
   */
  public static WsPool getInstance() {
    return INSTANCE;
  }

  /**
   * The members of the Room.
   */
  private HashMap<String, WebSocketAdapter> members = new HashMap<>();

  /**
   * The members of the Room.
   */
  private HashMap<WebSocketAdapter, String> membersInv = new HashMap<>();

  /**
   * The List of Members per Channel.
   */
  private HashMap<String, List<WebSocketAdapter>> channels = new HashMap<>();

  /**
   * Adds a WebSocket to the membersList.
   * @param socket WebSocke
   * @return string
   */
  public String join(final WebSocketAdapter socket) {
    String uniqueID = UUID.randomUUID().toString();
    members.put(uniqueID, socket);
    membersInv.put(socket, uniqueID);
    return uniqueID;
  }

  /**
   * Adds a Websocket to a specific Channel.
   * @param socket WebSocke
   * @param channel String
   * @return string unique id
   */
  public String joinChannel(final WebSocketAdapter socket, final String channel) {
    String uniqueID = UUID.randomUUID().toString();
    members.put(uniqueID, socket);
    membersInv.put(socket, uniqueID);

    List<WebSocketAdapter> channelMembers = channels.get(channel);
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
    WebSocketAdapter socket = members.get(id);
    members.remove(id);
    membersInv.remove(socket);
  }

  /**
   * Removes a Member from the ChannelMemberList.
   * @param socket WebSocke
   * @param channel String
   * @param id String
   */
  public void removeFromChannel(final WebSocketAdapter socket, final String channel,
      final String id) {
    WebSocketAdapter socketM = members.get(id);
    members.remove(id);
    membersInv.remove(socketM);
    List<WebSocketAdapter> channelMembers = channels.get(channel);
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
  public void broadcast(final Object message) {
    if(message == null) return;
    members.forEach((k, v) -> {
      if(!v.getSession().isOpen()) {
        membersInv.remove(v);
        members.remove(k);
      } else {
        RemoteEndpoint reMember = v.getSession().getRemote();
        checkAndSend(message, reMember);
      }
    });
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

  /**
   * Sends a Message to all connected Members except the ids given.
   * @param message Object
   * @param ids List of Ids
   */
  public void broadcast(final Object message, final List<String> ids) {
    if(message == null || ids == null || ids.size() == 0) return;
    members.forEach((id, member) -> {
      if(ids.contains(id)) {
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
   * Sens a Message to all connected Members in the Channel.
   * @param message String
   * @param channel String
   */
  public void broadcast(final Object message, final String channel) {
    List<WebSocketAdapter> channelMembers = channels.get(channel);
    // Channel list doesnt exist, return
    if(channelMembers == null) {
      return;
    }
    for(WebSocketAdapter member : channelMembers) {
      if(!member.getSession().isOpen()) {
        String id = membersInv.get(member);
        membersInv.remove(member);
        members.remove(id);
      } else {
        RemoteEndpoint reMember = member.getSession().getRemote();
        checkAndSend(message, reMember);
      }
    }
  }

  /**
   * Sends a Message to all connected Members in a Channel except the ids given.
   * @param message Object
   * @param channel String
   * @param ids List of Ids
   */
  public void broadcast(final Object message, final String channel, final List<String> ids) {
    List<WebSocketAdapter> channelMembers = channels.get(channel);
    // Channel list doesnt exist, return
    if(channelMembers == null) {
      return;
    }
    for(WebSocketAdapter member : channelMembers) {
      String id = membersInv.get(member);
      if(ids.contains(id)) {
        if(!member.getSession().isOpen()) {
          membersInv.remove(member);
          members.remove(id);
        } else {
          RemoteEndpoint reMember = member.getSession().getRemote();
          checkAndSend(message, reMember);
        }
      }
    }
  }

  /**
   * Sends a Message to a specific Member.
   * @param message Object
   * @param id String
   *
   * */
  public void sendTo(final Object message, final String id) {
    WebSocketAdapter member = members.get(id);
    if(member == null) return;
    checkAndSend(message, member.getSession().getRemote());
  }

  /**
   * Get all Ids.
   * @return List<String>
   * */
  public List<String> getAllIds() {
    List<String> ids = new ArrayList<>();
    members.forEach((id, adapter) -> {
      ids.add(id);
    });
    return ids;
  }

  /**
   * Get all Ids subscribed to a Channel.
   * @param channel String
   * @return List<String>
   * */
  public List<String> getChannelIds(final String channel) {
    List<String> ids = new ArrayList<>();
    List<WebSocketAdapter> channelMembers = channels.get(channel);
    // Channel list doesnt exist, return
    if(channelMembers == null) {
      return ids;
    }
    for(WebSocketAdapter member : channelMembers) {
        ids.add(membersInv.get(member));
    }
    return ids;
  }
}
