package org.basex.api;

import static org.basex.api.HTTPText.*;
import static org.basex.core.Text.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.basex.BaseXServer;
import org.basex.api.rest.RESTServlet;
import org.basex.api.webdav.WebDAVServlet;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

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

  /** Database server. */
  private BaseXServer server;
  /** Start as daemon. */
  private boolean service;
  /** Stopped flag. */
  private boolean stopped;
  /** HTTP server. */
  protected Server jetty;
  /** Quiet flag. */
  private boolean quiet;

  /**
   * Main method, launching the HTTP services.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
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

    // flag for starting/stopping the database server
    final boolean start =
        !Token.eqic(System.getProperty(DBMODE), LOCAL, CLIENT);

    final Context ctx = HTTPSession.context();
    final MainProp mprop = ctx.mprop;
    final int port = mprop.num(MainProp.SERVERPORT);
    final int eport = mprop.num(MainProp.EVENTPORT);
    final int hport = mprop.num(MainProp.HTTPPORT);
    final int sport = mprop.num(MainProp.STOPPORT);
    // check if ports are distinct
    int same = -1;
    if(port == eport || port == hport || port == sport) same = port;
    else if(eport == hport || eport == sport) same = eport;
    else if(hport == sport) same = hport;
    if(same != -1) throw new BaseXException(SERVERPORTS, same);

    final String shost = mprop.get(MainProp.SERVERHOST);

    if(service) {
      start(hport, args);
      Util.outln(HTTP + ' ' + SERVERSTART);
      if(start) Util.outln(SERVERSTART);
      Performance.sleep(1000);
      return;
    }

    if(stopped) {
      stop(sport);
      Util.outln(HTTP + ' ' + SERVERSTOPPED);
      if(start) {
        BaseXServer.stop(port, eport);
        Util.outln(SERVERSTOPPED);
      }
      Performance.sleep(1000);
      return;
    }

    // request password on command line if only the user was specified
    if(System.getProperty(DBUSER) != null) {
      while(System.getProperty(DBPASS) == null) {
        Util.out(SERVERPW + COLS);
        System.setProperty(DBPASS, Util.password());
      }
    }

    if(start) {
      // default mode: start database server
      server = quiet ? new BaseXServer(ctx, "-z") : new BaseXServer(ctx);
      Util.outln(HTTP + ' ' + SERVERSTART);
    } else {
      // local or client mode
      Util.outln(CONSOLE + HTTP + ' ' + SERVERSTART, SERVERMODE);
    }

    jetty = new Server();
    final Connector conn = new SelectChannelConnector();
    if(!shost.isEmpty()) conn.setHost(shost);
    conn.setPort(hport);
    jetty.addConnector(conn);

    final org.mortbay.jetty.servlet.Context jctx =
        new org.mortbay.jetty.servlet.Context(jetty, "/",
            org.mortbay.jetty.servlet.Context.SESSIONS);

    if(rest)   jctx.addServlet(RESTServlet.class, "/rest/*");
    if(webdav) jctx.addServlet(WebDAVServlet.class, "/webdav/*");

    final ResourceHandler rh = new ResourceHandler();
    rh.setWelcomeFiles(new String[] {
        "index.xml", "index.xhtml", "index.html" });
    rh.setResourceBase(ctx.mprop.get(MainProp.HTTPPATH));

    final HandlerList hl = new HandlerList();
    hl.addHandler(rh);
    hl.addHandler(jctx);
    //hl.addHandler(new DefaultHandler());
    jetty.setHandler(hl);

    jetty.start();
    new StopServer(sport, shost).start();

    // show info when HTTP server is aborted
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Util.outln(HTTP + ' ' + SERVERSTOPPED);
      }
    });
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
    boolean daemon = false, local = false, client = false;
    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'c': // use client mode
            System.setProperty(DBMODE, CLIENT);
            client = true;
            break;
          case 'd': // activate debug mode
            ctx.mprop.set(MainProp.DEBUG, true);
            break;
          case 'D': // hidden flag: daemon mode
            daemon = true;
            break;
          case 'e': // parse event port
            ctx.mprop.set(MainProp.EVENTPORT, arg.number());
            break;
          case 'h': // parse HTTP port
            ctx.mprop.set(MainProp.HTTPPORT, arg.number());
            break;
          case 'l': // use local mode
            System.setProperty(DBMODE, LOCAL);
            local = true;
            break;
          case 'n': // parse host name
            ctx.mprop.set(MainProp.HOST, arg.string());
            break;
          case 'p': // parse server port
            ctx.mprop.set(MainProp.PORT, arg.number());
            ctx.mprop.set(MainProp.SERVERPORT, ctx.mprop.num(MainProp.PORT));
            break;
          case 'R': // deactivate REST service
            rest = false;
            break;
          case 'P': // specify password
            System.setProperty(DBPASS, arg.string());
            break;
          case 's': // parse stop port
            ctx.mprop.set(MainProp.STOPPORT, arg.number());
            break;
          case 'S': // set service flag
            service = !daemon;
            break;
          case 'U': // specify user name
            System.setProperty(DBUSER, arg.string());
            break;
          case 'W': // deactivate WebDAV service
            webdav = false;
            break;
          case 'z': // suppress logging
            quiet = true;
            break;
          default:
            arg.usage();
        }
      } else {
        if(!arg.string().equalsIgnoreCase("stop")) arg.usage();
        stopped = true;
      }
    }

    // only allow local or client mode
    if(local && client) {
      Util.errln(INVMODE);
      arg.usage();
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
   * Generates a stop file for the specified port.
   * @param port server port
   * @return stop file
   */
  static IOFile stopFile(final int port) {
    return new IOFile(Prop.TMP, Util.name(BaseXHTTP.class) + port);
  }

  /**
   * Stops the server.
   * @param port server port
   * @throws IOException I/O exception
   */
  public static void stop(final int port) throws IOException {
    final IOFile stop = stopFile(port);
    try {
      stop.write(Token.EMPTY);
      new Socket(LOCALHOST, port).close();
      Performance.sleep(50);
    } catch(final IOException ex) {
      stop.delete();
      throw ex;
    }
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
      // if page is not found, server is running
      return ex instanceof FileNotFoundException;
    }
  }

  /** Monitor for stopping the Jetty server. */
  private final class StopServer extends Thread {
    /** Server socket. */
    private final ServerSocket ss;
    /** Stop file. */
    private final IOFile stop;

    /**
     * Constructor.
     * @param hport HTTP port
     * @param host host address
     * @throws IOException I/O exception
     */
    StopServer(final int hport, final String host) throws IOException {
      final InetAddress addr = host.isEmpty() ? null :
        InetAddress.getByName(host);
      ss = new ServerSocket();
      ss.setReuseAddress(true);
      ss.bind(new InetSocketAddress(addr, hport));
      stop = stopFile(hport);
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        while(true) {
          ss.accept().close();
          if(!stop.exists()) continue;
          stop.delete();
          BaseXHTTP.this.stop();
          ss.close();
          break;
        }
      } catch(final Exception ex) {
        Util.errln(ex);
      }
    }
  }
}
