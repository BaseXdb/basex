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
    // Get the PathInfo of the WebSocket
    String path = req.getHttpServletRequest().getPathInfo();
    if(path == null) path = "/";
    return new StandardWs(path);
  }

}
