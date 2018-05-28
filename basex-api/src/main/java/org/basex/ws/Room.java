package org.basex.ws;

import java.util.*;

import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Room for Websockets. It manages all Websockets which are connected.
 * https://stackoverflow.com/questions/15646213/how-do-i-access-instantiated-websockets-in-jetty-9
 * @TODO: Maybe define different Rooms for different Channels
 *
 * @author BaseX Team 2005-18, BSD License
 */

// TODO: Check id-header in the SUBSCRIBTION
public class Room {
  /**
   * The Room-Singleton.
   * */
  private static final Room INSTANCE = new Room();

  /**
   * Returns the Room instance.
   * @return INSTANCE Room
   */
  public static Room getInstance() {
    return INSTANCE;
  }

  /**
   * The members of the Room.
   */
  private List<WebSocketAdapter> members = new ArrayList<>();

  /**
   * The List of Members per Channel.
   */
  private HashMap<String, List<WebSocketAdapter>> channels = new HashMap<>();

  /**
   * Adds a WebSocket to the membersList.
   * @param socket WebSocke
   */
  public void join(final WebSocketAdapter socket) {
    members.add(socket);
  }

  /**
   * Adds a Websocket to a specific Channel.
   * @param socket WebSocke
   * @param channel String
   * */
  public void joinChannel(final WebSocketAdapter socket, final String channel) {
    List<WebSocketAdapter> channelMembers = channels.get(channel);
    if(channelMembers == null) {
      channelMembers = new ArrayList<>();
    }
    channelMembers.add(socket);
    channels.put(channel, channelMembers);
  }

  /**
   * Removes a Websocket from the MembersList.
   * @param socket WebSocke
   */
  public void remove(final WebSocketAdapter socket) {
    members.remove(socket);
  }

  /**
   * Removes a Member from the ChannelMemberList.
   * @param socket WebSocke
   * @param channel String
   */
  public void removeFromChannel(final WebSocketAdapter socket, final String channel) {
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
   * @param message String
   */
  public void broadcast(final String message) {
    for(WebSocketAdapter member: members) {
      // Sends a String asynchronely, method maybe return before the message was sent
      member.getSession().getRemote().sendStringByFuture(message);
    }
  }

  /**
   * Sens a Message to all connected Members in the Channel.
   * @param message String
   * @param channel String
   * */
  // TODO: Maybe not todo her but todo: what if the connection closed of one websocketclient
  // in the channel?
  public void broadcast(final String message, final String channel) {
    List<WebSocketAdapter> channelMembers = channels.get(channel);
    // Channel list doesnt exist, return
    if(channelMembers == null) {
      return;
    }
    for(WebSocketAdapter member: channelMembers) {
      // Sends a String asynchronely, method maybe return before the message was sent
      member.getSession().getRemote().sendStringByFuture(message);
    }
  }
}
