package org.basex.http.ws;

import java.io.*;

import javax.servlet.*;

import org.basex.http.*;
import org.eclipse.jetty.websocket.servlet.*;

/**
 * WebSocket servlet.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public final class WsServlet extends WebSocketServlet {
  @Override
  public void configure(final WebSocketServletFactory factory) {
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
