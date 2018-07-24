package org.basex.http.ws.adapter;

import static org.basex.http.web.WebText.*;

import java.util.*;

import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.query.ann.*;
import org.basex.query.value.*;
import org.basex.util.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a abstract WebsocketAdapter.
 * @author BaseX Team 2005-18, BSD License
 */
public abstract class WsAdapter  extends WebSocketAdapter {
  /**
   * The Websocketconnection.
   */
  protected WsConnection wsconnection;

  /**
   * The Path.
   */
  protected WsPath path;

  /**
   * The UUID of the Websocket.
   */
  protected String id;

  /**
   * The HeaderParams.
   */
  protected Map<String, String> headerParams = new HashMap<>();

  /**
   * Map with the Attributes.
   * */
  protected Map<String, Value> attributes = new HashMap<>();

  /**
   * The Serializer for specific Subprotocols.
   */
  protected WsResponse response;

  /**
   * Sets a Attribute of the WebsocketAttributes.
   * @param key The String key
   * @param value The Value to put in
   * */
  public void setAttribute(final String key, final Value value) {
    attributes.put(key, value);
  }

  /**
   * Returns a specific Attribute.
   * @param key The String key
   * @return The requested Attribute
   * */
  public Value getAttribute(final String key) {
    return attributes.get(key);
  }

  /**
   * Deletes a Attribute.
   * @param key The String key.
   * */
  public void delete(final String key) {
    attributes.remove(key);
  }

  /**
   * Returns the String Path of the WebsocketClient.
   * @return String Path
   * */
  public String getPath() {
    return path.toString();
  }

  @Override
  public void onWebSocketConnect(final Session sess) {
    super.onWebSocketConnect(sess);
    UpgradeRequest ur = sess.getUpgradeRequest();

    if(this.path != null) {
      id = WsPool.getInstance().joinChannel(this, path.toString());
    } else {
      id = WsPool.getInstance().join(this);
    }

    // Add Headers (for binding them to the XQueryParameters in the
    // corresponding bind Method
    headerParams.put("Http-Version", ur.getHttpVersion());
    headerParams.put("Origin", ur.getOrigin());
    headerParams.put("Protocol-version", ur.getProtocolVersion());
    headerParams.put("QueryString", ur.getQueryString());
    headerParams.put("IsSecure", String.valueOf(ur.isSecure()));
    headerParams.put("RequestURI", ur.getRequestURI().toString());
    List<String> headerKeys = new ArrayList<>();
    Collections.addAll(headerKeys, "Host", "Sec-WebSocket-Version");
    sess.getUpgradeRequest().getHeaders().forEach((k, v) -> {
      if(headerKeys.contains(k)) {
        headerParams.put(k, v.get(0));
      }
    });

    wsconnection = new WsConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(), sess,
        this.path.toString());
    findAndProcess(Annotation._WS_CONNECT, null, headerParams);
  }

  /*
   * This is a way for the internal implementation to notify of exceptions occured during the
   * processing of websocket. Usually this occurs from bad / malformed incoming packets. (example:
   * bad UTF8 data, frames that are too big, violations of the spec). This will result in the
   * Session being closed by the implementing side.
   */
  @Override
  public void onWebSocketError(final Throwable cause) {
    this.removeWebsocketFromPool();
    findAndProcess(Annotation._WS_ERROR, cause.toString(), headerParams);
    super.getSession().close();
  }

  @Override
  public void onWebSocketClose(final int statusCode, final String reason) {
    findAndProcess(Annotation._WS_CLOSE, null, null);
    super.onWebSocketClose(statusCode, reason);
    this.removeWebsocketFromPool();
  }

  /**
   * Implement the removing of the Websocket from the Pool. Remove from all
   * Channels in the Pool too.
   */
  protected abstract void removeWebsocketFromPool();

  /**
   * Finds a WSFunction and processes it.
   * @param ann The Websocketannotation
   * @param msg The Message
   * @param header The headers to set
   */
  protected void findAndProcess(final Annotation ann, final Object msg,
      final Map<String, String> header) {
    final WebModules rxm = WebModules.get(wsconnection.context);
    WsFunction func = null;
    try {
      func = rxm.find(wsconnection, ann);
      // If no matching XQuery-Function, throw Error
      if(func == null) {
        wsconnection.error(Util.info(XQUERY_MISSING_X, ann.toString()), 500);
        }
      if(func != null && response != null) func.process(wsconnection, msg, response, header, id);
    } catch(Exception e) {
      wsconnection.error(e.getMessage(), 500);
    }
  }
}
