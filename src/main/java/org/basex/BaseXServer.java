package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.server.ClientSession;
import org.basex.server.LocalSession;
import org.basex.server.Log;
import org.basex.server.LoginException;
import org.basex.server.ServerProcess;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This is the starter class for the database server.
 * It handles concurrent requests from multiple users.
 * Add the '-h' option to get a list on all available command-line
 * arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class BaseXServer extends Main implements Runnable {
  /** Log. */
  public Log log;

  /** User query. */
  private String commands;
  /** Quiet mode (no logging). */
  private boolean quiet;
  /** Start as daemon. */
  private boolean service;
  /** Server socket. */
  private ServerSocket server;
  /** Flag for server activity. */
  private boolean running;
  /** Stop file. */
  private IO stop;

  /**
   * Main method, launching the server process. Command-line arguments can be
   * listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    new BaseXServer(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public BaseXServer(final String... args) {
    super(args);
    if(!success) return;

    final int port = context.prop.num(Prop.SERVERPORT);
    if(service) {
      Util.outln(start(port));
      return;
    }

    log = new Log(context, quiet);
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

    try {
      server = new ServerSocket(port);
      new Thread(this).start();
      do Performance.sleep(100); while(!running);

      Util.outln(CONSOLE, SERVERMODE, console ? CONSOLE2 : SERVERSTART);

      if(commands != null) {
        // execute command-line arguments
        execute(commands);
      }
      
      if(console) quit(console());
    } catch(final Exception ex) {
      log.write(ex.getMessage());
      Util.errln(Util.server(ex));
    }
  }

  @Override
  public void run() {
    running = true;
    while(running) {
      try {
        final Socket s = server.accept();
        final ServerProcess sp = new ServerProcess(s, context, log);
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
  private static IO stopFile(final int port) {
    return IO.get(Prop.TMP + Util.name(BaseXServer.class) + port);
  }

  @Override
  public void quit(final boolean user) {
    if(!running) return;
    running = false;
    super.quit(user);

    try {
      // close input streams
      if(console) System.in.close();
      server.close();
    } catch(final IOException ex) {
      log.write(ex.getMessage());
      Util.stack(ex);
    }
    console = false;
    Performance.sleep(100);
  }

  @Override
  protected Session session() {
    if(session == null) session = new LocalSession(context);
    return session;
  }

  @Override
  protected boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this, SERVERINFO);
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'c') {
          // send database commands
          commands = arg.remaining();
        } else if(c == 'd') {
          // activate debug mode
          context.prop.set(Prop.DEBUG, true);
        } else if(c == 'i') {
          // activate interactive mode
          console = true;
        } else if(c == 'p') {
          // parse server port
          context.prop.set(Prop.SERVERPORT, arg.num());
        } else if(c == 's') {
          // parse server port
          service = true;
        } else if(c == 'z') {
          // suppress logging
          quiet = true;
        } else {
          arg.check(false);
        }
      } else {
        arg.check(false);
        if(arg.string().equalsIgnoreCase("stop")) {
          stop(context.prop.num(Prop.SERVERPORT));
          return false;
        }
      }
    }
    return arg.finish();
  }

  /**
   * Starts the server in a separate process.
   * @param port server port
   * @return error string, or null
   */
  public static String start(final int port) {
    // check if server is already running (needs some time)
    if(ping(LOCALHOST, port)) return SERVERBIND;

    final String path = Util.applicationPath();
    final String mem = "-Xmx" + Runtime.getRuntime().maxMemory();
    final String clz = BaseXServer.class.getName();
    try {
      new ProcessBuilder(new String[] { "java", mem, "-cp", path, clz,
          "-p", String.valueOf(port) }).start();

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
   * Stops the server.
   */
  public void stop() {
    final int port = context.prop.num(Prop.SERVERPORT);
    try {
      stop.write(Token.EMPTY);
      new Socket(LOCALHOST, port);
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
    }
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
   */
  public static void stop(final int port) {
    /** Stop file. */
    final IO stop = stopFile(port);
    try {
      stop.write(Token.EMPTY);
      new Socket(LOCALHOST, port);
      do Performance.sleep(200); while(ping(LOCALHOST, port));
      Util.outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      stop.delete();
      Util.errln(Util.server(ex));
    }
  }
}
