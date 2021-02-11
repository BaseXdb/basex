package org.basex;

import static org.basex.core.Text.*;
import static org.basex.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.server.Log.*;
import org.basex.util.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.gzip.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.webapp.*;
import org.eclipse.jetty.xml.*;

/**
 * This is the main class for the starting the database HTTP services.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Dirk Kirsten
 */
public final class BaseXHTTP extends CLI {
  /** Static options. */
  private final StaticOptions soptions;
  /** HTTP context. */
  private final HTTPContext hc;
  /** HTTP server. */
  private final Server jetty;

  /** Start as daemon. */
  private boolean service;
  /** Quiet flag. */
  private boolean quiet;
  /** Stop flag. */
  private boolean stop;
  /** HTTP port. */
  private int port;

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
    soptions = new StaticOptions(true);

    if(!quiet) Util.outln(header());

    hc = HTTPContext.get();
    hc.init(soptions);

    // create jetty instance and set default context to HTTP path
    final String webapp = soptions.get(StaticOptions.WEBPATH);
    final WebAppContext wac = new WebAppContext(webapp, "/");
    locate(WEBCONF, webapp);
    final IOFile url = locate(JETTYCONF, webapp);
    jetty = (Server) new XmlConfiguration(new PathResource(url.file())).configure();

    // enable GZIP support
    if(soptions.get(StaticOptions.GZIP)) {
      final GzipHandler gzip = new GzipHandler();
      gzip.setHandler(wac);
      jetty.setHandler(gzip);
    } else {
      jetty.setHandler(wac);
    }

    ServerConnector sc = null;
    for(final Connector conn : jetty.getConnectors()) {
      if(conn instanceof ServerConnector) sc = (ServerConnector) conn;
    }
    if(sc == null) throw new BaseXException("No Jetty connector defined in " + JETTYCONF + '.');
    if(port != 0) sc.setPort(port);
    else port = sc.getPort();

    // info strings
    final Function<Boolean, String> msg1 = start -> start ? SRV_STARTED_PORT_X : SRV_STOPPED_PORT_X;
    final Function<Boolean, String> msg2 = start -> Util.info(HTTP + ' ' + msg1.apply(start), port);
    // output user info, keep message visible for a while
    final Consumer<Boolean> info = start -> {
      Util.outln(msg2.apply(start));
      if(!soptions.get(StaticOptions.HTTPLOCAL)) {
        final int serverPort = soptions.get(StaticOptions.SERVERPORT);
        Util.outln(msg1.apply(start), serverPort);
      }
      Performance.sleep(1000);
    };

    // stop web server
    if(stop) {
      stop();
      if(!quiet) info.accept(false);
      return;
    }

    // start web server in a new Java process
    if(service) {
      start(args);
      if(!quiet) info.accept(true);
      return;
    }

    // start web server
    try {
      jetty.start();
    } catch(final BindException ex) {
      Util.debug(ex);
      throw new BaseXException(HTTP + ' ' + SRV_RUNNING_X, port);
    }
    // throw cached exception that did not break the servlet architecture
    final IOException ex = hc.exception();
    if(ex != null) throw ex;

    // initialize web.xml settings, assign system properties and run database server.
    // the call of this function may already have been triggered during the start of jetty
    context = hc.init(wac.getServletContext());

    // start daemon for stopping the HTTP server
    final int stopPort = soptions.get(StaticOptions.STOPPORT);
    if(stopPort > 0) new StopServer(stopPort).start();

    // show info when HTTP server is aborted. needs to be called in constructor:
    // otherwise, it may only be called if the JVM process is already shut down
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      final String message = msg2.apply(false);
      if(!quiet) Util.outln(message);
      context.log.writeServer(LogType.OK, message);
      context.close();
    }));

    // show start message
    if(!quiet) Util.outln(msg2.apply(true));

    // log server start at very end (logging flag could have been updated by web.xml)
    context.log.writeServer(LogType.OK, msg2.apply(true));

    // execute initial command-line arguments
    for(final Pair<String, String> cmd : commands) {
      if(!execute(cmd)) return;
    }
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  public void stop() throws IOException {
    final String host = soptions.get(StaticOptions.SERVERHOST);
    final int stopPort = soptions.get(StaticOptions.STOPPORT);
    if(stopPort > 0) stop(host.isEmpty() ? S_LOCALHOST : host, stopPort);
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
    final IO io = new IOFile("src/main/webapp", file);
    final byte[] data;
    if(io.exists()) {
      data = io.read();
      // check if resource path exists
      IOFile dir = new IOFile("src/main/resources");
      if(dir.exists()) {
        dir = new IOFile(dir, file);
        // update file in resource path if it has changed
        if(!dir.exists() || !Token.eq(data, dir.read())) {
          Util.errln("Updating " +  dir);
          dir.parent().md();
          dir.write(data);
        }
      }
    } else if(create) {
      // try to locate file from resource path
      try(InputStream is = BaseXHTTP.class.getResourceAsStream('/' + file)) {
        if(is == null) throw new BaseXException(io + " not found.");
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
          case 'g': // enable GZIP compression
            Prop.put(StaticOptions.GZIP, Boolean.toString(true));
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
    // do not evaluate command if additional service will be started
    if(service) commands.clear();
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
    stopFile.parent().md();
    stopFile.touch();

    // try to connect the server
    try(Socket s = new Socket(host, port)) {
      // wait until server was stopped
      do Performance.sleep(10); while(stopFile.exists());
    } catch(final IOException ex) {
      Util.debug(ex);
      stopFile.delete();
      throw new IOException(Util.info(CONNECTION_ERROR_X, port));
    }
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
    /** Port. */
    private final int stopPort;

    /**
     * Constructor.
     * @param port port to stop server
     * @throws IOException I/O exception
     */
    StopServer(final int port) throws IOException {
      stopPort = port;

      final String host = soptions.get(StaticOptions.SERVERHOST);
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
          Util.outln(HTTP + " STOP " + SRV_STARTED_PORT_X, stopPort);
          try(Socket s = socket.accept()) { /* no action */ }
          if(stopFile.exists()) {
            socket.close();
            Util.outln(HTTP + " STOP " + SRV_STOPPED_PORT_X, stopPort);
            jetty.stop();
            hc.close();
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
