package org.basex.http.ws;

import org.eclipse.jetty.websocket.servlet.*;

/**
 * WebsocketServlet which creates an Instance of a Websocket.
 * @author BaseX Team 2005-18, BSD License
 */
public class WebsocketServlet extends WebSocketServlet {
  @Override
  public void configure(final WebSocketServletFactory factory) {
    factory.getPolicy().setIdleTimeout(300000);
    factory.setCreator(new WsCreator());
  }
}
