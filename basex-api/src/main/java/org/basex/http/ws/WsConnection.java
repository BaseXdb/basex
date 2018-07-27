package org.basex.http.ws;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.server.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * Client info in WebSocket context.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class WsConnection implements ClientInfo {
  /** Database context. */
  public final Context context;
  /** Session for the connection. */
  public final Session session;
  /** Connection path. */
  private final String path;

  /**
   * Constructor for the WebSocket connection.
   * @param session session
   * @param path path
   */
  public WsConnection(final Session session, final String path) {
    this.session = session;
    this.path = path;
    final Context ctx = HTTPContext.context();
    context = new Context(ctx, this);
    context.user(ctx.user());
  }

  @Override
  public String clientAddress() {
    return session.getRemoteAddress().toString();
  }

  @Override
  public String clientName() {
    return context.user().name();
  }

  /**
   * Returns the path of the connection.
   * @return path String
   */
  public String path() {
    return path;
  }

  /**
   * Sends an error to the client.
   * @param message error message
   * @param code error code
   * @throws IOException I/O exception
   */
  public void error(final String message, final int code) throws IOException {
    if(session.isOpen()) session.getRemote().sendString(code + ":" + message);
  }
}
