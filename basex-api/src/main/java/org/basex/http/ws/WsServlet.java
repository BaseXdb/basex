package org.basex.http.ws;

import java.io.*;
import java.time.*;

import org.basex.http.*;
import org.eclipse.jetty.websocket.server.*;

import jakarta.servlet.*;

/**
 * WebSocket servlet.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WsServlet extends JettyWebSocketServlet {
  @Override
  public void configure(final JettyWebSocketServletFactory factory) {
    factory.setIdleTimeout(Duration.ofHours(1));
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
