package org.basex.modules;

import java.util.*;

import org.basex.http.ws.*;
import org.basex.query.*;

/**
 * This module contains functions for processing WebSockets.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Websocket extends QueryModule {

  /**
   * Broadcasts a Message to all connected Members.
   * @param message Object
   */
  public void broadcast(final Object message) {
    System.out.println("Broadcast");
    WsPool.getInstance().broadcast(message);
  }


  /**
   * Broadcasts a Message to all connected Members in the Channel.
   * @param message Object
   * @param channel String
   */
  public void broadcast(final Object message, final String channel) {
    WsPool.getInstance().broadcast(message, channel);
  }

  /**
   * Broadcasts a Message to all connected Members except the Ids.
   * @param message Object
   * @param ids List of Ids
   * */
  public void broadcast(final Object message, final List<String> ids) {
    WsPool.getInstance().broadcast(message, ids);
  }

  /**
   * Broadcasts a Message to all connected Members in the channel except the Ids.
   * @param message Object
   * @param channel String
   * @param ids List of Ids
   * */
  public void broadcast(final Object message, final String channel, final List<String> ids) {
    WsPool.getInstance().broadcast(message, channel, ids);
  }
}
