package org.basex;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.server.*;
import org.basex.server.Log.LogType;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is the starter class for running the database server. It handles
 * concurrent requests from multiple users.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class BaseXServer extends CLI implements Runnable {
  /** New sessions. */
  private final HashSet<ClientListener> authorizing = new HashSet<>();
  /** Indicates if server is running. */
  private volatile boolean running;
  /** Indicates if server is to be stopped. */
  private volatile boolean stop;
  /** Initial commands. */
  private StringList commands;
  /** Server socket. */
  private ServerSocket socket;
  /** Start as service. */
  private boolean service;
  /** Daemon flag. */
  private boolean daemon;
  /** Quiet flag. */
  private boolean quiet;
  /** Stop file. */
  private IOFile stopFile;

  /**
   * Main method, launching the server process.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseXServer(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public BaseXServer(final String... args) throws IOException {
    this(null, args);
  }

  /**
   * Constructor.
   * @param ctx database context (can be {@code null})
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public BaseXServer(final Context ctx, final String... args) throws IOException {
    super(args, ctx);

    final StaticOptions sopts = context.soptions;
    final int port = sopts.get(StaticOptions.SERVERPORT);
    final String host = sopts.get(StaticOptions.SERVERHOST);
    final InetAddress addr = host.isEmpty() ? null : InetAddress.getByName(host);

    if(stop) {
      stop();
      if(!quiet) Util.outln(SRV_STOPPED_PORT_X, port);
      // keep message visible for a while
      Performance.sleep(1000);
      return;
    }

    if(service) {
      start(port, args);
      if(!quiet) Util.outln(SRV_STARTED_PORT_X, port);
      Performance.sleep(1000);
      return;
    }

    try {
      // execute initial command-line arguments
      for(final String cmd : commands) execute(cmd, null);

      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(addr, port));
      stopFile = stopFile(port);
    } catch(final Exception ex) {
      context.log.writeServer(LogType.ERROR, Util.message(ex));
      if(ex instanceof BindException) throw new BaseXException(SRV_RUNNING_X, port);
      if(ex instanceof IOException) throw ex;
      throw new BaseXException(ex.getLocalizedMessage());
    }

    new Thread(this).start();

    // show info that server has been started
    final String startX = Util.info(SRV_STARTED_PORT_X, port);
    if(!quiet) {
      if(!daemon) Util.outln(header());
      Util.outln(startX);
    }
    context.log.writeServer(LogType.OK, startX);

    // show info when server is aborted
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        close();
      }
    });
  }

  @Override
  public void run() {
    running = true;
    while(running) {
      try {
        final Socket s = socket.accept();
        if(stopFile.exists()) {
          close();
        } else {
          // drop inactive connections
          final long ka = context.soptions.get(StaticOptions.KEEPALIVE) * 1000L;
          if(ka > 0) {
            final long ms = System.currentTimeMillis();
            for(final ClientListener cs : context.sessions) {
              if(ms - cs.last > ka) cs.close();
            }
          }
          // create client listener, stop authentication after timeout
          final ClientListener cl = new ClientListener(s, context, this);
          if(ka > 0) {
            cl.timeout.schedule(new TimerTask() {
              @Override
              public void run() {
                cl.close();
              }
            }, ka);
            authorizing.add(cl);
          }
          cl.start();
        }
      } catch(final SocketException ex) {
        break;
      } catch(final Throwable ex) {
        // socket may have been unexpectedly closed
        Util.errln(ex);
        context.log.writeServer(LogType.ERROR, Util.message(ex));
        break;
      }
    }
  }

  /**
   * Generates a stop file for the specified port.
   * @param port server port
   * @return stop file
   */
  private static IOFile stopFile(final int port) {
    return new IOFile(Prop.TMP, Util.className(BaseXServer.class) + port);
  }

  /**
   * Shuts down the server.
   */
  private synchronized void close() {
    if(!running) return;

    for(final ClientListener cl : authorizing) {
      remove(cl);
      cl.close();
    }
    context.sessions.close();

    try {
      // close interactive input if server was stopped by another process
      socket.close();
    } catch(final IOException ex) {
      Util.errln(ex);
      context.log.writeServer(LogType.ERROR, Util.message(ex));
    }

    final int port = context.soptions.get(StaticOptions.SERVERPORT);
    final String stopX = Util.info(SRV_STOPPED_PORT_X, port);
    if(!quiet) Util.outln(stopX);
    context.log.writeServer(LogType.OK, stopX);

    // close database context
    context.close();

    if(!stopFile.delete()) {
      context.log.writeServer(LogType.ERROR, Util.info(FILE_NOT_DELETED_X, stopFile));
    }
    running = false;
  }

  @Override
  protected void parseArgs() throws IOException {
    final MainParser arg = new MainParser(this);
    commands = new StringList();

    while(arg.more()) {
      if(arg.dash()) {
        switch(arg.next()) {
          case 'c': // send database commands
            commands.add(arg.string());
            break;
          case 'd': // activate debug mode
            Prop.debug = true;
            break;
          case 'D': // hidden flag: daemon mode
            daemon = true;
            break;
          case 'n': // parse host the server is bound to
            context.soptions.set(StaticOptions.SERVERHOST, arg.string());
            break;
          case 'p': // parse server port
            context.soptions.set(StaticOptions.SERVERPORT, arg.number());
            break;
          case 'q': // quiet flag (hidden)
            quiet = true;
            break;
          case 'S': // set service flag
            service = !daemon;
            break;
          case 'z': // suppress logging
            context.soptions.set(StaticOptions.LOG, false);
            break;
          default:
            throw arg.usage();
        }
      } else {
        if(S_STOP.equalsIgnoreCase(arg.string())) {
          stop = true;
        } else {
          throw arg.usage();
        }
      }
    }
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  public void stop() throws IOException {
    final StaticOptions sopts = context.soptions;
    final int port = sopts.get(StaticOptions.SERVERPORT);
    final String host = sopts.get(StaticOptions.SERVERHOST);
    stop(host.isEmpty() ? S_LOCALHOST : host, port);
  }

  // STATIC METHODS ===========================================================

  /**
   * Starts the database server in a separate process.
   * @param port server port
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  public static void start(final int port, final String... args) throws BaseXException {
    // start server and check if it caused an error message
    final String error = Util.error(Util.start(BaseXServer.class, args), 2000);
    if(error != null) throw new BaseXException(error.trim());

    // try to connect to the new server instance
    if(!ping(S_LOCALHOST, port)) throw new BaseXException(CONNECTION_ERROR_X, port);
  }

  /**
   * Checks if a server is running.
   * @param host host
   * @param port server port
   * @return boolean success
   */
  public static boolean ping(final String host, final int port) {
    try {
      // connect server with invalid login data
      try(ClientSession cs = new ClientSession(host, port, "", "")) { }
      return false;
    } catch(final LoginException ex) {
      // if login was checked, server is running
      return true;
    } catch(final IOException ex) {
      return false;
    }
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
    // wait and check if server was really stopped
    do Performance.sleep(10); while(stopFile.exists());
  }

  /**
   * Removes a client listener that is waiting for authentication.
   * @param client client to be removed
   */
  public void remove(final ClientListener client) {
    synchronized(authorizing) {
      client.timeout.cancel();
      authorizing.remove(client);
    }
  }

  @Override
  public String header() {
    return Util.info(S_CONSOLE_X, S_SERVER);
  }

  @Override
  public String usage() {
    return S_SERVERINFO;
  }
}
