package org.basex;

import static org.basex.core.Text.*;
import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.log.*;
import org.basex.util.options.*;
import org.eclipse.jetty.compression.gzip.*;
import org.eclipse.jetty.compression.server.*;
import org.eclipse.jetty.ee10.webapp.*;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.xml.*;

/**
 * This is the main class for the starting the database HTTP services.
 *
 * @author BaseX Team, BSD License
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
  /** Options assigned on the command line. */
  private HashMap<Option<?>, String> options;

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

    if(!quiet) Util.println(header());

    // initialize configuration files and initialize HTTP context
    final String webapp = soptions.get(StaticOptions.WEBPATH);
    final WebAppContext wac = new WebAppContext(webapp, "/");
    locate(WEBCONF, webapp);

    hc = HTTPContext.get();
    hc.init(soptions, options);

    // create jetty instance
    final ServerConnector sc = connector(new IOFile(webapp, JETTYCONF),
        soptions.get(StaticOptions.HTTPPORT), options.containsKey(StaticOptions.HTTPPORT));
    final int port = sc.getPort();
    jetty = sc.getServer();
    jetty.setHandler(soptions.get(StaticOptions.GZIP) ? gzip(wac) : wac);
    JakartaWebSocketServletContainerInitializer.configure(wac, null);

    // info strings
    final String started = Util.info(HTTP + ' ' + SRV_STARTED_PORT_X, port);
    final String stopped = Util.info(HTTP + ' ' + SRV_STOPPED_PORT_X, port);

    // stop web server
    if(stop) {
      stop();
      // the HTTP port of the stopped instance is unknown: report the stop port
      if(!quiet) Util.println(HTTP + " STOP " + SRV_STOPPED_PORT_X,
          soptions.get(StaticOptions.STOPPORT));
      return;
    }

    // start web server in a new Java process
    if(service) {
      start(args);
      if(!quiet) {
        // output user info, keep message visible for a while
        Util.println(started);
        if(!soptions.get(StaticOptions.HTTPLOCAL)) {
          Util.println(SRV_STARTED_PORT_X, soptions.get(StaticOptions.SERVERPORT));
        }
        Performance.sleep(1000);
      }
      return;
    }

    // start web server
    try {
      jetty.start();
    } catch(final BindException ex) {
      throw new BaseXException(HTTP + ' ' + SRV_RUNNING_X, port, ex);
    }
    // throw cached exception that did not break the servlet architecture
    final IOException ex = hc.exception();
    if(ex != null) throw ex;

    // initialize web.xml settings, assign system properties and run database server.
    // the call of this function may already have been triggered during the start of jetty
    hc.init(wac.getServletContext());
    context = hc.context();

    // start daemon for stopping the HTTP server
    final int stopPort = soptions.get(StaticOptions.STOPPORT);
    if(stopPort > 0) new StopServer(stopPort).start();

    // show info when HTTP server is aborted. needs to be called in constructor:
    // otherwise, it may only be called if the JVM process is already shut down
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if(!quiet) Util.println(stopped);
      context.log.writeServer(LogType.OK, stopped);
      context.close();
    }));

    // show start message
    if(!quiet) Util.println(started);

    // log server start at very end (logging flag could have been updated by web.xml)
    context.log.writeServer(LogType.OK, started);

    // execute initial command-line arguments
    for(final Entry<String, String> command : commands) {
      if(!execute(command)) return;
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
   * @throws IOException I/O exception
   */
  private static void locate(final String file, final String root) throws IOException {
    // try to locate file from development branch
    final IO io = new IOFile("src/main/webapp", file);
    byte[] data = null;
    if(io.exists()) {
      data = io.read();
      // check if resource path exists
      IOFile dir = new IOFile("src/main/resources");
      if(dir.exists()) {
        dir = new IOFile(dir, file);
        // update file in resource path if it has changed
        if(!dir.exists() || !Token.eq(data, dir.read())) {
          Util.errln("Updating " +  dir);
          dir.write(data);
        }
      }
    }

    final IOFile target = new IOFile(root, file);
    if(target.exists()) return;

    if(data == null) {
      // try to locate file from resource path
      try(InputStream is = BaseXHTTP.class.getResourceAsStream('/' + file)) {
        if(is == null) throw new BaseXException(io + " not found.");
        data = new IOStream(is).read();
      }
    }
    // create configuration file
    Util.errln("Creating " +  target);
    target.write(data);
  }

  /**
   * Creates the Jetty server (configured by the file if it exists) and returns its connector.
   * @param jettyXml Jetty configuration file
   * @param port HTTP port
   * @param force enforce the port if the configuration file assigns its own port
   * @return server connector
   * @throws Exception exception
   */
  private static ServerConnector connector(final IOFile jettyXml, final int port,
      final boolean force) throws Exception {
    if(jettyXml.exists()) {
      final Resource resource = new PathResourceFactory().newResource(jettyXml.file().toPath());
      final XmlConfiguration xc = new XmlConfiguration(resource);
      xc.getProperties().put("jetty.http.port", Integer.toString(port));
      final Server server = (Server) xc.configure();
      ServerConnector sc = null;
      for(final Connector conn : server.getConnectors()) {
        if(conn instanceof final ServerConnector s) sc = s;
      }
      if(sc == null) throw new BaseXException("No Jetty connector defined in " + JETTYCONF + '.');
      if(force || sc.getPort() == 0) sc.setPort(port);
      return sc;
    }
    final Server server = new Server();
    final ServerConnector sc = new ServerConnector(server);
    sc.setIdleTimeout(60000);
    sc.setPort(port);
    server.addConnector(sc);
    return sc;
  }

  /**
   * Returns a GZIP handler.
   * @param wac web application context
   * @return handler
   */
  private static Handler gzip(final WebAppContext wac) {
    final CompressionHandler ch = new CompressionHandler();
    ch.putCompression(new GzipCompression());
    ch.putConfiguration("/", CompressionConfig.builder().defaults().
        compressIncludeMethod("PUT").decompressIncludeMethod("PUT").build());
    ch.setHandler(wac);
    return ch;
  }

  @Override
  protected void parseArgs() throws IOException {
    // invoked by the super constructor: field initializers have not been run yet
    options = new HashMap<>();
    final MainParser arg = new MainParser(this);
    boolean daemon = true;

    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'c': // database command
            commands.add(commands(arg.string()));
            break;
          case 'C': // command script
            commands.add(script(arg.string()));
            break;
          case 'd': // activate debug mode
            option(StaticOptions.DEBUG, Boolean.toString(true));
            Prop.debug = true;
            break;
          case 'D': // hidden flag: daemon mode
            daemon = false;
            break;
          case 'g': // enable GZIP compression
            option(StaticOptions.GZIP, Boolean.toString(true));
            break;
          case 'h': // parse HTTP port
            option(StaticOptions.HTTPPORT, Integer.toString(arg.number()));
            break;
          case 'l': // use local mode
            option(StaticOptions.HTTPLOCAL, Boolean.toString(true));
            break;
          case 'L': // start database server in addition
            option(StaticOptions.HTTPLOCAL, Boolean.toString(false));
            break;
          case 'n': // parse host name
            final String n = arg.string();
            option(StaticOptions.HOST, n);
            option(StaticOptions.SERVERHOST, n);
            break;
          case 'p': // parse server port
            final int p = arg.number();
            option(StaticOptions.PORT, Integer.toString(p));
            option(StaticOptions.SERVERPORT, Integer.toString(p));
            break;
          case 'q': // quiet flag (hidden)
            quiet = true;
            break;
          case 's': // parse stop port
            option(StaticOptions.STOPPORT, Integer.toString(arg.number()));
            break;
          case 'S': // set service flag
            service = daemon;
            break;
          case 'U': // specify username
            option(StaticOptions.USER, arg.string());
            break;
          case 'z': // suppress logging
            option(StaticOptions.LOG, "");
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

  /**
   * Assigns a command-line option as global option.
   * @param option option
   * @param value value
   */
  private void option(final Option<?> option, final String value) {
    Prop.put(option, value);
    options.put(option, value);
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
    try(Socket socket = new Socket(host, port)) {
      socket.setTcpNoDelay(true);
      final PrintOutput out = PrintOutput.get(socket.getOutputStream());
      final BufferInput in = BufferInput.get(socket.getInputStream());
      // send the shutdown request, wait for the completion flag (0: stopped)
      out.print(BaseXServer.STOP);
      out.write(0);
      out.flush();
      if(in.read() != 0) throw new IOException(Util.info(CONNECTION_ERROR_X, port));
    } catch(final IOException ex) {
      throw new IOException(Util.info(CONNECTION_ERROR_X, port), ex);
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
    }

    @Override
    public void run() {
      Util.println(HTTP + " STOP " + SRV_STARTED_PORT_X, stopPort);
      try {
        while(true) {
          try(Socket s = socket.accept()) {
            // bound the read so an idle connection cannot block the monitor
            s.setSoTimeout(5000);
            final PrintOutput po = PrintOutput.get(s.getOutputStream());
            final boolean stp;
            try {
              // honor same-host shutdown requests only
              stp = BaseXServer.STOP.equals(BufferInput.get(s.getInputStream()).readString()) &&
                  Util.localHost(s.getInetAddress());
              if(!stp) {
                po.write(1);
                po.flush();
              }
            } catch(final IOException ex) {
              // ignore malformed, idle or aborted connections
              Util.debug(ex);
              continue;
            }
            if(!stp) continue;

            socket.close();
            Util.println(HTTP + " STOP " + SRV_STOPPED_PORT_X, stopPort);
            jetty.stop();
            hc.close();
            Prop.clear();
            try {
              // best-effort completion flag; the server is already shutting down
              po.write(0);
              po.flush();
            } catch(final IOException ex) {
              Util.debug(ex);
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
