package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.LocalSession;
import org.basex.core.Session;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.server.ClientSession;
import org.basex.server.Log;
import org.basex.server.LoginException;
import org.basex.server.ServerProcess;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.Token;

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
  /** Quiet mode (no logging). */
  private boolean quiet;
  /** Stop file. */
  private static final IO STOP = IO.get(Prop.TMP + "bxs");
  /** Server socket. */
  private ServerSocket server;
  /** Flag for server activity. */
  private boolean running;

  /**
   * Main method, launching the server process. Command-line arguments can be
   * listed with the <code>-h</code> argument.
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
    log = new Log(context, quiet);

    try {
      server = new ServerSocket(context.prop.num(Prop.SERVERPORT));
      new Thread(this).start();
      do Performance.sleep(200); while(!running);

      outln(CONSOLE, SERVERMODE, console ? CONSOLE2 : SERVERSTART);
      if(console) quit(console());
    } catch(final Exception ex) {
      log.write(ex.getMessage());
      errln(server(ex));
    }
  }

  /**
   * Server thread.
   */
  public void run() {
    running = true;
    while(running) {
      try {
        final ServerProcess s = new ServerProcess(server.accept(), this);
        if(STOP.exists()) {
          STOP.delete();
          quit(false);
        } else if(s.init()) {
          context.add(s);
        }
      } catch(final IOException ex) {
        // socket was closed..
        break;
      }
    }
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
      ex.printStackTrace();
    }

    console = false;
    context.close();
  }

  @Override
  protected Session session() {
    if(session == null) session = new LocalSession(context);
    return session;
  }

  @Override
  protected void parseArguments(final String[] args) {
    final Args arg = new Args(args);
    success = true;
    while(arg.more() && success) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'd') {
          // activate debug mode
          context.prop.set(Prop.DEBUG, true);
        } else if(c == 'i') {
          // activate interactive mode
          console = true;
        } else if(c == 'p') {
          // parse server port
          context.prop.set(Prop.SERVERPORT, arg.num());
        } else if(c == 'z') {
          // suppress logging
          quiet = true;
        } else {
          success = false;
        }
      } else {
        success = false;
        if(arg.string().equalsIgnoreCase("stop")) {
          stop(context.prop.num(Prop.SERVERPORT));
          return;
        }
      }
    }
    if(!success) outln(SERVERINFO);
  }

  /**
   * Starts the server in a separate process.
   * @param port server port
   * @return true if start was successful
   */
  public static boolean start(final int port) {
    final String path = IOFile.file(BaseXServer.class.getProtectionDomain().
        getCodeSource().getLocation().toString());
    final String mem = "-Xmx" + Runtime.getRuntime().maxMemory();
    final String clazz = BaseXServer.class.getName();
    try {
      new ProcessBuilder(new String[] { "java", mem, "-cp", path, clazz,
          "-p", String.valueOf(port) }).start();

      for(int c = 0; c < 6; c++) {
        if(ping("localhost", port)) return true;
        Performance.sleep(500);
      }
    } catch(final IOException ex) {
      Main.debug(ex);
    }
    return false;
  }

  /**
   * Checks if a server is running.
   * @param host host
   * @param port port
   * @return boolean success
   */
  public static boolean ping(final String host, final int port) {
    try {
      new ClientSession(host, port, "", "");
    } catch(final IOException e) {
      if(e instanceof LoginException) return true;
    }
    return false;
  }

  /**
   * Stops the server.
   * @param port server port
   */
  public static void stop(final int port) {
    try {
      STOP.write(Token.EMPTY);
      new Socket("localhost", port);
      outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      errln(server(ex));
    }
    return;
  }
}
