package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.basex.core.Session;
import org.basex.core.LocalSession;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.IntStop;
import org.basex.server.ClientSession;
import org.basex.server.ServerSession;
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
  boolean running = true;
  /** Verbose mode. */
  boolean info;

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
    if(!ok) return;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // interrupt running processes
        for(final ServerSession s : context.sessions) s.stopProcess();
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
              if(running) {
                context.add(new ServerSession(s, BaseXServer.this, info));
              }
            } catch(final IOException ex) {
              error(ex, false);
            }
          }
        } catch(final Exception ex) {
          error(ex, true);
        }
      }
    }.start();

    outln(CONSOLE, SERVERMODE, console ? CONSOLE2 : SERVERSTART);

    if(!console) return;
    quit(console());
  }

  @Override
  public void quit(final boolean user) {
    running = false;
    console = false;
    for(final ServerSession s : context.sessions) s.exit();
    super.quit(user);

    try {
      // dummy session for breaking the accept block
      new ClientSession(context);
    } catch(final IOException ex) {
      error(ex, false);
    }
  }

  /**
   * Stops the server.
   */
  public void stop() {
    try {
      new ClientSession(context).execute(new IntStop());
      outln(SERVERSTOPPED);
    } catch(final IOException ex) {
      error(ex, true);
    }
  }

  @Override
  protected Session session() {
    if(session == null) session = new LocalSession(context);
    return session;
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
          context.prop.set(Prop.DEBUG, true);
        } else if(c == 'i') {
          // activate interactive mode
          console = true;
        } else if(c == 'n') {
          // parse server name
          context.prop.set(Prop.HOST, arg.string());
        } else if(c == 'p') {
          // parse server port
          context.prop.set(Prop.PORT, arg.num());
        } else if(c == 'v') {
          // show process info
          info = true;
        } else {
          ok = false;
        }
      } else {
        ok = false;
        if(arg.string().equals("stop")) {
          stop();
          return;
        }
      }
    }
    if(!ok) outln(SERVERINFO);
  }
}
