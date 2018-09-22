package org.basex.http.ws;

import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.eclipse.jetty.websocket.servlet.*;

/**
 * Custom WebSocket creator.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class WsCreator implements WebSocketCreator {
  @Override
  public Object createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse res) {
    Context context = HTTPContext.context();
    final StaticOptions sopts = context.soptions;

    String subprotString = sopts.get(StaticOptions.WSSUBPROTOCOLS);
    String[] subprotocols = subprotString.split(",");

    for (String subprotocol : req.getSubProtocols()) {
      if(subprotocol.equals("v12.stomp")){
        res.setAcceptedSubProtocol(subprotocol);
        return StompV12WebSocket.get(req.getHttpServletRequest(), subprotocol);
      }
      if(Arrays.asList(subprotocols).contains(subprotocol)) {
        res.setAcceptedSubProtocol(subprotocol);
        return WebSocket.get(req.getHttpServletRequest(),subprotocol);
      }
    }
    return WebSocket.get(req.getHttpServletRequest(), "");
  }
}
