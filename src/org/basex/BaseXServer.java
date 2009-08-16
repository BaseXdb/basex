package org.basex;

import static org.basex.Text.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.basex.core.ClientLauncher;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.IntInfo;
import org.basex.core.proc.IntOutput;
import org.basex.core.proc.IntStop;
import org.basex.core.proc.Set;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Args;
import org.basex.util.Performance;

/**
 * This is the starter class for the database server.
 * It handles incoming requests and offers some simple threading to
 * allow simultaneous database requests.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXServer {
  /** Database Context. */
  final Context context = new Context();
  /** Flag for server activity. */
  boolean running = true;
  /** Verbose mode. */
  boolean verbose;

  /** Current client connections. */
  final ArrayList<BaseXSession> sess = new ArrayList<BaseXSession>();

  /**
   * Main method, launching the server process.
   * Command-line arguments can be listed with the <code>-h</code> argument.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXServer(args);
  }

  /**
   * The server calls this constructor to listen on the given port for incoming
   * connections. Protocol version handshake is performed when the connection
   * is established. This constructor blocks until a client connects.
   * @param args arguments
   */
  public BaseXServer(final String... args) {
    if(!parseArguments(args)) return;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // stop running processes
        for(final BaseXSession s : sess) s.core.stop();
        context.close();
      }
    });

    // this thread cleans the process stack
    new Thread() {
      @Override
      public void run() {
        while(running) {
          Performance.sleep(1000);
          clean();
        }
      }
    }.start();

    try {
      final ServerSocket server = new ServerSocket(context.prop.num(Prop.PORT));
      BaseX.outln(SERVERSTART);
      while(running) serve(server);
    } catch(final Exception ex) {
      error(ex, true);
    }
  }

  /**
   * Waits for a network request and evaluates the input.
   * @param server server reference
   */
  private void serve(final ServerSocket server) {
    try {
      // get socket and ip address
      final Socket s = server.accept();
      final Performance perf = new Performance();
      // get command and arguments
      final DataInputStream dis = new DataInputStream(s.getInputStream());
      final String in = dis.readUTF().trim();

      final InetAddress addr = s.getInetAddress();
      final String ha = addr.getHostAddress();
      final int sp = s.getPort();
      if(verbose) BaseX.outln("[%:%] %", ha, sp, in);

      Process pr = null;
      try {
        pr = new CommandParser(in, context, true).parse()[0];
      } catch(final QueryException ex) {
        pr = new Process(0) { };
        pr.error(ex.extended());
        add(new BaseXSession(sp, System.nanoTime(), pr));
        send(s, -sp);
        return;
      }

      if(pr instanceof IntStop) {
        send(s, 1);
        running = false;
        return;
      }

      // start session thread
      final Process proc = pr;
      new Thread() {
        @Override
        public void run() {
          try {
            if(proc instanceof IntOutput || proc instanceof IntInfo) {
              final OutputStream os = s.getOutputStream();
              final PrintOutput out = new PrintOutput(new BufferedOutput(os),
                  Prop.web ? context.prop.num(Prop.MAXTEXT) :
                    Integer.MAX_VALUE);
              final int id = Math.abs(Integer.parseInt(proc.args().trim()));
              final Process c = get(id);
              if(c == null) {
                out.print(BaseX.info(SERVERTIME,
                    context.prop.num(Prop.TIMEOUT)));
              } else if(proc instanceof IntOutput) {
                // the client requests result of the last process
                c.output(out);
              } else if(proc instanceof IntInfo) {
                // the client requests information about the last process
                c.info(out);
                // remove session after info has been requested
                remove(c);
              }
              out.close();
            } else {
              // process a normal request
              add(new BaseXSession(sp, System.nanoTime(), proc));
              // execute command and return process id (negative: error)
              send(s, proc.execute(context) ? sp : -sp);
              if(proc.info().equals(PROGERR)) proc.error(SERVERTIME);

            }
            dis.close();
          } catch(final Exception ex) {
            error(ex, false);
          }
          if(verbose) BaseX.outln("[%:%] %", ha, sp, perf.getTimer());
        }
      }.start();
    } catch(final Exception ex) {
      error(ex, false);
    }
  }

  /**
   * Caches a user connection and removes out-of-dated entries.
   * @param bs session to be added
   */
  synchronized void add(final BaseXSession bs) {
    clean();
    sess.add(bs);
  }

  /**
   * Removes obsolete or too slow processes.
   */
  synchronized void clean() {
    final long t = System.nanoTime();
    for(int i = 0; i < sess.size(); i++) {
      if(t - sess.get(i).time > context.prop.num(Prop.TIMEOUT) * 1000000000L) {
        final BaseXSession s = sess.remove(sess.size() - 1);
        if(i != sess.size()) sess.set(i--, s);
        s.stop();
      }
    }
  }

  /**
   * Removes the session with the specified process.
   * @param p process to be removed
   */
  synchronized void remove(final Process p) {
    for(int i = 0; i < sess.size(); i++) {
      if(sess.get(i).core == p) {
        sess.remove(i);
        break;
      }
    }
  }

  /**
   * Returns an answer to the client.
   * @param s socket reference
   * @param id session id to be returned
   * @throws IOException I/O exception
   */
  synchronized void send(final Socket s, final int id) throws IOException {
    final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    dos.writeInt(id);
    dos.close();
  }

  /**
   * Returns the correct client session.
   * @param id process id
   * @return core reference
   */
  synchronized Process get(final int id) {
    for(final BaseXSession s : sess) if(s != null && s.pid == id) return s.core;
    return null;
  }

  /**
   * Parses the command-line arguments.
   * @param args the command-line arguments
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
   * Quits the server.
   */
  public void quit() {
    try {
      new ClientLauncher(context).execute(new IntStop());
      BaseX.outln(SERVERSTOPPED);
    } catch(final Exception ex) {
      error(ex, true);
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

  /** Simple session class. */
  class BaseXSession {
    /** Process id. */
    int pid;
    /** Timer. */
    long time;
    /** Process. */
    Process core;

    /**
     * Constructor.
     * @param i process id
     * @param t timer
     * @param c process
     */
    BaseXSession(final int i, final long t, final Process c) {
      pid = i;
      time = t;
      core = c;
    }

    /**
     * Stops a process.
     */
    void stop() {
      core.stop();
    }
  }
}
