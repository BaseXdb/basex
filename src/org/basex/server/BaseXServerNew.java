package org.basex.server;

import static org.basex.Text.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.basex.BaseX;
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
  boolean verbose = false;
  /** Last Id from a client. */
  int lastid = 0;
  /** ServerSocket. */
  ServerSocket serverSocket;

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

    try {
      serverSocket = new ServerSocket(Prop.port);
      BaseX.outln(SERVERSTART);
      InputListener inputListener = new InputListener();
      inputListener.start();
      while(running) {
        Socket s = serverSocket.accept();
        lastid++;
        BaseX.outln("Login from Client " + lastid);
        new Session(s, lastid, verbose).start();
      }
      // close the serverSocket when Server is stopped.
      //serverSocket.close();
      // exits the BaseXServer
      //new Exit().execute(null);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      if(ex instanceof BindException) {
        BaseX.errln(SERVERBIND);
      } else if(ex instanceof IOException) {
        if(ex instanceof SocketException) {
          BaseX.outln("Server stopped.");
          // exits the BaseXServer
          new Exit().execute(null);
        } else {
        BaseX.errln(SERVERERR);
        }
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
      }
      if(!ok) break;
    }
    if(!ok) BaseX.errln(SERVERINFO);
    return ok;
  }
  
  /**
   * InputListener.
   * @author Andy
   *
   */
  class InputListener implements Runnable {
    
    /** Thread. */
    Thread thread = null;
    
    /**
     * Starts the thread.
     */
    public synchronized void start() {
      if (thread == null) {
        thread = new Thread(this);
        thread.start();
      }
    }

    public void run() {
      while(true) {
        // get user input
        try {
          final InputStreamReader isr = new InputStreamReader(System.in);
          String temp = new BufferedReader(isr).readLine().trim();
          if(temp.equals("stop")) {
            thread = null;
            // close the serverSocket when Server is stopped.
            serverSocket.close();
          }
        } catch(final Exception ex) {
          // also catches interruptions such as ctrl+c, etc.
          BaseX.outln();
        }
      }
    }
  }
}
