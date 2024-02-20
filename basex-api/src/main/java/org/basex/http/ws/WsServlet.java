package org.basex.http.ws;

import java.io.*;

import org.basex.http.*;
import org.eclipse.jetty.websocket.server.*;

import jakarta.servlet.*;

/**
 * WebSocket servlet.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Johannes Finckh
 */
public final class WsServlet extends JettyWebSocketServlet {
  @Override
  public void configure(final JettyWebSocketServletFactory factory) {
    factory.setCreator(new WsCreator());
  }

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      HTTPContext.get().init(config.getServletContext());
    } catch(final IOException ex) {
      throw new ServletException(ex);
    }
  }
}
