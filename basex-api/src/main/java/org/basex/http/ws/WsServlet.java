package org.basex.http.ws;

import java.io.*;
import java.time.*;
import java.util.*;

import org.basex.http.*;
import org.eclipse.jetty.ee9.websocket.server.*;

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
    final Map<String, ?> map = HTTPContext.get().initParams();
    final Object it = map.get("maxIdleTime");
    factory.setIdleTimeout(Duration.ofSeconds(it != null ? Long.parseLong(it.toString()) : 3600));
    final Object mtms = map.get("maxTextMessageSize");
    if(mtms != null) factory.setMaxTextMessageSize(Long.parseLong(mtms.toString()));
    final Object mbms = map.get("maxBinaryMessageSize");
    if(mbms != null) factory.setMaxBinaryMessageSize(Long.parseLong(mbms.toString()));

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
