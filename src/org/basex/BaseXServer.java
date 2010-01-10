package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.Context;
import org.basex.core.Session;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.server.Log;
import org.basex.server.Semaphore;
import org.basex.server.ServerProcess;
import org.basex.server.ServerSession;
import org.basex.util.Args;
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
  /** Semaphore for managing processes. */
  public final Semaphore sem = new Semaphore();
  /** Log. */
  public Log log;
  /** Quiet mode (no logging). */
  public boolean quiet;
  /** Stop file. */
  private static final IO STOP = IO.get(Prop.TMP + "bxs");
  /** Server socket. */
  private ServerSocket server;
  /** Flag for server activity. */
  private boolean running = true;

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
   * @param args command line arguments
   */
  public BaseXServer(final String... args) {
    super(args);
    if(!success) return;
    log = new Log(context, quiet);

    try {
      server = new ServerSocket(context.prop.num(Prop.SERVERPORT));
      new Thread(this).start();

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
    if(session == null) session = new ServerSession(context, sem);
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
          stop(context);
          return;
        }
      }
    }
    if(!success) outln(SERVERINFO);
  }

  /**
   * Stops the server.
   * @param ctx context reference
   */
  public static void stop(final Context ctx) {
    try {
      STOP.write(Token.EMPTY);
      new Socket("localhost", ctx.prop.num(Prop.SERVERPORT));
      outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      errln(server(ex));
    }
    return;
  }
}
