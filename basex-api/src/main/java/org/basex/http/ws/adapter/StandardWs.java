package org.basex.http.ws.adapter;

import java.util.*;

import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.query.ann.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Websocket. It implements WebsocketAdapter of jetty-native-websockets.
 * Remeber: Each Client has its own instance of this Websocket!
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class StandardWs extends WebSocketAdapter {
  /**
   * The Websocketconnection.
   */
  private WsConnection wsconnection;

  /**
   * The Path.
   */
  private WsPath path;

  /**
   * The UUID of the Websocket.
   * */
  private String id;

  /**
   * The StandardSerializer.
   */
  private WsResponse response = new WsStandardResponse();

  /**
   * The HeaderParams.
   * */
  private Map<String, String> headerParams = new HashMap<>();

  /**
   * Constructor.
   * @param pPath as a String
   * */
  public StandardWs(final String pPath) {
    super();
    path = new WsPath(pPath);
  }

  @Override
  public void onWebSocketConnect(final Session sess) {
    // Sets Session and Remote in Superclass
    super.onWebSocketConnect(sess);

    UpgradeRequest ur = sess.getUpgradeRequest();

    // Add to the WebSocketRoom
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
    headerParams.put("id", id);
    // The Headers of the upgraderequest we are interested in
    List<String> headerKeys = new ArrayList<>();
    Collections.addAll(headerKeys, "Host", "Sec-WebSocket-Version");
    sess.getUpgradeRequest().getHeaders().forEach((k, v) -> {
      if(headerKeys.contains(k)) {
        headerParams.put(k, v.get(0));
      }
    });
    ;

    // Create new WebsocketConnection
    wsconnection = new WsConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(),
        sess, this.path.toString());
    findAndProcess(Annotation._WS_CONNECT, null, headerParams);
  }

  @Override
  public void onWebSocketText(final String message) {
    Annotation ann = Annotation._WS_MESSAGE;
    findAndProcess(ann, message, headerParams);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    System.out.println("On binary: ");
    System.out.println(payload);
    headerParams.put("offset", "" + offset);
    headerParams.put("len", "" + len);
    headerParams.put("id", id);
    findAndProcess(Annotation._WS_MESSAGE, payload, headerParams);
  }

  @Override
  public void onWebSocketClose(final int statusCode, final String reason) {
    findAndProcess(Annotation._WS_CLOSE, null, null);

    // Resets Session and Remote in Superclass
    super.onWebSocketClose(statusCode, reason);

    // Remove the user from the Room
    if(path == null) {
      WsPool.getInstance().remove(id);
    } else {
      WsPool.getInstance().removeFromChannel(this, path.toString(), id);
    }
  }

  /*
   * This is a way for the internal implementation to notify of exceptions occured during the
   * processing of websocket. Usually this occurs from bad / malformed incoming packets. (example:
   * bad UTF8 data, frames that are too big, violations of the spec). This will result in the
   * Session being closed by the implementing side.
   */
  @Override
  public void onWebSocketError(final Throwable cause) {
    findAndProcess(Annotation._WS_ERROR, cause.toString(), headerParams);
    cause.printStackTrace(System.err);
    super.getSession().close();
  }

  /**
   * Finds a WSFunction and processes it.
   * @param ann The Websocketannotation
   * @param msg The Message
   * @param header The headers to set
   */
  private void findAndProcess(final Annotation ann, final Object msg,
      final Map<String, String> header) {
    final RestXqModules rxm = RestXqModules.get(wsconnection.context);
    // select the closest match for this request
    WsFunction func = null;
    try {
      func = rxm.find(wsconnection, ann);
      if(func == null) wsconnection.error(HTTPCode.NO_XQUERY.toString(), 500);
      if(func != null && response != null) func.process(wsconnection, msg, response, header);
    } catch(Exception e) {
      e.printStackTrace();
      wsconnection.error(e.getMessage(), 500);
    }
  }
}
