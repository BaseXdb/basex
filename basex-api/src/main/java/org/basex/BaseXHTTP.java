package org.basex;

import static org.basex.core.Text.*;
import static org.basex.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.server.*;
import org.basex.server.Log.*;
import org.basex.util.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.webapp.*;
import org.eclipse.jetty.xml.*;

/**
 * This is the main class for the starting the database HTTP services.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 * @author Dirk Kirsten
 */
public final class BaseXHTTP extends CLI {
  /** HTTP server. */
  private Server jetty;
  /** HTTP port. */
  private int port;
  /** HTTP host. */
  private String host;
  /** HTTP stop port. */
  private int stopPort;
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
    super(null, args);
    // context must be initialized after parsing of arguments
    context = HTTPContext.context();
    // execute initial command-line arguments
    for(final Pair<String, String> cmd : commands) {
      if(!execute(cmd)) return;
    }

    // create jetty instance and set default context to HTTP path
    final StaticOptions sopts = context.soptions;
    final String webapp = sopts.get(StaticOptions.WEBPATH);
    final WebAppContext wac = new WebAppContext(webapp, "/");
    jetty = (Server) new XmlConfiguration(initJetty(webapp).inputStream()).configure();
    jetty.setHandler(wac);

    final ArrayList<ServerConnector> conns = new ArrayList<>(1);
    for(final Connector conn : jetty.getConnectors()) {
      if(conn instanceof ServerConnector) conns.add((ServerConnector) conn);
    }
    if(conns.isEmpty())
      throw new BaseXException("No Jetty connectors defined in " + JETTYCONF + '.');

    stopPort = sopts.get(StaticOptions.STOPPORT);
    host = sopts.get(StaticOptions.SERVERHOST);
    final ServerConnector conn1 = conns.get(0);
    if(port != 0) conn1.setPort(port);

    // info strings
    final String startX = HTTP + ' ' + SRV_STARTED_PORT_X;
    final String stopX = HTTP + ' ' + SRV_STOPPED_PORT_X;

    if(stop) {
      stop();
      if(!quiet) for(final ServerConnector conn : conns) Util.outln(stopX, conn.getPort());
      // keep message visible for a while
      Performance.sleep(1000);
      return;
    }

    // start web server in a new process
    if(service) {
      start(args);
      if(!quiet) for(final ServerConnector conn : conns) Util.outln(startX, conn.getPort());
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
      for(final ServerConnector conn : conns) Util.outln(startX, conn.getPort());
    }

    // initialize web.xml settings, assign system properties and run database server.
    // the call of this function may already have been triggered during the start of jetty
    HTTPContext.init(wac.getServletContext());

    // start daemon for stopping web server
    if(stopPort > 0) new StopServer().start();

    // show info when HTTP server is aborted. needs to be called in constructor:
    // otherwise, it may only be called if the JVM process is already shut down
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if(!quiet) {
        for(final ServerConnector conn : conns) Util.outln(stopX, conn.getPort());
      }
      final Log log = context.log;
      if(log != null) {
        for(final ServerConnector conn : conns) {
          log.writeServer(LogType.OK, Util.info(stopX, conn.getPort()));
        }
      }
      context.close();
    }));

    // log server start at very end (logging flag could have been updated by web.xml)
    for(final ServerConnector conn : conns) {
      context.log.writeServer(LogType.OK, Util.info(startX, conn.getPort()));
    }

    // start persistent jobs
    new Jobs(context).run();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    if(stopPort > 0) stop(host.isEmpty() ? S_LOCALHOST : host, stopPort);
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
    boolean daemon = true;

    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'c': // gather up database commands
            commands.add(input(arg.string()));
            break;
          case 'd': // activate debug mode
            Prop.put(StaticOptions.DEBUG, Boolean.toString(true));
            Prop.debug = true;
            break;
          case 'D': // hidden flag: daemon mode
            daemon = false;
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
            service = daemon;
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

  // STATIC METHODS ===============================================================================

  /**
   * Starts the HTTP server in a separate process.
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  public static void start(final String... args) throws BaseXException {
    // start server and check if it caused an error message
    final String error = Util.error(Util.start(BaseXHTTP.class, args), 2000);
    if(error != null) throw new BaseXException(error.trim());
  }

  /**
   * Stops the server.
   * @param host server host
   * @param port server port
   * @throws IOException I/O exception
   */
  public static void stop(final String host, final int port) throws IOException {
    // create stop file
    final IOFile stopFile = stopFile(BaseXHTTP.class, port);
    stopFile.touch();

    // try to connect the server
    try(Socket s = new Socket(host, port)) {
      // no action
    } catch(final ConnectException ex) {
      Util.debug(ex);
      stopFile.delete();
      throw new IOException(Util.info(CONNECTION_ERROR_X, port));
    }
    // wait until server was stopped
    do Performance.sleep(10); while(stopFile.exists());
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
     * @throws IOException I/O exception
     */
    StopServer() throws IOException {
      final InetAddress addr = host.isEmpty() ? null : InetAddress.getByName(host);
      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(addr, stopPort));
      stopFile = stopFile(BaseXHTTP.class, stopPort);
    }

    @Override
    public void run() {
      try {
        while(true) {
          Util.outln(HTTP + ' ' + STOP + ' ' + SRV_STARTED_PORT_X, stopPort);
          try(Socket s = socket.accept()) { /* no action */ }
          if(stopFile.exists()) {
            socket.close();
            Util.outln(HTTP + ' ' + STOP + ' ' + SRV_STOPPED_PORT_X, stopPort);
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
