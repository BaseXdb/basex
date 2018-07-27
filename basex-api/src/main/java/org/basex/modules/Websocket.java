package org.basex.modules;

import static org.basex.http.web.WebText.*;

import org.basex.http.*;
import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * This module contains functions for processing WebSockets.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Websocket extends QueryModule {

  /**
   * Emits the Message to all connected Members.
   * @param message Object
   */
  public void emit(final Object message) {
    WsPool.getInstance().emit(message);
  }

  /**
   * Broadcasts a Message to all connected Members without the sender.
   * @param message Object
   * @throws QueryException Query Exception
   */
  public void broadcast(final Object message) throws QueryException {
    Object id = queryContext.getProperty(HTTPText.WS);
    if(id == null) throw new QueryException(NO_WSID_FOUND);
    try {
      Str sId = Str.get((String) id);
      WsPool.getInstance().broadcast(message, sId);
    } catch(Exception e) {
      throw new QueryException(WS_CAST_FAILED);
    }
  }

  /**
   * Sends a Message to a specific Member.
   * @param message Object
   * @param id Specific id
   */
  public void send(final Object message, final Str id) {
    WsPool.getInstance().send(message, id);
  }

  /**
   * Returns the ID of the current WebSocketClient.
   * @return String id of the current WebSocketClient
   * @throws QueryException QueryException
   */
  public Str id() throws QueryException {
    Object id = queryContext.getProperty(HTTPText.WS);
    if(id == null) throw new QueryException(NO_WSID_FOUND);
    try {
      Str sId = Str.get((String) id);
      return sId;
    } catch(Exception e) {
      throw new QueryException(WS_CAST_FAILED);
    }
  }

  /**
   * Returns the IDs of the all connected WebSocketClients.
   * @return Sequence of Strings of the ids of the connected WebSocketClients
   */
  public Value ids() {
    return WsPool.getInstance().ids();
  }

  /**
   * Returns a Websocket attribute of the current WebsocketClient.
   * @param key key to be requested
   * @return session attribute
   * @throws QueryException QueryException
   */
  public Value get(final Str key) throws QueryException {
    Object id = queryContext.getProperty(HTTPText.WS);
    if(id == null) throw new QueryException(NO_WSID_FOUND);
    try {
      return get(Str.get((String) id), key);
    } catch(Exception e) {
      throw new QueryException(WS_CAST_FAILED);
    }
  }

  /**
   * Returns a Websocket attribute.
   * @param id of the WebsocketClient
   * @param key key to be requested
   * @return session attribute
   */
  public Value get(final Str id, final Str key) {
    return WsPool.getInstance().getAttribute(id.toJava(), key.toJava());
  }

  /**
   * Updates a Websocket Attribute of the current WebsocketClient.
   * @param key The key of the Attribute
   * @param value The Value of the Attribute
   * @throws QueryException QueryException
   */
  public void set(final Str key, final Value value) throws QueryException {
    Object id = queryContext.getProperty(HTTPText.WS);
    if(id == null) throw new QueryException(NO_WSID_FOUND);
    try {
      set(Str.get((String) id), key, value);
    } catch(Exception e) {
      throw new QueryException(WS_CAST_FAILED);
    }
  }

  /**
   * Updates a Websocket attribute.
   * @param id of the Websocketclient
   * @param key key of the attribute
   * @param value value to be stored
   */
  public void set(final Str id, final Str key, final Value value) {
    WsPool.getInstance().setAttribute(id.toJava(), key.toJava(), value);
  }

  /**
   * Removes a session attribute from the current WebsocketClient.
   * @param key The key of the attribute
   * @throws QueryException Query Exception
   */
  public void delete(final Str key) throws QueryException {
    Object id = queryContext.getProperty(HTTPText.WS);
    if(id == null) throw new QueryException(NO_WSID_FOUND);
    try {
      delete(Str.get((String) id), key);
    } catch(Exception e) {
      throw new QueryException(WS_CAST_FAILED);
    }
  }

  /**
   * Removes a session attribute.
   * @param id of the WebsocketClient
   * @param key key of the attribute
   */
  public void delete(final Str id, final Str key) {
    WsPool.getInstance().delete(id.toJava(), key.toJava());
  }

  /**
   * Returns the Path of the WebsocketClient.
   * @param id of the WebsocketClient
   * @return Str the String of the Path of the WebsocketClient
   */
  public Str path(final Str id) {
    return Str.get(WsPool.getInstance().path(id.toJava()));
  }

  /**
   * Returns the Path of the current WebsocketClient.
   * @throws QueryException QueryException
   * @return Str the path
   */
  public Str path() throws QueryException {
    Object id = queryContext.getProperty(HTTPText.WS);
    if(id == null) throw new QueryException(NO_WSID_FOUND);
    try {
      return path(Str.get((String) id));
    } catch(Exception e) {
      throw new QueryException(WS_CAST_FAILED);
    }
  }
}
