package org.basex.http.ws;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.util.*;
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
    String acceptedOrigin = sopts.get(StaticOptions.WSORIGIN);
    if(acceptedOrigin.length() > 0 && !acceptedOrigin.equals(req.getOrigin())) {
      try {
        res.sendForbidden(HTTPCode.FORBIDDEN_ORIGIN.toString());
      } catch(IOException e) {
        Util.debug(e);
      }
    }
    return WebSocket.get(req.getHttpServletRequest());
  }
}
