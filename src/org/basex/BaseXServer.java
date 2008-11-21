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
import org.basex.core.ClientProcess;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.core.proc.GetInfo;
import org.basex.core.proc.GetResult;
import org.basex.io.BufferedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This is the starter class for the database server.
 * It handles incoming requests and offers some simple threading to
 * allow simultaneous database requests.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXServer {
  /** Session time out in seconds. */
  private static final long TIMEOUT = 5;

  /** Database Context. */
  final Context context = new Context();
  /** Flag for server activity. */
  boolean running = true;
  /** Verbose mode. */
  boolean verbose;

  /** Current client connections. */
  private final ArrayList<BaseXSession> sess = new ArrayList<BaseXSession>();

  /**
   * Main method, launching the server process.
   * Command-line arguments can be listed with the <code>-h</code> argument.
   * @param args command line arguments
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
  private BaseXServer(final String[] args) {
    try {
      if(!parseArguments(args)) return;

      final ServerSocket server = new ServerSocket(Prop.port);
      BaseX.outln(SERVERSTART);
      while(running) serve(server);
      BaseX.outln(SERVERSTOPPED);

      context.close();
    } catch(final Exception ex) {
      BaseX.debug(ex);
      if(ex instanceof BindException) {
        BaseX.errln(SERVERBIND);
      } else if(ex instanceof IOException) {
        BaseX.errln(SERVERERR);
      } else {
        BaseX.errln(ex.getMessage());
      }
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
        pr = new CommandParser(in).parse()[0];
      } catch(final QueryException ex) {
        BaseX.errln(ex.getMessage());
        send(s, 0);
        return;
      }
      final Process proc = pr;

      if(proc instanceof Exit) {
        send(s, 1);
        // interrupt running processes
        for(final BaseXSession ss : sess) ss.core.stop();
        running = false;
        return;
      }

      // start session thread
      new Thread() {
        @Override
        public void run() {
          try {
            if(proc instanceof GetResult || proc instanceof GetInfo) {
              final OutputStream os = s.getOutputStream();
              final PrintOutput out = new PrintOutput(new BufferedOutput(os));
              final int id = Math.abs(Integer.parseInt(proc.args().trim()));
              final Process c = get(id);
              if(c == null) {
                out.print(SERVERFULL);
              } else if(proc instanceof GetResult) {
                // the client requests result of the last process
                c.output(out);
              } else if(proc instanceof GetInfo) {
                // the client requests information about the last process
                c.info(out);
              }
              out.close();
            } else {
              // process a normal request
              add(new BaseXSession(sp, System.nanoTime(), proc));
              // execute command and return process id (negative: error)
              send(s, proc.execute(context) ? sp : -sp);
            }
            dis.close();
          } catch(final Exception ex) {
            if(ex instanceof IOException) BaseX.errln(SERVERERR);
            ex.printStackTrace();
          }
          if(verbose) BaseX.outln("[%:%] %", ha, sp, perf.getTimer());
        }
      }.start();
    } catch(final Exception ex) {
      if(ex instanceof IOException) BaseX.errln(SERVERERR);
      ex.printStackTrace();
    }
  }

  /**
   * Caches a user connection and removes out-of-dated entries.
   * @param bs session to be added
   */
  synchronized void add(final BaseXSession bs) {
    final long t = System.nanoTime();
    for(int i = 0; i < sess.size(); i++) {
      if(t - sess.get(i).time > TIMEOUT * 1000000000L) {
        final BaseXSession s = sess.remove(sess.size() - 1);
        if(i != sess.size()) sess.set(i--, s);
      }
    }
    sess.add(bs);
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
   * Parses the command line arguments.
   * @param args the command line arguments
   * @return true if all arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    boolean ok = true;

    // loop through all arguments
    for(int a = 0; a < args.length; a++) {
      ok = false;
      if(args[a].startsWith("-")) {
        for(int i = 1; i < args[a].length(); i++) {
          final char c = args[a].charAt(i);
          if(c == 'p') {
            // parse server port
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            final int p = Token.toInt(args[a].substring(i));
            if(p <= 0) {
              BaseX.errln(SERVERPORT + args[a].substring(i));
              break;
            }
            Prop.port = p;
            i = args[a].length();
            ok = true;
          } else if(c == 'd') {
            Prop.debug = true;
            ok = true;
          } else if(c == 'v') {
            verbose = true;
            ok = true;
          } else {
            break;
          }
        }
      } else if(args[a].equals("stop")) {
        try {
          // run new process, sending the stop command
          new ClientProcess("localhost", Prop.port, new Exit()).execute(null);
          BaseX.outln(SERVERSTOPPED);
        } catch(final Exception ex) {
          if(ex instanceof IOException) BaseX.errln(SERVERERR);
          else BaseX.errln(ex.getMessage());
          BaseX.debug(ex);
        }
        return false;
      }
      if(!ok) break;
    }
    if(!ok) BaseX.errln(SERVERINFO);
    return ok;
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
  }
}
