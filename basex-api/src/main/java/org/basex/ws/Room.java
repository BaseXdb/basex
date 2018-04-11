package org.basex.ws;

import java.util.*;

/**
 * This class defines a Room for Websockets. It manages all Websockets which are connected.
 * https://stackoverflow.com/questions/15646213/how-do-i-access-instantiated-websockets-in-jetty-9
 * @TODO: Maybe define different Rooms for different Channels
 *
 * @author BaseX Team 2005-18, BSD License
 */
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
  private List<WebSocke> members = new ArrayList<>();

  /**
   * Adds a WebSocket to the membersList.
   * @param socket WebSocke
   */
  public void join(final WebSocke socket) {
    members.add(socket);
  }

  /**
   * Removes a Websocket from the MembersList.
   * @param socket WebSocke
   */
  public void remove(final WebSocke socket) {
    members.remove(socket);
  }

  /**
   * Sends a Message to all connected Members.
   * @param message String
   */
  public void broadcast(final String message) {
    for(WebSocke member: members) {
      // Sends a String asynchronely, method maybe return before the message was sent
      member.getSession().getRemote().sendStringByFuture(message);
    }
  }
}
