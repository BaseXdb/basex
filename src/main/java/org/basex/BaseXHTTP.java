package org.basex;

import static org.basex.core.Text.*;
import static org.basex.http.HTTPText.*;

import java.io.*;
import java.net.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.server.*;
import org.basex.util.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.webapp.*;
import org.eclipse.jetty.xml.*;

/**
 * This is the main class for the starting the database HTTP services.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Dirk Kirsten
 */
public final class BaseXHTTP {
  /** Database context. */
  final Context context = HTTPContext.init();
  /** HTTP server. */
  private final Server jetty;

  /** Stop port. */
  private int stopPort;
  /** HTTP port. */
  private int httpPort;
  /** Start as daemon. */
  private boolean service;
  /** Stopped flag. */
  private boolean stopped;

  /**
   * Main method, launching the HTTP services.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseXHTTP(args);
    } catch(final Exception ex) {
      Util.debug(ex);
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

    // create jetty instance and set default context to HTTP path
    final String webapp = context.mprop.get(MainProp.WEBPATH);
    jetty = (Server) new XmlConfiguration(initJetty(webapp).inputStream()).configure();
    jetty.setHandler(new WebAppContext(webapp, "/"));

    // retrieve jetty port
    for(final Connector c : jetty.getConnectors()) {
      if(c instanceof SelectChannelConnector) {
        if(httpPort == 0) httpPort = c.getPort();
        else c.setPort(httpPort);
      }
    }
    // stop port: one below jetty port
    if(stopPort == 0) stopPort = httpPort - 1;

    // check if ports are distinct
    final MainProp mprop = context.mprop;
    final int port = mprop.num(MainProp.SERVERPORT);
    final int eport = mprop.num(MainProp.EVENTPORT);
    int same = -1;
    if(port == eport || port == httpPort || port == stopPort) same = port;
    else if(eport == httpPort || eport == stopPort) same = eport;
    if(same != -1) throw new BaseXException(PORT_TWICE_X, same);

    final HTTPProp hprop = HTTPContext.hprop(context);
    final boolean server = hprop.is(HTTPProp.SERVER);
    if(service) {
      start(httpPort, args);
      Util.outln(HTTP + ' ' + SRV_STARTED);
      if(server) Util.outln(SRV_STARTED);
      // temporary console windows: keep the message visible for a while
      Performance.sleep(1000);
      return;
    }

    if(stopped) {
      stop();
      Util.outln(HTTP + ' ' + SRV_STOPPED);
      if(server) Util.outln(SRV_STOPPED);
      // temporary console windows: keep the message visible for a while
      Performance.sleep(1000);
      return;
    }

    // request password on command line if only the user was specified
    if(!hprop.get(HTTPProp.USER).isEmpty()) {
      while(hprop.get(HTTPProp.PASSWORD).isEmpty()) {
        Util.out(PASSWORD + COLS);
        hprop.set(HTTPProp.PASSWORD, Util.password());
      }
    }

    if(server) {
      new BaseXServer(context);
      Util.outln(HTTP + ' ' + SRV_STARTED);
    } else {
      Util.outln(CONSOLE + HTTP + ' ' + SRV_STARTED, SERVERMODE);
    }
    context.log.writeServer(OK, HTTP + ' ' + SRV_STARTED);

    jetty.start();
    new StopServer(mprop.get(MainProp.SERVERHOST)).start();

    // show info when HTTP server is aborted
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Util.outln(HTTP + ' ' + SRV_STOPPED);
        final Log l = context.log;
        if(l != null) l.writeServer(OK, HTTP + ' ' + SRV_STOPPED);
        context.close();
      }
    });
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    // notify the jetty monitor to stop
    stop(stopPort);
    // server has been started as separate process and need to be stopped
    if(HTTPContext.hprop(context).is(HTTPProp.SERVER)) {
      final int port = context.mprop.num(MainProp.SERVERPORT);
      final int eport = context.mprop.num(MainProp.EVENTPORT);
      BaseXServer.stop(port, eport);
    }
  }

  /**
   * Returns an input stream to the Jetty configuration file.
   * @param root web root path
   * @return input stream
   * @throws IOException I/O exception
   */
  private static IOFile initJetty(final String root) throws IOException {
    // try to locate file in HTTP path and development branch
    final IOFile trg = new IOFile(root + '/' + JETTYCONF);
    if(!trg.exists()) {
      // try to copy WEB-INF files from development branch
      final IOFile src = new IOFile("src/main/webapp/" + JETTYCONF);
      if(!src.exists()) throw new BaseXException(trg + " not found.");
      final IOFile trgDir = trg.dir();
      for(final IOFile s : src.dir().children()) {
        final IOFile t = new IOFile(trgDir, s.name());
        if(!s.isDir() && !t.exists()) {
          Util.errln("Copy " + s + " to " + trgDir);
          s.copyTo(t);
        }
      }
    }
    return trg;
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  private void parseArguments(final String[] args) throws IOException {
    final HTTPProp hprop = HTTPContext.hprop(context);
    final Args arg = new Args(args, this, HTTPINFO, Util.info(CONSOLE, HTTP));
    boolean daemon = false;
    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'd': // activate debug mode
            context.mprop.set(MainProp.DEBUG, true);
            break;
          case 'D': // hidden flag: daemon mode
            daemon = true;
            break;
          case 'e': // parse event port
            context.mprop.set(MainProp.EVENTPORT, arg.number());
            break;
          case 'h': // parse HTTP port
            httpPort = arg.number();
            break;
          case 'l': // use local mode
            hprop.set(HTTPProp.SERVER, false);
            break;
          case 'n': // parse host name
            context.mprop.set(MainProp.HOST, arg.string());
            break;
          case 'p': // parse server port
            final int p = arg.number();
            context.mprop.set(MainProp.PORT, p);
            context.mprop.set(MainProp.SERVERPORT, p);
            break;
          case 'P': // specify password
            hprop.set(HTTPProp.PASSWORD, arg.string());
            break;
          case 's': // parse stop port
            stopPort = arg.number();
            break;
          case 'S': // set service flag
            service = !daemon;
            break;
          case 'U': // specify user name
            hprop.set(HTTPProp.USER, arg.string());
            break;
          case 'v': // verbose output
            System.setProperty(HTTPINFO, Boolean.TRUE.toString());
            break;
          case 'z': // suppress logging
            context.mprop.set(MainProp.LOG, false);
            break;
          default:
            arg.usage();
        }
      } else {
        if(!arg.string().equalsIgnoreCase("stop")) arg.usage();
        stopped = true;
      }
    }
  }

  // STATIC METHODS ===========================================================

  /**
   * Starts the HTTP server in a separate process.
   * @param port server port
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  private static void start(final int port, final String... args) throws BaseXException {
    // check if server is already running (takes some time)
    if(ping(LOCALHOST, port)) throw new BaseXException(SRV_RUNNING);

    Util.start(BaseXHTTP.class, args);

    // try to connect to the new server instance
    for(int c = 0; c < 10; ++c) {
      if(ping(LOCALHOST, port)) return;
      Performance.sleep(100);
    }
    throw new BaseXException(CONNECTION_ERROR);
  }

  /**
   * Generates a stop file for the specified port.
   * @param port server port
   * @return stop file
   */
  private static File stopFile(final int port) {
    return new File(Prop.TMP, Util.name(BaseXHTTP.class) + port);
  }

  /**
   * Stops the server.
   * @param port server port
   * @throws IOException I/O exception
   */
  private static void stop(final int port) throws IOException {
    final File stop = stopFile(port);
    try {
      stop.createNewFile();
      new Socket(LOCALHOST, port).close();
      // give the notified process some time to quit
      Performance.sleep(100);
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
      final URL url = new URL("http://" + host + ':' + port);
      url.openConnection().getInputStream();
      return true;
    } catch(final IOException ex) {
      // if page is not found, server is running
      return ex instanceof FileNotFoundException;
    }
  }

  /** Monitor for stopping the Jetty server. */
  @SuppressWarnings("synthetic-access")
  private final class StopServer extends Thread {
    /** Server socket. */
    private final ServerSocket ss;
    /** Stop file. */
    private final File stop;

    /**
     * Constructor.
     * @param host host address
     * @throws IOException I/O exception
     */
    StopServer(final String host) throws IOException {
      final InetAddress addr = host.isEmpty() ? null : InetAddress.getByName(host);
      ss = new ServerSocket();
      ss.bind(new InetSocketAddress(addr, stopPort));
      stop = stopFile(stopPort);
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        while(true) {
          ss.accept().close();
          if(stop.exists()) {
            ss.close();
            stop.delete();
            jetty.stop();
            break;
          }
        }
      } catch(final Exception ex) {
        Util.errln(ex);
      }
    }
  }
}
