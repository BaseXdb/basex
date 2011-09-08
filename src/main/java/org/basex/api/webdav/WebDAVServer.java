package org.basex.api.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.api.HTTPContext;
import org.basex.core.MainProp;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;

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
   * @param http HTTP context
   * @throws Exception exception
   */
  public WebDAVServer(final HTTPContext http) throws Exception {
    final HttpManager m = new HttpManager(new BXResourceFactory(http));

    final Handler h = new AbstractHandler() {
      @Override
      public void handle(final String target, final HttpServletRequest request,
          final HttpServletResponse response, final int dispatch)
          throws IOException, ServletException {

        final Request req = new BXServletRequest(request);
        final Response res = new BXServletResponse(response);

        try {
          m.process(req, res);
        } finally {
          res.getOutputStream().flush();
          response.flushBuffer();
        }
      }
    };

    jetty = new Server(http.context.mprop.num(MainProp.WEBDAVPORT));
    jetty.setHandler(h);
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
