package org.basex;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.ClientProcess;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.GetInfo;
import org.basex.core.proc.GetResult;
import org.basex.core.proc.Stop;
import org.basex.io.BufferedOutput;
import org.basex.io.ConsoleOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;
import static org.basex.Text.*;

/**
 * This is the starter class for the database server.
 * It handles incoming requests and offers some simple threading to
 * allow simultaneous database requests.
 * Add the '-h' option to get a list on all available command-line arguments.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Stefan Klinger
 */
public final class BaseXServer {
  /** Maximum number of simultaneous connections. */
  private static final int MAX = 10;
  /** Time out in nanoseconds. */
  private static final long TIMEOUT = 180 * 1000000000;

  /** Database Context. */
  final Context context = new Context();
  /** Flag for server activity. */
  boolean running = true;
  /** Verbose mode. */
  boolean verbose;
  
  /** Client ip addresses. */
  private final String[] client = new String[MAX];
  /** Core reference. */
  private final long[] time = new long[MAX];
  /** Core reference. */
  private final Process[] corc = new Process[MAX];
  /* Core reference.
  private final Proc[] core = new Proc[MAX]; */
  
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
      Prop.read();
      if(!parseArguments(args)) return;

      final ServerSocket server = new ServerSocket(Prop.port);
      BaseX.outln(SERVERSTART);
      while(running) serve(server);
      BaseX.outln(SERVERSTOPPED);

      Prop.write();
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
      final String ip = addr.toString();
      if(verbose) BaseX.outln("[%:%] %", addr.getHostAddress(),
          s.getPort(), in);

      Process pr = null;
      try {
        pr = new CommandParser(in).parse()[0];
      } catch(final QueryException ex) {
        BaseX.errln(ex.getMessage());
        final OutputStream out = s.getOutputStream();
        out.write(0);
        out.close();
        return;
      }
      final Process proc = pr;
      
      if(proc instanceof Stop) {
        s.getOutputStream().write(1);
        // interrupt running processes
        for(int p = 0; p < MAX; p++) if(corc[p] != null) corc[p].stop();
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
              final Process c = session(ip);
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
              boolean ok = save(proc, ip);
              if(ok) {
                ok = proc.execute(context);
                if(!ok && verbose) {
                  final PrintOutput o = new ConsoleOutput(System.err);
                  proc.info(o);
                  o.close();
                }
              }
              // answer with ok flag (1: everything alright, 0: error)
              final OutputStream out = s.getOutputStream();
              out.write(ok ? 1 : 0);
              out.close();
            }
            dis.close();
          } catch(final Exception ex) {
            if(ex instanceof IOException) BaseX.errln(SERVERERR);
            else ex.printStackTrace();
          }
          if(verbose) BaseX.outln("[%:%] %", addr.getHostAddress(),
              s.getPort(), perf.getTimer());
        }
      }.start();
    } catch(final Exception ex) {
      if(ex instanceof IOException) BaseX.errln(SERVERERR);
      else ex.printStackTrace();
    }
  }

  /**
   * Saves a user process.
   * @param cmd command
   * @param ip client ip address
   * @return result of check
   */
  synchronized boolean save(final Process cmd, final String ip) {
    final long t = System.nanoTime();
    
    for(int i = 0; i < MAX; i++) {
      if(client[i] == null || client[i].equals(ip) || t - time[i] > TIMEOUT) {
        client[i] = ip;
        time[i] = System.nanoTime();
        corc[i] = cmd;
        return true;
      }
    }
    BaseX.errln(SERVERFULL);
    return false;
  }

  /**
   * Returns a new client session.
   * @param cmd command
   * @param ip client ip address
   * @return core reference
  synchronized Proc newCore(final Proc cmd, final String ip) {
    final long t = System.nanoTime();
    
    for(int i = 0; i < MAX; i++) {
      if(client[i] == null || client[i].equals(ip) || t - time[i] > TIMEOUT) {
        client[i] = ip;
        time[i] = System.nanoTime();
        core[i] = cmd;
        return cmd;
      }
    }
    BaseX.errln(SERVERFULL);
    return null;
  }
   */

  /**
   * Returns the correct client session.
   * @param ip client ip address
   * @return core reference
  synchronized Proc getSession(final String ip) {
    for(int i = 0; i < MAX; i++) {
      if(ip.equals(client[i])) return core[i];
    }
    return null;
  }
   */
  
  /**
   * Returns the correct client session.
   * @param ip client ip address
   * @return core reference
   */
  synchronized Process session(final String ip) {
    for(int i = 0; i < MAX; i++) {
      if(ip.equals(client[i])) return corc[i];
    }
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
          new ClientProcess("localhost", Prop.port, new Stop()).execute(null);
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
}
