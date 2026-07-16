package org.basex.http.ws;

import org.eclipse.jetty.ee10.websocket.server.*;

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
    final WebSocket ws = WebSocket.get(request.getHttpServletRequest());
    if(ws != null && ws.subprotocol != null) response.setAcceptedSubProtocol(ws.subprotocol);
    return ws;
  }
}
