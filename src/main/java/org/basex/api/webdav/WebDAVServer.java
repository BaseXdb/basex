package org.basex.api.webdav;

import org.basex.api.HTTPContext;
import org.basex.core.MainProp;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

/**
 * This is the starter class for running the WebDAV server. A database server
 * and the Jetty server is launched by the constructor. The Jetty server listens
 * for HTTP requests, which are then processed by the WebDAV implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class WebDAVServer {
  /** HTTP server. */
  private final Server jetty;

  /**
   * Constructor.
   * @throws Exception exception
   */
  public WebDAVServer() throws Exception {
    final HTTPContext http = HTTPContext.get();
    jetty = new Server(http.context.mprop.num(MainProp.WEBDAVPORT));
    new Context(jetty, "/").addServlet(WebDAVServlet.class, "/webdav/*");
    jetty.start();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    jetty.stop();
  }
}
