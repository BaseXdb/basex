package org.basex.http.ws;

import org.eclipse.jetty.websocket.servlet.*;

/**
 * Custom WebSocket creator.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public final class WsCreator implements WebSocketCreator {
  @Override
  public Object createWebSocket(final ServletUpgradeRequest request,
      final ServletUpgradeResponse response) {
    return WebSocket.get(request.getHttpServletRequest());
  }
}
