package org.basex;

import static org.basex.Text.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.basex.core.ClientLauncher;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.Session;
import org.basex.core.proc.IntStop;
import org.basex.core.proc.Set;
import org.basex.data.Data;
import org.basex.util.Args;

/**
 * This is the starter class for the database server. It handles incoming
 * requests and offers some simple threading to allow simultaneous database
 * requests. Add the '-h' option to get a list on all available command-line
 * arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class BaseXServer {
  /** Database Context. */
  public final Context context = new Context();
  /** Current client connections. */
  public final ArrayList<Session> sessions = new ArrayList<Session>();

  /** Flag for server activity. */
  boolean running = true;
  /** Verbose mode. */
  boolean verbose;
  /** ServerSocket. */
  ServerSocket socket;

  /** SessionListenre. */
  private SessionListener session;
  /** InputListener. */
  private InputListener input;
  /** Flag for interactive mode. */
  private boolean interactive;

  /**
   * Main method, launching the server process. Command-line arguments can be
   * listed with the <code>-h</code> argument.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXServer(args);
  }

  /**
   * The server calls this constructor to listen on the given port for incoming
   * connections. Protocol version handshake is performed when the connection is
   * established. This constructor blocks until a client connects.
   * @param args arguments
   */
  public BaseXServer(final String... args) {
    if(!parseArguments(args)) return;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // interrupt running processes
        for(final Session s : sessions) s.core.stop();
        context.close();
      }
    });

    try {
      socket = new ServerSocket(context.prop.num(Prop.PORT));
      BaseX.outln(SERVERSTART);
      if(interactive) {
        input = new InputListener();
        input.start();
      }
      session = new SessionListener(this);
      session.start();
    } catch(final Exception ex) {
      error(ex, true);
    }
  }

  /**
   * Stops the server socket listener.
   * @throws IOException I/O exception
   */
  public void stop() throws IOException {
    running = false;
    if(interactive) {
      input.thread.interrupt();
      input = null;
    }
    for(final Session s : sessions) s.close();

    try {
      // dummy socket for breaking the accept block
      new Socket(context.prop.get(Prop.HOST), context.prop.num(Prop.PORT));
    } catch(final IOException ex) {
      error(ex, false);
    }
  }

  /**
   * Quits the server.
   */
  public void quit() {
    try {
      new ClientLauncher(context).execute(new IntStop());
      BaseX.outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      error(ex, true);
    }
  }

  /**
   * Closes everything up.
   */
  public void close() {
    try {
      socket.close();
    } catch(final IOException ex) {
      error(ex, false);
    }
  }

  /**
   * Parses the command-line arguments.
   * @param args command-line arguments
   * @return true if all arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    final Args arg = new Args(args);
    boolean ok = true;
    while(arg.more() && ok) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'd') {
          // activate debug mode
          ok = set(Prop.DEBUG, true);
        } else if(c == 'i') {
          // activate interactive mode
          interactive = true;
        } else if(c == 'p') {
          // parse server port
          ok = set(Prop.PORT, arg.string());
        } else if(c == 's') {
          // parse server name
          ok = set(Prop.HOST, arg.string());
        } else if(c == 'v') {
          // show process info
          verbose = true;
        } else {
          ok = false;
        }
      } else {
        ok = false;
        if(arg.string().equals("stop")) {
          quit();
          return false;
        }
      }
    }
    if(!ok) BaseX.outln(SERVERINFO);
    return ok;
  }

  /**
   * Sets the specified option.
   * @param opt option to be set
   * @param arg argument
   * @return success flag
   */
  private boolean set(final Object[] opt, final Object arg) {
    return new Set(opt, arg).execute(context);
  }

  /**
   * Listens to the console input.
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  class InputListener implements Runnable {
    /** Thread. */
    Thread thread = null;

    /**
     * Starts the thread.
     */
    public void start() {
      thread = new Thread(this);
      thread.start();
    }

    public void run() {
      BaseX.outln();
      while(running) {
        // get user input
        try {
          BaseX.out("> ");
          final InputStreamReader isr = new InputStreamReader(System.in);
          final String com = new BufferedReader(isr).readLine().trim();
          if(com.equals("stop") || com.equals("exit")) {
            stop();
          } else if(com.equals("list")) {
            final int size = sessions.size();
            BaseX.outln(size + " Session(s):");
            for(int i = 0; i < size; i++) {
              final Session s = sessions.get(i);
              final Data data = s.context.data();
              BaseX.outln("- " + s +
                  (data != null ? ": " + data.meta.name : ""));
            }
          } else if(com.equals("help")) {
            BaseX.outln("-list     Lists all server sessions"
                + NL + "-stop     Stops the server");
          } else if(com.length() > 0) {
            BaseX.outln("No such command");
          }
        } catch(final Exception ex) {
          // also catches interruptions such as ctrl+c, etc.
          BaseX.outln();
        }
      }
    }
  }

  /**
   * Listens to new client-server sessions.
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Andreas Weiler
   */
  class SessionListener implements Runnable {
    /** Thread. */
    Thread thread = null;
    /** Server reference. */
    BaseXServer bx;

    /**
     * Constructor.
     * @param b server reference
     */
    public SessionListener(final BaseXServer b) {
      bx = b;
    }

    /**
     * Starts the thread.
     */
    public void start() {
      thread = new Thread(this);
      thread.start();
    }

    public void run() {
      while(running) {
        try {
          final Socket s = socket.accept();
          if(!running) {
            close();
          } else {
            sessions.add(new Session(s, verbose, bx));
          }
        } catch(final IOException ex) {
          error(ex, false);
        }
      }
    }
  }

  /**
   * Prints a server error message.
   * @param ex exception reference
   * @param quiet quiet flag
   */
  public static void error(final Exception ex, final boolean quiet) {
    if(quiet) {
      BaseX.debug(ex);
      if(ex instanceof BindException) {
        BaseX.errln(SERVERBIND);
      } else if(ex instanceof IOException) {
        BaseX.errln(SERVERERR);
      } else {
        BaseX.errln(ex.getMessage());
      }
    } else {
      ex.printStackTrace();
    }
  }
}
