package org.basex.http.ws;

import org.eclipse.jetty.websocket.server.*;

/**
 * Custom WebSocket creator.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Johannes Finckh
 */
public final class WsCreator implements JettyWebSocketCreator {
  @Override
  public Object createWebSocket(final JettyServerUpgradeRequest request,
      final JettyServerUpgradeResponse response) {
    return WebSocket.get(request.getHttpServletRequest());
  }
}
