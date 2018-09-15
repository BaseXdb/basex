package org.basex.http.ws;

import javax.servlet.http.*;

import org.basex.http.web.*;
import org.basex.http.ws.stomp.*;
import org.basex.query.ann.*;
import org.basex.util.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines an abstract WebSocket. It inherits the Jetty WebSocket adapter.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class StompV12WebSocket extends WebSocket {

  /**
   * Constructor.
   * @param req request
   * @param subprotocol subprotocol
   */
  StompV12WebSocket(final HttpServletRequest req, final String subprotocol) {
    super(req,subprotocol);
  }

  /**
   * Creates a new WebSocket instance.
   * @param req request
   * @param subprotocol subprotocol
   * @return WebSocket or {@code null}
   */
  static StompV12WebSocket get(final HttpServletRequest req, final String subprotocol) {
    final StompV12WebSocket ws = new StompV12WebSocket(req, subprotocol);
    try {
      if(!WebModules.get(ws.context).findWs(ws, null).isEmpty()) return ws;
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
    return null;
  }

  @Override
  public void onWebSocketText(final String message) {
    StompFrame stompframe = parseStompFrame(message);
    if(stompframe == null) return;


    findAndProcess(Annotation._WS_MESSAGE, message);
  }


  /**
   * Parses a Stringmessage to a StompFrame.
   * @param message String
   * @return the StompFrame
   */
  private StompFrame parseStompFrame(final String message) {
    StompFrame stompframe = null;
    try {
      stompframe = StompFrame.parse(message);
    } catch(HeadersException e) {
      Util.debug(e);
      throw new CloseException(StatusCode.ABNORMAL, e.getMessage());
    }
    return stompframe;
  }

  /**
   * Finds a function and processes it.
   * @param ann annotation
   * @param message message (can be {@code null}; otherwise string or byte array)
   */
  private void findAndProcess(final Annotation ann, final Object message) {
    // check if an HTTP session exists, and if it still valid
    try {
      if(session != null) session.getCreationTime();
    } catch(final IllegalStateException ex) {
      session = null;
    }

    try {
      // find function to evaluate
      final WsFunction func = WebModules.get(context).websocket(this, ann);
      if(func != null) new WsResponse(this).create(func, message);
    } catch(final RuntimeException ex) {
      throw ex;
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
  }
}
