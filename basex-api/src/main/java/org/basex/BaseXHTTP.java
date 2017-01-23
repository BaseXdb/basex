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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 * @author Dirk Kirsten
 */
public final class BaseXHTTP extends Main {
  /** Database context. */
  private final Context context;
  /** HTTP server. */
  private final Server jetty;
  /** HTTP port. */
  private int port;
  /** Start as daemon. */
  private boolean service;
  /** Quiet flag. */
  private boolean quiet;
  /** Stop flag. */
  private boolean stop;

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
    context = HTTPContext.context();

    // create jetty instance and set default context to HTTP path
    final StaticOptions sopts = context.soptions;
    final String webapp = sopts.get(StaticOptions.WEBPATH);
    final WebAppContext wac = new WebAppContext(webapp, "/");
    jetty = (Server) new XmlConfiguration(initJetty(webapp).inputStream()).configure();
    jetty.setHandler(wac);

    final Connector[] conns = jetty.getConnectors();
    if(conns == null || conns.length == 0)
      throw new BaseXException("No Jetty connector defined in " + JETTYCONF + '.');

    if(port != 0) {
      for(final Connector conn : conns) {
        if(conn instanceof SelectChannelConnector) {
          conn.setPort(port);
          break;
        }
      }
    }

    // info strings
    final String startX = HTTP + ' ' + SRV_STARTED_PORT_X;
    final String stopX = HTTP + ' ' + SRV_STOPPED_PORT_X;

    if(stop) {
      stop();
      if(!quiet) for(final Connector conn : conns) Util.outln(stopX, conn.getPort());
      // keep message visible for a while
      Performance.sleep(1000);
      return;
    }

    // start web server in a new process
    final Connector conn1 = conns[0];
    if(service) {
      start(conn1.getPort(), conn1 instanceof SslSelectChannelConnector, args);
      if(!quiet) for(final Connector conn : conns) Util.outln(startX, conn.getPort());
      // keep message visible for a while
      Performance.sleep(1000);
      return;
    }

    // start web server
    if(!quiet) Util.outln(header());
    try {
      jetty.start();
    } catch(final BindException ex) {
      Util.debug(ex);
      throw new BaseXException(HTTP + ' ' + SRV_RUNNING_X, conn1.getPort());
    }
    // throw cached exception that did not break the servlet architecture
    final IOException ex = HTTPContext.exception();
    if(ex != null) throw ex;

    // show start message
    if(!quiet) {
      for(final Connector conn : conns) Util.outln(startX, conn.getPort());
    }

    // initialize web.xml settings, assign system properties and run database server.
    // the call of this function may already have been triggered during the start of jetty
    HTTPContext.init(wac.getServletContext());

    // start daemon for stopping web server
    final int sport = sopts.get(StaticOptions.STOPPORT);
    if(sport >= 0) new StopServer(sopts.get(StaticOptions.SERVERHOST), sport).start();

    // show info when HTTP server is aborted. needs to be called in constructor:
    // otherwise, it may only be called if the JVM process is already shut down
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if(!quiet) {
          for(final Connector conn : conns) Util.outln(stopX, conn.getPort());
        }
        final Log log = context.log;
        if(log != null) {
          for(final Connector conn : conns) {
            log.writeServer(LogType.OK, Util.info(stopX, conn.getPort()));
          }
        }
        context.close();
      }
    });

    // log server start at very end (logging flag could have been updated by web.xml)
    for(final Connector conn : conns) {
      context.log.writeServer(LogType.OK, Util.info(startX, conn.getPort()));
    }
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    // notify the jetty monitor to stop
    final StaticOptions sopts = context.soptions;
    final int sport = num(StaticOptions.STOPPORT, sopts);
    final String host = sopts.get(StaticOptions.SERVERHOST);
    if(sport >= 0) stop(host.isEmpty() ? S_LOCALHOST : host, sport);
  }

  /**
   * Returns a numeric value for the specified option.
   * @param option option to be retrieved
   * @param sopts static options
   * @return numeric value
   */
  private static int num(final NumberOption option, final StaticOptions sopts) {
    final String val = Prop.get(option);
    return val == null || val.isEmpty() ? sopts.get(option) : Strings.toInt(val);
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
    final IOFile trg = new IOFile(root, file);
    final boolean create = !trg.exists();

    // try to locate file from development branch
    final IO in = new IOFile("src/main/webapp", file);
    final byte[] data;
    if(in.exists()) {
      data = in.read();
      // check if resource path exists
      IOFile res = new IOFile("src/main/resources");
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
      try(InputStream is = BaseXHTTP.class.getResourceAsStream('/' + file)) {
        if(is == null) throw new BaseXException(in + " not found.");
        data = new IOStream(is).read();
      }
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
          case 'c': // use client mode
            Prop.put(StaticOptions.HTTPLOCAL, Boolean.toString(false));
            break;
          case 'd': // activate debug mode
            Prop.put(StaticOptions.DEBUG, Boolean.toString(true));
            Prop.debug = true;
            break;
          case 'D': // hidden flag: daemon mode
            serve = false;
            break;
          case 'h': // parse HTTP port
            port = arg.number();
            break;
          case 'l': // use local mode
            Prop.put(StaticOptions.HTTPLOCAL, Boolean.toString(true));
            break;
          case 'n': // parse host name
            final String n = arg.string();
            Prop.put(StaticOptions.HOST, n);
            Prop.put(StaticOptions.SERVERHOST, n);
            break;
          case 'p': // parse server port
            final int p = arg.number();
            Prop.put(StaticOptions.PORT, Integer.toString(p));
            Prop.put(StaticOptions.SERVERPORT, Integer.toString(p));
            break;
          case 'q': // quiet flag (hidden)
            quiet = true;
            break;
          case 's': // parse stop port
            Prop.put(StaticOptions.STOPPORT, Integer.toString(arg.number()));
            break;
          case 'S': // set service flag
            service = serve;
            break;
          case 'U': // specify user name
            Prop.put(StaticOptions.USER, arg.string());
            break;
          case 'z': // suppress logging
            Prop.put(StaticOptions.LOG, Boolean.toString(false));
            break;
          default:
            throw arg.usage();
        }
      } else {
        if(!S_STOP.equalsIgnoreCase(arg.string())) throw arg.usage();
        stop = true;
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
  public static void start(final int port, final boolean ssl, final String... args)
      throws BaseXException {

    // start server and check if it caused an error message
    final String error = Util.error(Util.start(BaseXHTTP.class, args), 2000);
    if(error != null) throw new BaseXException(error.trim());

    // try to connect to the new server instance
    if(!ping(S_LOCALHOST, port, ssl)) throw new BaseXException(CONNECTION_ERROR_X, port);
  }

  /**
   * Stops the server.
   * @param host server host
   * @param port server port
   * @throws IOException I/O exception
   */
  public static void stop(final String host, final int port) throws IOException {
    final IOFile stopFile = stopFile(port);
    stopFile.touch();
    try(Socket s = new Socket(host, port)) {
    } catch(final ConnectException ex) {
      throw new IOException(Util.info(CONNECTION_ERROR_X, port));
    }
    do Performance.sleep(10); while(stopFile.exists());
  }

  /**
   * Checks if a server is running.
   * @param host host
   * @param port server port
   * @param ssl encryption via HTTPS
   * @return boolean success
   */
  public static boolean ping(final String host, final int port, final boolean ssl) {
    try(InputStream is = new IOUrl((ssl ? "https://" : "http://") + host + ':' + port).
      connection().getInputStream()) {
      // create connection
      return true;
    } catch(final FileNotFoundException | SSLHandshakeException ex) {
      // if page is not found, server is running
      // if SSL handshake failed server is running, otherwise SSLException
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }

  /**
   * Generates a stop file for the specified port.
   * @param port server port
   * @return stop file
   */
  private static IOFile stopFile(final int port) {
    return new IOFile(Prop.TMP, Util.className(BaseXHTTP.class) + port);
  }

  @Override
  public String header() {
    return Util.info(S_CONSOLE_X, S_HTTP_SERVER);
  }

  @Override
  public String usage() {
    return S_HTTPINFO;
  }

  /** Monitor for stopping the Jetty server. */
  private final class StopServer extends Thread {
    /** Server socket. */
    private final ServerSocket socket;
    /** Stop file. */
    private final IOFile stopFile;

    /**
     * Constructor.
     * @param host host address
     * @param port stop port
     * @throws IOException I/O exception
     */
    StopServer(final String host, final int port) throws IOException {
      final InetAddress addr = host.isEmpty() ? null : InetAddress.getByName(host);
      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(addr, port));
      stopFile = stopFile(port);
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        while(true) {
          try(Socket s = socket.accept()) { }
          if(stopFile.exists()) {
            socket.close();
            jetty.stop();
            HTTPContext.close();
            Prop.clear();
            if(!stopFile.delete()) {
              context.log.writeServer(LogType.ERROR, Util.info(FILE_NOT_DELETED_X, stopFile));
            }
            break;
          }
        }
      } catch(final Exception ex) {
        Util.stack(ex);
      }
    }
  }
}
