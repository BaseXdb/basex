package org.basex.http.ws;

import org.basex.http.ws.adapter.*;
import org.eclipse.jetty.websocket.servlet.*;

/**
 * Custom WebsockCreator.
 * @author BaseX Team 2005-18, BSD License
 */
public class WsCreator implements WebSocketCreator {

  @Override
  public Object createWebSocket(final ServletUpgradeRequest req,
                                final ServletUpgradeResponse resp) {

    String path = req.getHttpServletRequest().getPathInfo();
    if(path == null) path = "/";

    // Check for subprotocols, take the first matching
    for (String subprotocol : req.getSubProtocols()) {
      if("v10.stomp".equals(subprotocol)) {
        resp.setAcceptedSubProtocol(subprotocol);
        return new StompWebSocket(subprotocol);
      }
    }

    // If no valid subprotocol
    return new StandardWebSocket(path);
  }

}
