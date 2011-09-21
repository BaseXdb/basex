package org.basex.api;

import static org.basex.api.HTTPText.*;
import static org.basex.core.Text.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.BaseXServer;
import org.basex.api.rest.RESTServlet;
import org.basex.api.webdav.WebDAVServlet;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.mortbay.jetty.Server;

/**
 * This is the main class for the starting the database HTTP services.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BaseXHTTP {
  /** Activate WebDAV. */
  private boolean webdav = true;
  /** Activate REST. */
  private boolean rest = true;
  /** Start as daemon. */
  protected boolean service;

  /** Database server. */
  private BaseXServer server;
  /** HTTP server. */
  private Server jetty;
  /** Quiet flag. */
  private boolean quiet;
  /** Stopped flag. */
  private boolean stopped;

  /**
   * Main method, launching the HTTP services.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      new BaseXHTTP(args);
    } catch(final Exception ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public BaseXHTTP(final String... args) throws Exception {
    parseArguments(args);

    final Context context = HTTPSession.context();
    final MainProp mprop = context.mprop;

    final int port = mprop.num(MainProp.SERVERPORT);
    final int eport = context.mprop.num(MainProp.EVENTPORT);
    final int http = mprop.num(MainProp.HTTPPORT);

    if(service) {
      start(http, args);
      Util.outln(HTTP + ' ' + SERVERSTART);
      Performance.sleep(1000);
      return;
    }

    if(stopped) {
      stop(port, eport);
      return;
    }

    // set password if only user was specified
    if(System.getProperty(DBUSER) != null) {
      while(System.getProperty(DBPASS) == null) {
        Util.out(SERVERPW + COLS);
        System.setProperty(DBPASS, Util.password());
      }
    }

    if(HTTPSession.client()) {
      server = new BaseXServer(context, quiet ? "-z" : "");
      Util.outln(HTTP + ' ' + SERVERSTART);
    } else {
      Util.outln(CONSOLE + HTTP + ' ' + SERVERSTART, SERVERMODE);
    }

    jetty = new Server(mprop.num(MainProp.HTTPPORT));
    final org.mortbay.jetty.servlet.Context jcontext =
        new org.mortbay.jetty.servlet.Context(jetty, "/");

    if(rest) jcontext.addServlet(RESTServlet.class, "/rest/*");
    if(webdav) jcontext.addServlet(WebDAVServlet.class, "/webdav/*");

    jetty.start();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    if(jetty != null) jetty.stop();
    if(server != null) server.quit();
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  protected void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, HTTPINFO, Util.info(CONSOLE, HTTP));
    final Context ctx = HTTPSession.context();
    boolean daemon = false;
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        switch(c) {
          case 'c':
            System.setProperty(DBCLIENT, Boolean.toString(true));
            break;
          case 'd':
            ctx.mprop.set(MainProp.DEBUG, true);
            break;
          case 'D':
            daemon = true;
            break;
          case 'e':
            ctx.mprop.set(MainProp.EVENTPORT, arg.num());
            break;
          case 'h':
            ctx.mprop.set(MainProp.HTTPPORT, arg.num());
            break;
          case 'n':
            ctx.mprop.set(MainProp.HOST, arg.num());
            break;
          case 'p':
            final int p = arg.num();
            ctx.mprop.set(MainProp.PORT, p);
            ctx.mprop.set(MainProp.SERVERPORT, p);
            break;
          case 'R':
            rest = false;
            break;
          case 'P':
            System.setProperty(DBPASS, arg.string());
            break;
          case 's':
            // set service flag
            service = !daemon;
            break;
          case 'U':
            System.setProperty(DBUSER, arg.string());
            break;
          case 'W':
            webdav = false;
            break;
          case 'z':
            quiet = true;
            break;
          default:
            arg.usage();
        }
      } else {
        if(arg.string().equalsIgnoreCase("stop")) {
          stopped = true;
        } else {
          arg.usage();
        }
      }
    }
  }

  // STATIC METHODS ===========================================================

  /**
   * Starts the specified class in a separate process.
   * @param port server port
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  public static void start(final int port, final String... args)
      throws BaseXException {

    // check if server is already running (needs some time)
    if(ping(LOCALHOST, port)) throw new BaseXException(SERVERBIND);

    Util.start(BaseXHTTP.class, args);

    // try to connect to the new server instance
    for(int c = 0; c < 10; ++c) {
      if(ping(LOCALHOST, port)) return;
      Performance.sleep(100);
    }
    throw new BaseXException(SERVERERROR);
  }

  /**
   * Stops the server.
   * @param port server port
   * @param eport event port
   * @throws IOException I/O exception
   */
  public static void stop(final int port, final int eport) throws IOException {
    BaseXServer.stop(port, eport);
  }

  /**
   * Checks if a server is running.
   * @param host host
   * @param port server port
   * @return boolean success
   */
  private static boolean ping(final String host, final int port) {
    try {
      // create connection
      final URL url = new URL("http://" + host + ":" + port);
      final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.getInputStream();
      return true;
    } catch(final IOException ex) {
      // if login was checked, server is running
      return ex instanceof FileNotFoundException;
    }
  }
}
