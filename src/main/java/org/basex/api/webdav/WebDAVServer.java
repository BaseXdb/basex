package org.basex.api.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.BaseXServer;
import org.basex.util.Util;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;

/**
 * This is the starter class for running the WebDAV server. A database server
 * and the Jetty server is launched by the constructor. The Jetty server listens
 * for HTTP requests, which are then processed by the WebDAV implementation.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura, Dimitar Popov
 */
public class WebDAVServer extends BaseXServer {
  /** HTTP server. */
  private final Server jetty;

  /**
   * Main method, launching the WebDAV implementation.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new WebDAVServer(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public WebDAVServer(final String... args) {
    super(args);

    final HttpManager m = new HttpManager(new BXResourceFactory(context));
    final Handler h = new AbstractHandler() {
      @SuppressWarnings("unused")
      @Override
      public void handle(final String target, final HttpServletRequest request,
          final HttpServletResponse response, final int dispatch)
          throws IOException, ServletException {

        final ServletRequest req = new ServletRequest(request);
        final ServletResponse res = new ServletResponse(response);

        try {
          m.process(req, res);
        } finally {
          res.getOutputStream().flush();
          response.flushBuffer();
        }
      }
    };

    jetty = new Server(8080);
    jetty.setHandler(h);
    try {
      jetty.start();
    } catch(Exception ex) {
      Util.server(ex);
    }
  }

  @Override
  public void quit(final boolean user) {
    super.quit(user);
    if(jetty != null) {
      try {
        jetty.stop();
      } catch(Exception ex) {
        Util.server(ex);
      }
    }
  }
}
