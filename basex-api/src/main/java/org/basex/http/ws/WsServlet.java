package org.basex.http.ws;

import org.eclipse.jetty.websocket.servlet.*;

/**
 * WebSocket servlet, which creates an instance of a WebSocket.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class WsServlet extends WebSocketServlet {
  @Override
  public void configure(final WebSocketServletFactory factory) {
    factory.setCreator(new WsCreator());
  }
}
