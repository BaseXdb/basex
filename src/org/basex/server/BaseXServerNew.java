package org.basex.server;

import static org.basex.Text.*;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.basex.BaseX;
import org.basex.core.ClientProcess;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.util.Token;


/**
 * This is the starter class for the database server.
 * It handles incoming requests and offers some simple threading to
 * allow simultaneous database requests.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class BaseXServerNew {
  
  /** Flag for server activity. */
  boolean running = true;
  /** Verbose mode. */
  boolean verbose;
  /** Counter for clientid. */
  int counter = 0;

  /**
   * Main method, launching the server process.
   * Command-line arguments can be listed with the <code>-h</code> argument.
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    new BaseXServerNew(args);
  }

  /**
   * The server calls this constructor to listen on the given port for incoming
   * connections. Protocol version handshake is performed when the connection
   * is established. This constructor blocks until a client connects.
   * @param args arguments
   */
  public BaseXServerNew(final String... args) {
    Prop.server = true;

    if(!parseArguments(args)) return;

    // this thread cleans the process stack
    /*new Thread() {
      @Override
      public void run() {
        while(running) {
          Performance.sleep(2500L);
          clean();
        }
      }
    }.start();*/

    try {
      final ServerSocket server = new ServerSocket(Prop.port);
      BaseX.outln(SERVERSTART);
      while(running) {
        Socket s = server.accept();
        counter++;
        new Session(s, counter).start();
      }
      server.close();
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

}
