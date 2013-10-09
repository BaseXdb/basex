package org.basex;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is the starter class for running the database server. It handles
 * concurrent requests from multiple users.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class BaseXServer extends Main implements Runnable {
  /** Flag for server activity. */
  private volatile boolean running;
  /** Event server socket. */
  private ServerSocket esocket;
  /** Stop file. */
  private IOFile stop;

  /** New sessions. */
  private final HashSet<ClientListener> auth = new HashSet<ClientListener>();
  /** Stopped flag. */
  private volatile boolean stopped;
  /** EventsListener. */
  private EventListener events;
  /** Initial commands. */
  private StringList commands;
  /** Server socket. */
  private ServerSocket socket;
  /** Start as daemon. */
  private boolean service;

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
    final GlobalOptions gopts = context.globalopts;
    final int port = gopts.get(GlobalOptions.SERVERPORT);
    final int eport = gopts.get(GlobalOptions.EVENTPORT);
    // check if ports are distinct
    if(port == eport) throw new BaseXException(PORT_TWICE_X, port);

    final String host = gopts.get(GlobalOptions.SERVERHOST);
    final InetAddress addr = host.isEmpty() ? null : InetAddress.getByName(host);

    if(service) {
      start(port, args);
      Util.outln(SRV_STARTED_PORT_X, port);
      Performance.sleep(1000);
      return;
    }

    if(stopped) {
      stop(port, eport);
      Util.outln(SRV_STOPPED_PORT_X, port);
      Performance.sleep(1000);
      return;
    }

    try {
      // execute command-line arguments
      for(final String c : commands) execute(c);

      socket = new ServerSocket();
      // reuse address (on non-Windows machines: !Prop.WIN);
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(addr, port));
      esocket = new ServerSocket();
      esocket.setReuseAddress(true);
      esocket.bind(new InetSocketAddress(addr, eport));
      stop = stopFile(port);

      // show info when server is aborted
      context.log.writeServer(OK, Util.info(SRV_STARTED_PORT_X, port));
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          context.log.writeServer(OK, Util.info(SRV_STOPPED_PORT_X, port));
          Util.outln(SRV_STOPPED_PORT_X, port);
        }
      });

      new Thread(this).start();
      while(!running) Performance.sleep(10);

      Util.outln(CONSOLE + (console ? TRY_MORE_X :
        Util.info(SRV_STARTED_PORT_X, port)), SERVERMODE);

      if(console) {
        console();
        quit();
      }
    } catch(final IOException ex) {
      context.log.writeError(ex);
      throw ex;
    }
  }

  @Override
  public void run() {
    running = true;
    while(running) {
      try {
        final Socket s = socket.accept();
        if(stop.exists()) {
          if(!stop.delete()) {
            context.log.writeServer(ERROR_C + Util.info(FILE_NOT_DELETED_X, stop));
          }
          quit();
        } else {
          // drop inactive connections
          final long ka = context.globalopts.get(GlobalOptions.KEEPALIVE) * 1000L;
          if(ka > 0) {
            final long ms = System.currentTimeMillis();
            for(final ClientListener cs : context.sessions) {
              if(ms - cs.last > ka) cs.quit();
            }
          }
          final ClientListener cl = new ClientListener(s, context, this);
          // start authentication timeout
          final long to = context.globalopts.get(GlobalOptions.KEEPALIVE) * 1000L;
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
        context.log.writeError(ex);
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

  @Override
  protected synchronized void quit() throws IOException {
    if(!running) return;
    running = false;

    for(final ClientListener cs : auth) {
      remove(cs);
      cs.quitAuth();
    }
    for(final ClientListener cs : context.sessions) {
      cs.quit();
    }
    super.quit();

    try {
      // close interactive input if server was stopped by another process
      if(console) System.in.close();
      esocket.close();
      socket.close();
    } catch(final IOException ex) {
      Util.errln(ex);
      context.log.writeError(ex);
    }
    console = false;
  }

  @Override
  protected Session session() {
    if(session == null) session = new LocalSession(context, out);
    return session;
  }

  @Override
  protected void parseArguments(final String... args) throws IOException {
    final Args arg = new Args(args, this, SERVERINFO, Util.info(CONSOLE, SERVERMODE));
    commands = new StringList();
    boolean daemon = false;

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
          case 'e': // parse event port
            context.globalopts.set(GlobalOptions.EVENTPORT, arg.number());
            break;
          case 'i': // activate interactive mode
            console = true;
            break;
          case 'n': // parse host the server is bound to
            context.globalopts.set(GlobalOptions.SERVERHOST, arg.string());
            break;
          case 'p': // parse server port
            context.globalopts.set(GlobalOptions.SERVERPORT, arg.number());
            break;
          case 'S': // set service flag
            service = !daemon;
            break;
          case 'z': // suppress logging
            context.globalopts.set(GlobalOptions.LOG, false);
            break;
          default:
            arg.usage();
        }
      } else {
        if(arg.string().equalsIgnoreCase("stop")) {
          stopped = true;
        } else {
          arg.usage();
        }
      }
    }
  }

  /**
   * Stops the server of this instance.
   * @throws IOException I/O exception
   */
  public void stop() throws IOException {
    final GlobalOptions gopts = context.globalopts;
    stop(gopts.get(GlobalOptions.SERVERPORT), gopts.get(GlobalOptions.EVENTPORT));
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
    if(ping(LOCALHOST, port)) throw new BaseXException(SRV_RUNNING);

    Util.start(BaseXServer.class, args);

    // try to connect to the new server instance
    for(int c = 1; c < 10; ++c) {
      if(ping(LOCALHOST, port)) return;
      Performance.sleep(c * 100);
    }
    throw new BaseXException(CONNECTION_ERROR);
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
      new ClientSession(host, port, "", "");
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
   * @param port server port
   * @param eport event port
   * @throws IOException I/O exception
   */
  public static void stop(final int port, final int eport) throws IOException {
    final IOFile stop = stopFile(port);
    try {
      stop.touch();
      new Socket(LOCALHOST, eport).close();
      new Socket(LOCALHOST, port).close();
      // wait and check if server was really stopped
      do Performance.sleep(50); while(ping(LOCALHOST, port));
    } catch(final IOException ex) {
      stop.delete();
      throw ex;
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

  /**
   * Initializes the event listener.
   */
  public void initEvents() {
    if(events == null) {
      events = new EventListener();
      events.start();
    }
  }

  /**
   * Inner class to listen for event registrations.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Andreas Weiler
   */
  final class EventListener extends Thread {
    @Override
    public void run() {
      while(running) {
        try {
          final Socket es = esocket.accept();
          if(stop.exists()) {
            esocket.close();
            break;
          }
          final BufferInput bi = new BufferInput(es.getInputStream());
          final long id = Token.toLong(bi.readString());
          for(final ClientListener s : context.sessions) {
            if(s.getId() == id) {
              s.register(es);
              break;
            }
          }
        } catch(final IOException ex) {
          break;
        }
      }
    }
  }
}
