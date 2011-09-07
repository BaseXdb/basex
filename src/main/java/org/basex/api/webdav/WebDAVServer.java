package org.basex.api.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.core.Main;
import org.basex.core.MainProp;
import org.basex.core.Text;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Util;
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
public final class WebDAVServer extends Main {
  /** Configuration: database user. */
  static final String DBUSER = "org.basex.user";
  /** Configuration: database user password. */
  static final String DBPASS = "org.basex.password";
  /** Configuration: database server host. */
  static final String DBHOST = "org.basex.serverhost";
  /** Configuration: database server port. */
  static final String DBPORT = "org.basex.serverport";
  /** Configuration: WebDAV server port. */
  static final String WEBDAVPORT = "org.basex.webdavport";

  /** HTTP server. */
  private Server jetty;
  /** Stand-alone flag: no remote BaseX server. */
  private boolean standalone = true;

  /**
   * Main method, launching the WebDAV implementation.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    if(new WebDAVServer(args).failed()) System.exit(1);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public WebDAVServer(final String... args) {
    super(args);
    if(failed) return;

    set(DBHOST, context.mprop.get(MainProp.HOST));
    set(DBPORT, Integer.toString(context.mprop.num(MainProp.SERVERPORT)));

    final HttpManager m = new HttpManager(new BXResourceFactory(standalone));
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

    final String p = System.getProperty(WEBDAVPORT);
    final int port = p == null ? context.mprop.num(MainProp.WEBDAVPORT) :
      Integer.parseInt(p);

    jetty = new Server(port);
    jetty.setHandler(h);
    try {
      jetty.start();
    } catch(final Exception ex) {
      Util.errln(ex);
    }
  }

  /**
   * Store a configuration property as a system property.
   * @param k property key
   * @param v property value
   */
  private static void set(final String k, final String v) {
    if(System.getProperty(k) == null) System.setProperty(k, v);
  }

  /**
   * Stops the server.
   */
  public void stop() {
    try {
      jetty.stop();
    } catch(final Exception ex) {
      Util.errln(ex);
    }
  }

  @Override
  protected Session session() { return null; }

  @Override
  protected boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this, Text.WEBDAVINFO);
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        switch(c) {
          case 'n': set(DBHOST, arg.string()); break;
          case 'P': set(DBPASS, arg.string()); break;
          case 'r': set(DBPORT, arg.string()); break;
          case 's': standalone = true; break;
          case 'U': set(DBUSER, arg.string()); break;
          case 'w': set(WEBDAVPORT, arg.string()); break;
          default: arg.ok(false);
        }
      } else {
        arg.ok(false);
      }
    }
    return arg.finish();
  }
}
