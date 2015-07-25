package org.basex;

import static org.basex.core.Text.*;
import static org.basex.http.HTTPText.*;

import java.io.*;
import java.net.*;

import javax.net.ssl.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.server.*;
import org.basex.server.Log.LogType;
import org.basex.util.*;
import org.basex.util.options.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.server.ssl.*;
import org.eclipse.jetty.webapp.*;
import org.eclipse.jetty.xml.*;

/**
 * This is the main class for the starting the database HTTP services.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @author Dirk Kirsten
 */
public final class BaseXHTTP extends Main {
  /** Database context. */
  private final Context context;
  /** HTTP server. */
  private final Server jetty;
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
    super(args);
    parseArgs();

    // context must be initialized after parsing of arguments
    context = HTTPContext.init();

    // create jetty instance and set default context to HTTP path
    final StaticOptions sopts = context.soptions;
    final String webapp = sopts.get(StaticOptions.WEBPATH);
    final WebAppContext wac = new WebAppContext(webapp, "/");
    jetty = (Server) new XmlConfiguration(initJetty(webapp).inputStream()).configure();
    jetty.setHandler(wac);

    // set the first http port (can also be https) to the port provided on command line
    if(httpPort != 0) {
      for(final Connector c : jetty.getConnectors()) {
        if(c instanceof SelectChannelConnector) {
          c.setPort(httpPort);
          break;
        }
      }
    }

    // info strings
    final String startX = HTTP + ' ' + SRV_STARTED_PORT_X;
    final String stopX = HTTP + ' ' + SRV_STOPPED_PORT_X;

    if(stopped) {
      stop();
      for(final Connector c : jetty.getConnectors())
        Util.outln(stopX, c.getPort());
      // temporary console windows: keep the message visible for a while
      Performance.sleep(1000);
      return;
    }

    // start web server in a new process
    final Connector connector = jetty.getConnectors()[0];
    if(service) {
      start(connector.getPort(), connector instanceof SslSelectChannelConnector, args);

      for(final Connector c : jetty.getConnectors()) {
        Util.outln(startX, c.getPort());
      }
      // temporary console windows: keep the message visible for a while
      Performance.sleep(1000);
      return;
    }

    // request password on command line if only the user was specified
    if(!Prop.get(StaticOptions.USER).isEmpty()) {
      while(Prop.get(StaticOptions.PASSWORD).isEmpty()) {
        Util.out(PASSWORD + COLS);
        Prop.put(StaticOptions.PASSWORD, Util.password());
      }
    }

    // start web server
    try {
      jetty.start();
    } catch(final BindException ex) {
      throw new IOException(Util.info(HTTP + ' ' + SRV_RUNNING_X, connector.getPort()), ex);
    }
    // throw cached exception that did not break the servlet architecture
    final IOException ex = HTTPContext.exception();
    if(ex != null) throw HTTPContext.exception();

    // show start message
    for(final Connector c : jetty.getConnectors()) Util.outln(startX, c.getPort());

    // initialize web.xml settings, assign system properties and run database server.
    // the call of this function may already have been triggered during the start of jetty
    HTTPContext.init(wac.getServletContext());

    // start daemon for stopping web server
    final int stop = sopts.get(StaticOptions.STOPPORT);
    if(stop >= 0) new StopServer(sopts.get(StaticOptions.SERVERHOST), stop).start();

    // show info when HTTP server is aborted. needs to be called in constructor:
    // otherwise, it may only be called if the JVM process is already shut down
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        for(final Connector c : jetty.getConnectors()) {
          Util.outln(stopX, c.getPort());
        }
        final Log l = context.log;
        if(l != null) {
          for(final Connector c : jetty.getConnectors()) {
            l.writeServer(LogType.OK, Util.info(stopX, c.getPort()));
          }
        }
        context.close();
      }
    });

    // log server start at very end (logging flag could have been updated by web.xml)
    for(final Connector c : jetty.getConnectors()) {
      context.log.writeServer(LogType.OK, Util.info(startX, c.getPort()));
    }
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    // notify the jetty monitor to stop
    final StaticOptions mprop = context.soptions;
    final int stop = num(StaticOptions.STOPPORT, mprop);
    if(stop >= 0) stop(stop);

    // server has been started in a separate process and needs to be stopped
    if(!bool(StaticOptions.HTTPLOCAL, mprop)) {
      BaseXServer.stop(num(StaticOptions.SERVERPORT, mprop));
    }
  }

  /**
   * Returns a numeric value for the specified option.
   * @param option option to be retrieved
   * @param sopts static options
   * @return numeric value
   */
  private static int num(final NumberOption option, final StaticOptions sopts) {
    final String val = Prop.get(option);
    return val.isEmpty() ? sopts.get(option) : Strings.toInt(val);
  }

  /**
   * Returns a boolean value for the specified option.
   * @param option option to be retrieved
   * @param sopts static options
   * @return boolean value
   */
  private static boolean bool(final BooleanOption option, final StaticOptions sopts) {
    final String val = Prop.get(option);
    return val.isEmpty() ? sopts.get(option) : Boolean.parseBoolean(val);
  }

  /**
   * Returns a reference to the Jetty configuration file.
   * @param root target root directory
   * @return input stream
   * @throws IOException I/O exception
   */
  private static IOFile initJetty(final String root) throws IOException {
    locate(WEBCONF, root);
    return locate(JETTYCONF, root);
  }

  /**
   * Locates the specified configuration file.
   * @param file file to be copied
   * @param root target root directory
   * @return reference to created file
   * @throws IOException I/O exception
   */
  private static IOFile locate(final String file, final String root) throws IOException {
    final IOFile trg = new IOFile(root + '/' + file);
    final boolean create = !trg.exists();

    // try to locate file from development branch
    final IO in = new IOFile("src/main/webapp/" + file);
    final byte[] data;
    if(in.exists()) {
      data = in.read();
      // check if resource path exists
      IOFile res = new IOFile("src/main/resources/");
      if(res.exists()) {
        res = new IOFile(res, file);
        // update file in resource path if it has changed
        if(!res.exists() || !Token.eq(data, res.read())) {
          Util.errln("Updating " +  res);
          res.parent().md();
          res.write(in.read());
        }
      }
    } else if(create) {
      // try to locate file from resource path
      final InputStream is = BaseXHTTP.class.getResourceAsStream('/' + file);
      if(is == null) throw new BaseXException(in + " not found.");
      data = new IOStream(is).read();
    } else {
      return trg;
    }

    if(create) {
      // create configuration file
      Util.errln("Creating " +  trg);
      trg.parent().md();
      trg.write(data);
    }
    return trg;
  }

  @Override
  protected void parseArgs() throws IOException {
    /* command-line properties will be stored in system properties;
     * this way, they will not be overwritten by the settings specified in web.xml. */
    final MainParser arg = new MainParser(this);
    boolean serve = true;
    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'd': // activate debug mode
            Prop.put(StaticOptions.DEBUG, true);
            Prop.debug = true;
            break;
          case 'D': // hidden flag: daemon mode
            serve = false;
            break;
          case 'h': // parse HTTP port
            httpPort = arg.number();
            break;
          case 'l': // use local mode
            Prop.put(StaticOptions.HTTPLOCAL, true);
            break;
          case 'n': // parse host name
            final String n = arg.string();
            Prop.put(StaticOptions.HOST, n);
            Prop.put(StaticOptions.SERVERHOST, n);
            break;
          case 'p': // parse server port
            final int p = arg.number();
            Prop.put(StaticOptions.PORT, p);
            Prop.put(StaticOptions.SERVERPORT, p);
            break;
          case 'P': // specify password
            Prop.put(StaticOptions.PASSWORD, arg.string());
            break;
          case 's': // parse stop port
            Prop.put(StaticOptions.STOPPORT, arg.number());
            break;
          case 'S': // set service flag
            service = serve;
            break;
          case 'U': // specify user name
            Prop.put(StaticOptions.USER, arg.string());
            break;
          case 'z': // suppress logging
            Prop.put(StaticOptions.LOG, false);
            break;
          default:
            throw arg.usage();
        }
      } else {
        if(!"stop".equalsIgnoreCase(arg.string())) throw arg.usage();
        stopped = true;
      }
    }
  }

  // STATIC METHODS ===========================================================

  /**
   * Starts the HTTP server in a separate process.
   * @param port server port
   * @param ssl encryption via HTTPS
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  private static void start(final int port, final boolean ssl, final String... args)
      throws BaseXException {

    Util.start(BaseXHTTP.class, args);
    // try to connect to the new server instance
    for(int c = 1; c < 10; ++c) {
      if(ping(S_LOCALHOST, port, ssl)) return;
      Performance.sleep(c * 100L);
    }
    throw new BaseXException(CONNECTION_ERROR_X, port);
  }

  /**
   * Generates a stop file for the specified port.
   * @param port server port
   * @return stop file
   */
  private static IOFile stopFile(final int port) {
    return new IOFile(Prop.TMP, Util.className(BaseXHTTP.class) + port);
  }

  /**
   * Stops the server.
   * @param port server port
   * @throws IOException I/O exception
   */
  private static void stop(final int port) throws IOException {
    final IOFile stop = stopFile(port);
    try {
      stop.touch();
      new Socket(S_LOCALHOST, port).close();
      // give the notified process some time to quit
      Performance.sleep(100);
    } catch(final ConnectException ex) {
      throw new IOException(Util.info(CONNECTION_ERROR_X, port));
    } finally {
      stop.delete();
    }
  }

  /**
   * Checks if a server is running.
   * @param host host
   * @param port server port
   * @param ssl encryption via HTTPS
   * @return boolean success
   */
  private static boolean ping(final String host, final int port, final boolean ssl) {
    try {
      // create connection
      new IOUrl((ssl ? "https://" : "http://") + host + ':' + port).connection().getInputStream();
      return true;
    } catch(final FileNotFoundException | SSLHandshakeException ex) {
      // if page is not found, server is running
      // if SSL handshake failed server is running, otherwise SSLException
      return true;
    } catch(final IOException ex) {
      return false;
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
     * @param host host address
     * @param port stop port
     * @throws IOException I/O exception
     */
    StopServer(final String host, final int port) throws IOException {
      final InetAddress addr = host.isEmpty() ? null : InetAddress.getByName(host);
      ss = new ServerSocket();
      ss.setReuseAddress(true);
      ss.bind(new InetSocketAddress(addr, port));
      stop = stopFile(port);
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

  @Override
  public String header() {
    return Util.info(S_CONSOLE, HTTP);
  }

  @Override
  public String usage() {
    return S_HTTPINFO;
  }
}
