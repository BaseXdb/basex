package org.basex.http.ws.adapter;

import static org.basex.http.web.WebText.*;

import java.io.*;
import java.util.*;

import org.basex.http.web.*;
import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.query.ann.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines an abstract WebSocket adapter. It inherits the Jetty WebSocket adapter.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public abstract class WsAdapter extends WebSocketAdapter {
  /** Connection. */
  protected WsConnection connection;
  /** Path. */
  protected WsPath path;
  /** Client id. */
  protected String id;
  /** Header parameters. */
  protected final Map<String, String> headers = new HashMap<>();
  /** Attributes. */
  protected final Map<String, Value> attributes = new HashMap<>();
  /** Response serializer. */
  protected WsResponse response;

  /**
   * Sets an attribute.
   * @param key key
   * @param value value
   */
  public void setAttribute(final String key, final Value value) {
    attributes.put(key, value);
  }

  /**
   * Returns the attribute value for the specified key.
   * @param key key
   * @return attribute value (can be {@code null})
   */
  public Value getAttribute(final String key) {
    return attributes.get(key);
  }

  /**
   * Deletes an attribute.
   * @param key key
   */
  public void delete(final String key) {
    attributes.remove(key);
  }

  /**
   * Returns the client path.
   * @return path
   */
  public String getPath() {
    return path.toString();
  }

  @Override
  public void onWebSocketConnect(final Session session) {
    super.onWebSocketConnect(session);

    final UpgradeRequest ur = session.getUpgradeRequest();
    final WsPool pool = WsPool.get();
    if(path != null) {
      id = pool.joinChannel(this, path.toString());
    } else {
      id = pool.join(this);
    }

    // add headers (for binding them to the XQuery parameters in the corresponding bind method)
    headers.put("Http-Version", ur.getHttpVersion());
    headers.put("Origin", ur.getOrigin());
    headers.put("Protocol-version", ur.getProtocolVersion());
    headers.put("QueryString", ur.getQueryString());
    headers.put("IsSecure", String.valueOf(ur.isSecure()));
    headers.put("RequestURI", ur.getRequestURI().toString());

    final String[] names = { "Host", "Sec-WebSocket-Version" };
    for(final String name : names) {
      final String value = ur.getHeader(name);
      if(value != null) headers.put(name, value);
    }

    connection = new WsConnection(session, path.toString());
    findAndProcess(Annotation._WS_CONNECT, null, headers);
  }

  /*
   * This is a way for the internal implementation to notify of exceptions that occurred during the
   * WebSocket processing. Usually this occurs from bad / malformed incoming packets. (example:
   * bad UTF8 data, frames that are too big, violations of the spec). This will result in the
   * Session being closed by the implementing side.
   */
  @Override
  public void onWebSocketError(final Throwable cause) {
    try {
      findAndProcess(Annotation._WS_ERROR, cause.toString(), headers);
      super.getSession().close();
    } finally {
      removeWebSocket();
    }
  }

  @Override
  public void onWebSocketClose(final int statusCode, final String reason) {
    try {
      findAndProcess(Annotation._WS_CLOSE, null, null);
      super.onWebSocketClose(statusCode, reason);
    } finally {
      removeWebSocket();
    }
  }

  /**
   * Removes the WebSocket from the pool. Removes it from all channels in the pool as well.
   */
  protected abstract void removeWebSocket();

  /**
   * Finds a function and processes it.
   * @param ann annotation
   * @param message message (can be {@code null}; otherwise string or byte array)
   * @param header headers to set
   */
  protected void findAndProcess(final Annotation ann, final Object message,
      final Map<String, String> header) {

    final WebModules modules = WebModules.get(connection.context);
    try {
      final  WsFunction func = modules.find(connection, ann);
      if(func == null) connection.error(Util.info(WS_MISSING_X, ann.toString()), 500);
      else if(response != null) func.process(connection, message, response, header, id);
    } catch(final Exception ex) {
      try {
        connection.error(ex.getMessage(), 500);
      } catch(final IOException e) { /* ignore error */ }
    }
  }
}
