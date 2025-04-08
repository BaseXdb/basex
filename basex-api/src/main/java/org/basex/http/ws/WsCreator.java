package org.basex.http.ws;

import org.eclipse.jetty.ee9.websocket.server.*;

/**
 * Custom WebSocket creator.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WsCreator implements JettyWebSocketCreator {
  @Override
  public Object createWebSocket(final JettyServerUpgradeRequest request,
      final JettyServerUpgradeResponse response) {
    return WebSocket.get(request.getHttpServletRequest());
  }
}
