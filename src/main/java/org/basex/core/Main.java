package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This is the abstract main class for the starter classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Main {
  /** Database context. */
  public final Context context;

  /** Output file for queries. */
  protected OutputStream out = System.out;
  /** Console mode. May be set to {@code false} during execution. */
  protected boolean console;
  /** Session. */
  protected Session session;
  /** Verbose mode. */
  protected boolean verbose;
  /** Trailing newline. */
  protected boolean newline;

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  protected Main(final String[] args) throws IOException {
    this(args, null);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @param ctx database context, or {@code null}
   * @throws IOException I/O exception
   */
  protected Main(final String[] args, final Context ctx) throws IOException {
    context = ctx != null ? ctx : new Context();
    parseArguments(args);

    // console: turn on verbose mode
    verbose |= console;

    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public synchronized void run() {
        context.close();
      }
    });
  }

  /**
   * Launches the console mode, which reads and executes user input.
   */
  protected final void console() {
    // create console reader
    final Scanner cr = new Scanner(System.in);
    // loop until console is set to false (may happen in server mode)
    while(console) {
      Util.out("> ");
      // end of input: break loop
      if(!cr.hasNextLine()) break;
      // get next line
      final String in = cr.nextLine();
      // skip empty lines
      if(in.isEmpty()) continue;
      try {
        // show goodbye message if method returns false
        if(!execute(in)) {
          Util.outln(BYE[new Random().nextInt(4)]);
          return;
        }
      } catch(final IOException ex) {
        // output error messages
        Util.errln(ex);
      }
    }
  }

  /**
   * Quits the console mode.
   * @throws IOException I/O exception
   */
  protected void quit() throws IOException {
    execute(new Exit(), true);
    if(out == System.out || out == System.err) out.flush();
    else out.close();
  }

  /**
   * Parses and executes the input string.
   * @param in input commands
   * @return {@code false} if the exit command was sent
   * @throws IOException database exception
   */
  protected final boolean execute(final String in) throws IOException {
    final PasswordReader pr = new PasswordReader() {
      @Override
      public String password() {
        Util.out(PASSWORD + COLS);
        return md5(Util.password());
      }
    };
    final CommandParser cp = new CommandParser(in, context).password(pr);

    try {
      for(final Command cmd : cp.parse()) {
        if(cmd instanceof Exit) return false;
        execute(cmd, verbose);
      }
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw new BaseXException(ex);
    }
    return true;
  }

  /**
   * Executes the specified command and optionally prints some information.
   * @param cmd command to be run
   * @param info verbose flag
   * @throws IOException I/O exception
   */
  protected final void execute(final Command cmd, final boolean info) throws IOException {
    final Session ss = session();
    ss.execute(cmd);
    if(newline && cmd instanceof XQuery) out.write(token(NL));
    if(info) Util.out(ss.info());
  }

  /**
   * Returns the session.
   * @return session
   * @throws IOException I/O exception
   */
  protected abstract Session session() throws IOException;

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  protected abstract void parseArguments(final String[] args)
      throws IOException;
}
