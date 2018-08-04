package org.basex.modules;

import java.io.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * This module contains functions for processing global WebSockets.
 * The class name is {@code Websockets} instead of {@code WebSockets}.
 * Otherwise, it would be resolved to {@code web-socket}s in XQuery.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class Websockets {
  /**
   * Returns the ids of the all connected clients.
   * @return client ids
   */
  public Value ids() {
    return StrSeq.get(WsPool.get().ids());
  }

  /**
   * Emits the message to all connected members.
   * @param message message
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void emit(final Item message) throws QueryException, IOException {
    WsPool.get().emit(message);
  }

  /**
   * Sends a message to a specific member.
   * @param message message
   * @param id id
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void send(final Item message, final Str id) throws QueryException, IOException {
    WsPool.get().send(message, id);
  }

  /**
   * Returns the path of the specified client.
   * @param id client id
   * @return path
   */
  public Str path(final Str id) {
    return Str.get(WsPool.get().path(id.toJava()));
  }
}
