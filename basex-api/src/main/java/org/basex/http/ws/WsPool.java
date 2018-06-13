package org.basex.http.ws;

import java.nio.*;
import java.util.*;

import org.basex.query.value.item.*;
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
   * The members of the Pool. ID -> WebSocketAdapter.
   */
  private HashMap<String, WebSocketAdapter> members = new HashMap<>();

  /**
   * The members of the Pool. WebSocketAdapter -> ID.
   */
  private HashMap<WebSocketAdapter, String> membersInv = new HashMap<>();

  /**
   * The List of Members per Channel.
   */
  private HashMap<String, List<WebSocketAdapter>> channels = new HashMap<>();

  /**
   * Adds a WebSocket to the membersList.
   * @param socket WebSocketAdapter
   * @return string The Unique Id of the WebSocketInstance
   */
  public String join(final WebSocketAdapter socket) {
    String uniqueID = UUID.randomUUID().toString();
    members.put(uniqueID, socket);
    membersInv.put(socket, uniqueID);
    return uniqueID;
  }

  /**
   * Adds a Websocket to a specific Channel.
   * @param socket WebSocketAdapter
   * @param channel String
   * @return string The Unique ID of the WebSocketInstance
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
   * @param socket WebSocketAdapter
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
   * Sends a Message to all connected Members except the one with the id given.
   * @param message Object
   * @param pId The ID
   */
  public void broadcastWithoutID(final Object message, final Str pId) {
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
   * Sends a Message to all connected Members in the Channel.
   * @param message String
   * @param channel String
   */
  public void broadcastChannel(final Object message, final Str channel) {
    List<WebSocketAdapter> channelMembers = channels.get(channel.toJava());
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
   * Sends a Message to all connected Members in a Channel except the one with the given id.
   * @param message Object
   * @param channel Str
   * @param pId Id to dismiss
   */
  public void broadcastChannelWoID(final Object message, final Str channel, final Str pId) {
    List<WebSocketAdapter> channelMembers = channels.get(channel.toJava());
    // Channel list doesnt exist, return
    if(channelMembers == null) {
      return;
    }
    for(WebSocketAdapter member : channelMembers) {
      String id = membersInv.get(member);
      if(!pId.toJava().equals(id)) {
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
   */
  public void sendTo(final Object message, final Str id) {
    WebSocketAdapter member = members.get(id.toJava());
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
