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
    final WebAppContext wac = new WebAppContext(webapp, "/");
    jetty = (Server) new XmlConfiguration(initJetty(webapp).inputStream()).configure();
    jetty.setHandler(wac);

    // retrieve jetty port
    for(final Connector c : jetty.getConnectors()) {
      if(c instanceof SelectChannelConnector) {
        if(httpPort == 0) httpPort = c.getPort();
        else c.setPort(httpPort);
      }
    }
    // stop port: one below jetty port
    if(stopPort == 0) stopPort = httpPort - 1;

    // stop server
    if(stopped) {
      stop();
      Util.outln(HTTP + ' ' + SRV_STOPPED);
      // temporary console windows: keep the message visible for a while
      Performance.sleep(1000);
      return;
    }

    // start web server in a new process
    if(service) {
      start(httpPort, args);
      Util.outln(HTTP + ' ' + SRV_STARTED);
      // temporary console windows: keep the message visible for a while
      Performance.sleep(1000);
      return;
    }

    // request password on command line if only the user was specified
    if(!Prop.getSystem(HTTPProp.USER).isEmpty()) {
      while(Prop.getSystem(HTTPProp.PASSWORD).isEmpty()) {
        Util.out(PASSWORD + COLS);
        Prop.setSystem(HTTPProp.PASSWORD, Util.password());
      }
    }

    // try to start web server
    jetty.start();
    Util.outln(HTTP + ' ' + SRV_STARTED, SERVERMODE);

    // initialize http context, start daemon for stopping web server
    HTTPContext.init(wac.getServletContext());
    new StopServer(context.mprop.get(MainProp.SERVERHOST)).start();

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

    // log server start at very end (logging flag could have been updated in web.xml)
    context.log.writeServer(OK, HTTP + ' ' + SRV_STARTED);
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
    final Args arg = new Args(args, this, HTTPINFO, Util.info(CONSOLE, HTTP));
    boolean daemon = false;
    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'd': // activate debug mode
            Prop.setSystem(MainProp.DEBUG, true);
            break;
          case 'D': // hidden flag: daemon mode
            daemon = true;
            break;
          case 'e': // parse event port
            Prop.setSystem(MainProp.EVENTPORT, arg.number());
            break;
          case 'h': // parse HTTP port
            httpPort = arg.number();
            break;
          case 'l': // use local mode
            Prop.setSystem(HTTPProp.SERVER, false);
            break;
          case 'n': // parse host name
            Prop.setSystem(MainProp.HOST, arg.string());
            break;
          case 'p': // parse server port
            final int p = arg.number();
            Prop.setSystem(MainProp.PORT, p);
            Prop.setSystem(MainProp.SERVERPORT, p);
            break;
          case 'P': // specify password
            Prop.setSystem(HTTPProp.PASSWORD, arg.string());
            break;
          case 's': // parse stop port
            stopPort = arg.number();
            break;
          case 'S': // set service flag
            service = !daemon;
            break;
          case 'U': // specify user name
            Prop.setSystem(HTTPProp.USER, arg.string());
            break;
          case 'v': // verbose output
            Prop.setSystem(HTTPProp.VERBOSE, true);
            break;
          case 'z': // suppress logging
            Prop.setSystem(MainProp.LOG, false);
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
