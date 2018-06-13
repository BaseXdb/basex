package org.basex.http.ws;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.server.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * Clientinfo in Websocket context.
 * @author BaseX Team 2005-18, BSD License
 *
 */
public class WsConnection implements ClientInfo {

  /**
   * The UpdateRequest.
   */
  public final UpgradeRequest req;
  /**
   * The UpdateResponse.
   * */
  public final UpgradeResponse res;
  /**
   * The DatabaseContext.
   * */
  public final Context context;
  /**
   * The path of the Connection.
   * */
  private final String path;
  /**
   * The corresponding session for the connection.
   * */
  public final Session sess;

  /**
   * Constructor for the WebsocketConnection.
   * @param req the UpdateRequest
   * @param res the UpdateResponse
   * @param sess the Session
   * @param path the Path
   */
  public WsConnection(final UpgradeRequest req,
                             final UpgradeResponse res,
                             final Session sess,
                             final String path) {
    this.req = req;
    this.res = res;
    context = new Context(HTTPContext.context(), this);
    context.user(HTTPContext.context().user());
    this.path = path;
    this.sess = sess;
  }

  @Override
  public String clientAddress() {
    return sess.getRemoteAddress().toString();
  }

  @Override
  public String clientName() {
    return context.user().name();
  }

  /**
   * Returns the Path of the Connection.
   * @return path String
   */
  public String path() {
    return this.path;
  }

  /**
   * Send an Error.
   * @param errorMsg String
   * @param errorcode int
   * */
  public void error(final String errorMsg, final int errorcode) {
      try {
        if(this.sess.isOpen())
          this.sess.getRemote().sendString(errorcode + ":" + errorMsg);
      } catch(IOException e) {
        e.printStackTrace();
      }
  }
}
