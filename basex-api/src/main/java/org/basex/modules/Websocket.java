package org.basex.modules;

import java.util.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

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
    WsPool.getInstance().broadcast(message);
  }

  /**
   * Broadcasts a Message to all connected Members in the Channel.
   * @param message Object
   * @param channel Str
   */
  public void broadcastChannel(final Object message, final Str channel) {
    WsPool.getInstance().broadcastChannel(message, channel);
  }

  /**
   * Broadcasts a Message to all connected Members except the one with the given Id.
   * @param message Object
   * @param id id to dismiss
   */
  public void broadcastWithoutID(final Object message, final Str id) {
    WsPool.getInstance().broadcastWithoutID(message, id);
  }

  /**
   * Broadcasts a Message to all connected Members in the channel except the one with the given Id.
   * @param message Object
   * @param channel Str
   * @param id id to miss
   */
  public void broadcastChannelWoID(final Object message, final Str channel,
      final Str id) {
    WsPool.getInstance().broadcastChannelWoID(message, channel, id);
  }

  /**
   * Sends a Message to a specific Member.
   * @param message Object
   * @param id Specific id
   */
  public void sendTo(final Object message, final Str id) {
    WsPool.getInstance().sendTo(message, id);
  }
}
