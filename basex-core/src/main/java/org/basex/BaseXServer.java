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
  private final HashSet<ClientListener> auth = new HashSet<>();
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
  public static void main(final String[] args) {
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
   * @param ctx database context
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
      for(final String cmd : commands) execute(cmd);

      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(addr, port));
      stopFile = stopFile(port);
    } catch(final IOException ex) {
      context.log.writeServer(LogType.ERROR, Util.message(ex));
      throw ex instanceof BindException ? new IOException(Util.info(SRV_RUNNING_X, port)) : ex;
    }

    new Thread(this).start();
    do Thread.yield(); while(!running);

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
        final String stopX = Util.info(SRV_STOPPED_PORT_X, port);
        if(!quiet) Util.outln(stopX);
        context.log.writeServer(LogType.OK, stopX);
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
          if(!stopFile.delete()) {
            context.log.writeServer(LogType.ERROR, Util.info(FILE_NOT_DELETED_X, stopFile));
          }
          quit();
        } else {
          // drop inactive connections
          final long ka = context.soptions.get(StaticOptions.KEEPALIVE) * 1000L;
          if(ka > 0) {
            final long ms = System.currentTimeMillis();
            for(final ClientListener cs : context.sessions) {
              if(ms - cs.last > ka) cs.quit();
            }
          }
          final ClientListener cl = new ClientListener(s, context, this);
          // start authentication timeout
          final long to = context.soptions.get(StaticOptions.KEEPALIVE) * 1000L;
          if(to > 0) {
            cl.auth.schedule(new TimerTask() {
              @Override
              public void run() {
                cl.quitAuth();
              }
            }, to);
            auth.add(cl);
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
  private synchronized void quit() {
    if(!running) return;
    running = false;

    for(final ClientListener cs : auth) {
      remove(cs);
      cs.quitAuth();
    }
    for(final ClientListener cs : context.sessions) {
      cs.quit();
    }

    try {
      // close interactive input if server was stopped by another process
      socket.close();
    } catch(final IOException ex) {
      Util.errln(ex);
      context.log.writeServer(LogType.ERROR, Util.message(ex));
    }
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
        if("stop".equalsIgnoreCase(arg.string())) {
          stop = true;
        } else {
          throw arg.usage();
        }
      }
    }
  }

  /**
   * Stops the server of this instance.
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
    // check if server is already running (needs some time)
    final String host = S_LOCALHOST;
    if(ping(host, port)) throw new BaseXException(SRV_RUNNING_X, port);

    Util.start(BaseXServer.class, args);

    // try to connect to the new server instance
    for(int c = 1; c < 10; ++c) {
      Performance.sleep(c * 100L);
      if(ping(host, port)) return;
    }
    throw new BaseXException(CONNECTION_ERROR_X, port);
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
      try(final ClientSession cs = new ClientSession(host, port, "", "")) { }
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
    try {
      stopFile.touch();
      try(final Socket s = new Socket(host, port)) { }
      // wait and check if server was really stopped
      do Performance.sleep(100); while(ping(S_LOCALHOST, port));
    } catch(final ConnectException ex) {
      throw new IOException(Util.info(CONNECTION_ERROR_X, port));
    } finally {
      stopFile.delete();
    }
  }

  /**
   * Removes an authenticated session.
   * @param client client to be removed
   */
  public void remove(final ClientListener client) {
    synchronized(auth) {
      auth.remove(client);
      client.auth.cancel();
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
