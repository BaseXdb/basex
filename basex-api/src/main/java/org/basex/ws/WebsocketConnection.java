package org.basex.ws;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.server.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * Clientinfo in Websocket context.
 * Should be like HTTPConnection.java, just for websockets
 *
 */
public class WebsocketConnection implements ClientInfo {

  public final UpgradeRequest req;
  public final UpgradeResponse res;
  public final Context context;
  private final String path;

  WebsocketConnection(final UpgradeRequest req, final UpgradeResponse res) {
    this.req = req;
    this.res = res;
    context = new Context(HTTPContext.context(), this);
    // todo: normalize path
    this.path = req.getRequestURI().toString().substring(19);
  }

  @Override
  public String clientAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String clientName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String path() {
    return this.path;
  }

}
