package org.basex.core;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;
import org.basex.core.Commands.Cmd;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Password;
import org.basex.core.cmd.Set;
import org.basex.query.QueryException;
import org.basex.server.Session;
import org.basex.util.StringList;
import org.basex.util.Util;

/**
 * This is the abstract main class for all starter classes.
 * Moreover, it offers some utility methods which are used
 * throughout the project.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Main {
  /** Flag for using default standard input. */
  private static final boolean NOCONSOLE = System.console() == null;
  /** Database context. */
  public final Context context = new Context();
  /** Successful command line parsing. */
  protected final boolean success;
  /** Output file for queries. */
  protected OutputStream out = System.out;

  /** Console mode. */
  protected boolean console;
  /** Session. */
  protected Session session;
  /** Verbose mode. */
  protected boolean verbose;

  /**
   * Constructor.
   * @param args command-line arguments
   */
  protected Main(final String... args) {
    success = parseArguments(args);
    verbose |= console;
    if(!success) return;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        context.close();
      }
    });
  }

  /**
   * Launches the console mode, which reads and executes user input.
   * @return true if exit command was sent
   * @throws IOException I/O exception
   */
  protected final boolean console() throws IOException {
    while(console) {
      Util.out("> ");
      for(final String in : inputs()) {
        if(in.length() != 0 && !execute(in)) return true;
      }
    }
    return false;
  }

  /**
   * Quits the console mode.
   * @param user quit by user
   */
  protected void quit(final boolean user) {
    try {
      if(user) Util.outln(CLIENTBYE[new Random().nextInt(4)]);
      execute(new Exit(), true);
      out.flush();
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
    }
  }

  /**
   * Parses and executes the input string.
   * @param in input commands
   * @return false if exit command was sent
   * @throws IOException database exception
   */
  protected final boolean execute(final String in) throws IOException {
    try {
      for(final Command cmd : new CommandParser(in, context).parse()) {
        if(cmd instanceof Exit) return false;

        // offer optional password input
        final int i = cmd instanceof Password && cmd.args[0] == null ? 0 :
          (cmd instanceof CreateUser || cmd instanceof AlterUser) &&
          cmd.args[1] == null ? 1 : -1;
        if(i != -1) {
          Util.out(SERVERPW + COLS);
          cmd.args[i] = password();
        }
        if(!execute(cmd, verbose)) break;
      }
    } catch(final QueryException ex) {
      error(ex, ex.getMessage());
    }
    return true;
  }

  /**
   * Executes the specified command and optionally prints some information.
   * @param cmd command to be run
   * @param info verbose flag
   * @return true if operation was successful
   * @throws IOException I/O exception
   */
  protected final boolean execute(final Command cmd, final boolean info)
      throws IOException {

    final Session ss = session();
    if(ss == null) return false;
    try {
      ss.execute(cmd);
      if(info) Util.out(ss.info());
      if(cmd instanceof Exit) return true;
      return true;
    } catch(final BaseXException ex) {
      error(null, ex.getMessage());
      return false;
    }
  }

  /**
   * Sets the specified option.
   * @param opt option to be set
   * @param arg argument
   * @return success flag
   * @throws IOException database exception
   */
  protected final boolean set(final Object[] opt, final Object arg)
      throws IOException {
    return execute(new Set(opt, arg), false);
  }

  /**
   * Prints an error message.
   * @param ex exception reference
   * @param msg message
   */
  protected final void error(final Exception ex, final String msg) {
    Util.errln((console ? "" : INFOERROR) + msg.trim());
    Util.debug(ex);
  }

  /**
   * Returns multiple lines from standard input.
   * @return list of strings
   */
  protected final StringList inputs() {
    final StringList sl = new StringList();
    // find end of input from interactive user input
    final Scanner scan = new Scanner(System.in).useDelimiter("\\z");
    if(scan.hasNext()) {
      // catch several lines sent from redirected standard input
      final Scanner lines = new Scanner(scan.next());
      while(lines.hasNextLine()) sl.add(lines.nextLine());
    }
    // no more input: send exit command
    if(sl.size() == 0) sl.add(Cmd.EXIT.toString());
    return sl;
  }

  /**
   * Returns a single line from standard input.
   * @return string
   */
  protected final String input() {
    final Scanner sc = new Scanner(System.in);
    return sc.hasNextLine() ? sc.nextLine().trim() : "";
  }

  /**
   * Returns a password from standard input.
   * @return password
   */
  protected final String password() {
    // use standard input if no console if defined (such as in Eclipse)
    if(NOCONSOLE) return input();
    // hide password
    final char[] pw = System.console().readPassword();
    return pw != null ? new String(pw) : "";
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
   * @return success flag
   */
  protected abstract boolean parseArguments(final String[] args);
}
