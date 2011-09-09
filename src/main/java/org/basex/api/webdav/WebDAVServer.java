package org.basex.api.webdav;

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
  /**
   * Constructor.
   * @param jetty jetty server
   */
  public WebDAVServer(final Server jetty) {
    new Context(jetty, "/").addServlet(WebDAVServlet.class, "/webdav/*");
  }
}
