package org.basex;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.server.ClientListener;
import org.basex.server.ClientSession;
import org.basex.server.LocalSession;
import org.basex.server.Log;
import org.basex.server.LoginException;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.hash.TokenIntMap;

/**
 * This is the starter class for running the database server. It handles
 * concurrent requests from multiple users.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public class BaseXServer extends Main implements Runnable {
  /** Flag for server activity. */
  public volatile boolean running;
  /** Event server socket. */
  ServerSocket esocket;
  /** Stop file. */
  IOFile stop;
  /** Log. */
  Log log;

  /** EventsListener. */
  private final EventListener events = new EventListener();
  /** Temporarily blocked clients. */
  private final TokenIntMap blocked = new TokenIntMap();

  /** Quiet mode (no logging). */
  private boolean quiet;
  /** Start as daemon. */
  private boolean service;
  /** Stopped flag. */
  private volatile boolean stopped;

  /** Server socket. */
  private ServerSocket socket;
  /** User query. */
  private String commands;

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
  public BaseXServer(final Context ctx, final String... args)
      throws IOException {

    super(args, ctx);
    final int port = context.mprop.num(MainProp.SERVERPORT);
    final String host = context.mprop.get(MainProp.SERVERHOST);
    final InetAddress hostAddress = host.isEmpty() ? null :
      InetAddress.getByName(host);
    final int eport = context.mprop.num(MainProp.EVENTPORT);

    if(service) {
      start(port, args);
      Util.outln(SERVERSTART);
      Performance.sleep(1000);
      return;
    }

    if(stopped) {
      stop(port, eport);
      Performance.sleep(1000);
      return;
    }

    try {
      // execute command-line arguments
      if(commands != null) execute(commands);

      log = new Log(context, quiet);
      log.write(SERVERSTART);

      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(hostAddress, port));
      esocket = new ServerSocket();
      esocket.setReuseAddress(true);
      esocket.bind(new InetSocketAddress(hostAddress, eport));
      stop = stopFile(port);

      // guarantee correct shutdown...
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          log.write(SERVERSTOPPED);
          log.close();
          Util.outln(SERVERSTOPPED);
        }
      });

      new Thread(this).start();
      while(!running) Performance.sleep(100);

      Util.outln(CONSOLE + (console ? CONSOLE2 : SERVERSTART), SERVERMODE);

      if(console) {
        console();
        quit();
      }
    } catch(final IOException ex) {
      if(log != null) log.write(ex.getMessage());
      throw ex;
    }
  }

  @Override
  public void run() {
    events.start();
    running = true;
    while(running) {
      try {
        final Socket s = socket.accept();
        if(stop.exists()) {
          if(!stop.delete()) log.write(Util.info(DBNOTDELETED, stop));
          quit();
        } else {
          // drop inactive connections
          final long ka = context.mprop.num(MainProp.KEEPALIVE) * 1000L;
          if(ka > 0) {
            final long ms = System.currentTimeMillis();
            for(final ClientListener cs : context.sessions) {
              if(ms - cs.last > ka) cs.exit();
            }
          }
          new ClientListener(s, context, log, this).start();
        }
      } catch(final IOException ex) {
        // socket was closed..
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
    return new IOFile(Prop.TMP, Util.name(BaseXServer.class) + port);
  }

  @Override
  public void quit() throws IOException {
    if(!running) return;
    running = false;
    super.quit();

    try {
      // close interactive input if server was stopped by another process
      if(console) System.in.close();
      esocket.close();
      socket.close();
    } catch(final IOException ex) {
      log.write(ex.getMessage());
      Util.stack(ex);
    }
    console = false;
    context.close();
  }

  @Override
  protected Session session() {
    if(session == null) session = new LocalSession(context, out);
    return session;
  }

  @Override
  protected void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, SERVERINFO,
        Util.info(CONSOLE, SERVERMODE));
    boolean daemon = false;
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'c') {
          // send database commands
          commands = arg.remaining();
        } else if(c == 'd') {
          // activate debug mode
          context.mprop.set(MainProp.DEBUG, true);
        } else if(c == 'D') {
          // hidden flag: daemon mode
          daemon = true;
        } else if(c == 'e') {
          // parse event port
          context.mprop.set(MainProp.EVENTPORT, arg.num());
        } else if(c == 'i') {
          // activate interactive mode
          console = true;
        } else if(c == 'p') {
          // parse server port
          context.mprop.set(MainProp.SERVERPORT, arg.num());
        } else if(c == 's') {
          // set service flag
          service = !daemon;
        } else if(c == 'z') {
          // suppress logging
          quiet = true;
        } else {
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

    if(context.mprop.num(MainProp.SERVERPORT) ==
       context.mprop.num(MainProp.EVENTPORT)) {
      throw new BaseXException(SERVERPORTS);
    }
  }

  /**
   * Stops the server of this instance.
   * @throws IOException I/O exception
   */
  public void stop() throws IOException {
    stop.write(Token.EMPTY);
    new Socket(LOCALHOST, context.mprop.num(MainProp.EVENTPORT));
    final int port = context.mprop.num(MainProp.SERVERPORT);
    new Socket(LOCALHOST, port);
    while(ping(LOCALHOST, port)) Performance.sleep(100);
  }

  // STATIC METHODS ===========================================================

  /**
   * Starts the specified class in a separate process.
   * @param port server port
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  public static void start(final int port, final String... args)
      throws BaseXException {

    // check if server is already running (needs some time)
    if(ping(LOCALHOST, port)) throw new BaseXException(SERVERBIND);

    Util.start(BaseXServer.class, args);

    // try to connect to the new server instance
    for(int c = 0; c < 10; ++c) {
      if(ping(LOCALHOST, port)) return;
      Performance.sleep(100);
    }
    throw new BaseXException(SERVERERROR);
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
    } catch(final IOException ex) {
      // if login was checked, server is running
      return ex instanceof LoginException;
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
      stop.write(Token.EMPTY);
      new Socket(LOCALHOST, eport);
      new Socket(LOCALHOST, port);
      while(ping(LOCALHOST, port)) Performance.sleep(100);
      Util.outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      stop.delete();
      throw ex;
    }
  }

  /**
   * Registers the client and calculates the delay after unsuccessful logins.
   * @param client client address
   * @return delay
   */
  public int block(final byte[] client) {
    synchronized(blocked) {
      int delay = blocked.get(client);
      delay = delay == -1 ? 1 : Math.min(delay, 1024) * 2;
      blocked.add(client, delay);
      return delay;
    }
  }

  /**
   * Resets the login delay after successful login.
   * @param client client address
   */
  public void unblock(final byte[] client) {
    synchronized(blocked) {
      blocked.delete(client);
    }
  }

  /**
   * Inner class to listen for event registrations.
   *
   * @author BaseX Team 2005-11, BSD License
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
