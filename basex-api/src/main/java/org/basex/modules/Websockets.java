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
   * Returns an attribute from the current client.
   * @param id client id
   * @param key key to be requested
   * @return attribute value
   */
  public Value get(final Str id, final Str key) {
    return WsPool.get().getAttribute(id.toJava(), key.toJava());
  }

  /**
   * Assigns an attribute to the specified client.
   * @param id client id
   * @param key key of the attribute
   * @param value value to be stored
   */
  public void set(final Str id, final Str key, final Value value) {
    WsPool.get().setAttribute(id.toJava(), key.toJava(), value);
  }

  /**
   * Removes a session attribute.
   * @param id client id
   * @param key key of the attribute
   */
  public void delete(final Str id, final Str key) {
    WsPool.get().delete(id.toJava(), key.toJava());
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
