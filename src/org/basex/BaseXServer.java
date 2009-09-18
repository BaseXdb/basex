package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.IntStop;
import org.basex.server.ClientLauncher;
import org.basex.server.Session;
import org.basex.util.Args;

/**
 * This is the starter class for the client/server architecture.
 * It handles incoming requests and allows simultaneous database requests.
 * Add the '-h' option to get a list on all available command-line
 * arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class BaseXServer extends Main {
  /** Flag for server activity. */
  public boolean running = true;
  /** Verbose mode. */
  public boolean verbose;

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
    super(true, true, args);
    if(!ok) return;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // interrupt running processes
        for(final Session s : context.sessions) s.core.stop();
        context.close();
      }
    });

    new Thread() {
      @Override
      public void run() {
        try {
          final ServerSocket ss = new ServerSocket(context.prop.num(Prop.PORT));
          while(running) {
            try {
              final Socket s = ss.accept();
              if(running) context.add(new Session(s, BaseXServer.this));
            } catch(final IOException ex) {
              Main.error(ex, false);
            }
          }
        } catch(final Exception ex) {
          Main.error(ex, true);
        }
      }
    }.start();

    outln(CONSOLE, SERVERMODE, console ? CONSOLE2 : SERVERSTART);
    if(console) quit(console());
  }

  @Override
  public void quit(final boolean user) {
    try {
      running = false;
      console = false;
      for(final Session s : context.sessions) s.close();

      // dummy launcher for breaking the accept block
      new ClientLauncher(context);
    } catch(final IOException ex) {
      Main.error(ex, false);
    }
  }

  /**
   * Quits the server.
   */
  public void quit() {
    try {
      new ClientLauncher(context).execute(new IntStop());
      Main.outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      Main.error(ex, true);
    }
  }

  @Override
  protected void parseArguments(final String[] args) {
    final Args arg = new Args(args);
    ok = true;
    while(arg.more() && ok) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'd') {
          // activate debug mode
          ok = set(Prop.DEBUG, true);
        } else if(c == 'i') {
          // activate interactive mode
          console = true;
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
          return;
        }
      }
    }
    if(!ok) Main.outln(SERVERINFO);
  }
}
