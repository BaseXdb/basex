package org.basex.http.ws;

import org.basex.http.ws.adapter.*;
import org.eclipse.jetty.websocket.servlet.*;

/**
 * Custom WebSocket creator.
 *
 * @author Johannes Finckh
 * @author BaseX Team 2005-18, BSD License
 */
public class WsCreator implements WebSocketCreator {
  @Override
  public Object createWebSocket(final ServletUpgradeRequest req,
      final ServletUpgradeResponse resp) {
    return new StandardWs(req.getHttpServletRequest());
  }

}
