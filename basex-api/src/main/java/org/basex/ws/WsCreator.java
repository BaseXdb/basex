package org.basex.ws;

import org.eclipse.jetty.websocket.servlet.*;

/**
 * Custom WebsockCreator.
 * @author BaseX Team 2005-18, BSD License
 */
public class WsCreator implements WebSocketCreator {

  @Override
  public Object createWebSocket(final ServletUpgradeRequest req,
                                final ServletUpgradeResponse resp) {
    // Check for subprotocols, take the first matching
    for (String subprotocol : req.getSubProtocols()) {
      if("v10.stomp".equals(subprotocol)) {
        resp.setAcceptedSubProtocol(subprotocol);
        WebSocke websocke = new WebSocke(subprotocol);
        return websocke;
      }
    }

    // If no valid subprotocol
    return new WebSocke(null);
  }

}
