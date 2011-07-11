package org.basex;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.BufferInput;
import org.basex.io.IOFile;
import org.basex.server.ClientSession;
import org.basex.server.LocalSession;
import org.basex.server.Log;
import org.basex.server.LoginException;
import org.basex.server.ClientListener;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This is the starter class for running the database server. It handles
 * concurrent requests from multiple users.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public class BaseXServer extends Main {
  /** Quiet mode (no logging). */
  protected boolean quiet;
  /** Start as daemon. */
  protected boolean service;
  /** Log. */
  protected Log log;

  /** Event server socket. */
  ServerSocket esocket;
  /** Flag for server activity. */
  boolean running;
  /** Stop file. */
  IOFile stop;

  /** EventsListener. */
  private final EventListener events = new EventListener();
  /** Server socket. */
  private ServerSocket socket;
  /** User query. */
  private String commands;

  /**
   * Main method, launching the server process. Command-line arguments are
   * listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXServer(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public BaseXServer(final String... args) {
    super(args);
    check(success);

    final int port = context.prop.num(Prop.SERVERPORT);
    if(service) {
      Util.outln(start(port, getClass(), args));
      Performance.sleep(1000);
      return;
    }

    try {
      // execute command-line arguments
      if(commands != null) {
        final Boolean b = execute(commands);
        check(b == null || b);
      }

      log = new Log(context, quiet);
      log.write(SERVERSTART);
      socket = new ServerSocket(port);
      esocket = new ServerSocket(context.prop.num(Prop.EVENTPORT));
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

      if(console) quit(console());
    } catch(final Exception ex) {
      if(log != null) log.write(ex.getMessage());
      Util.errln(Util.server(ex));
      check(false);
    }
  }

  @Override
  public void run() {
    events.start();
    running = true;
    while(running) {
      try {
        final Socket s = socket.accept();
        final ClientListener sp = new ClientListener(s, context, log);
        if(stop.exists()) {
          if(!stop.delete()) log.write(Util.info(DBNOTDELETED, stop));
          quit(false);
        } else if(sp.init()) {
          context.add(sp);
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
  public void quit(final boolean user) {
    if(!running) return;
    running = false;
    super.quit(user);

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
  protected boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this, SERVERINFO, Util.info(CONSOLE,
        SERVERMODE));
    boolean daemon = false;
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'c') {
          // send database commands
          commands = arg.remaining();
        } else if(c == 'd') {
          // activate debug mode
          context.prop.set(Prop.DEBUG, true);
        } else if(c == 'D') {
          // hidden flag: daemon mode
          daemon = true;
        } else if(c == 'e') {
          // parse event port
          context.prop.set(Prop.EVENTPORT, arg.num());
        } else if(c == 'i') {
          // activate interactive mode
          console = true;
        } else if(c == 'p') {
          // parse server port
          context.prop.set(Prop.SERVERPORT, arg.num());
        } else if(c == 's') {
          // set service flag
          service = !daemon;
        } else if(c == 'z') {
          // suppress logging
          quiet = true;
        } else {
          arg.check(false);
        }
      } else {
        arg.check(false);
        if(arg.string().equalsIgnoreCase("stop")) {
          stop(context.prop.num(Prop.SERVERPORT),
              context.prop.num(Prop.EVENTPORT));
          Performance.sleep(1000);
          return false;
        }
      }
    }
    if(context.prop.num(Prop.SERVERPORT) == context.prop.num(Prop.EVENTPORT)) {
      arg.check(error(null, SERVERPORTS));
    }
    return arg.finish();
  }

  /**
   * Stops the server of this instance.
   */
  public void stop() {
    try {
      stop.write(Token.EMPTY);
      new Socket(LOCALHOST, context.prop.num(Prop.EVENTPORT));
      new Socket(LOCALHOST, context.prop.num(Prop.SERVERPORT));
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
    }
  }

  // STATIC METHODS ===========================================================

  /**
   * Starts the specified class in a separate process.
   * @param port server port
   * @param clz class to start
   * @param args command-line arguments
   * @return error string or {@code null}
   */
  public static String start(final int port, final Class<?> clz,
      final String... args) {

    // check if server is already running (needs some time)
    if(ping(LOCALHOST, port)) return SERVERBIND;

    final StringList sl = new StringList();
    final String[] largs = { "java", "-Xmx" + Runtime.getRuntime().maxMemory(),
        "-cp", System.getProperty("java.class.path"), clz.getName(), "-D", };
    for(final String a : largs)
      sl.add(a);
    for(final String a : args)
      sl.add(a);

    try {
      new ProcessBuilder(sl.toArray()).start();

      // try to connect to the new server instance
      for(int c = 0; c < 5; ++c) {
        if(ping(LOCALHOST, port)) return SERVERSTART;
        Performance.sleep(100);
      }
    } catch(final IOException ex) {
      Util.notexpected(ex);
    }
    return SERVERERROR;
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
   */
  public static void stop(final int port, final int eport) {
    final IOFile stop = stopFile(port);
    try {
      stop.write(Token.EMPTY);
      new Socket(LOCALHOST, eport);
      new Socket(LOCALHOST, port);
      while(ping(LOCALHOST, port)) Performance.sleep(100);
      Util.outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      stop.delete();
      Util.errln(Util.server(ex));
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
          final long id = Long.parseLong(bi.readString());
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
