package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;
import org.basex.core.Commands.Cmd;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Password;
import org.basex.query.QueryException;
import org.basex.server.Session;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * This is the abstract main class for all starter classes.
 * Moreover, it offers some utility methods which are used
 * throughout the project.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Main {
  /** Flag for using default standard input. */
  private static final boolean NOCONSOLE = System.console() == null;
  /** Database context. */
  public final Context context = new Context();

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
   * @throws IOException I/O exception
   */
  protected Main(final String[] args) throws IOException {
    parseArguments(args);

    // console: turn on verbose mode
    verbose |= console;

    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        context.close();
      }
    });
  }

  /**
   * Launches the console mode, which reads and executes user input.
   */
  protected final void console() {
    while(console) {
      Util.out("> ");
      for(final String in : inputs()) {
        if(in.isEmpty()) continue;
        try {
          if(!execute(in)) {
            Util.outln(CLIENTBYE[new Random().nextInt(4)]);
            return;
          }
        } catch(final IOException ex) {
          Util.errln(ex);
        }
      }
    }
  }

  /**
   * Quits the console mode.
   * @throws IOException I/O exception
   */
  protected void quit() throws IOException {
    execute(new Exit(), true);
    out.flush();
  }

  /**
   * Parses and executes the input string.
   * @param in input commands
   * @return {@code false} if the exit command was sent
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
          cmd.args[i] = md5(password());
        }
        execute(cmd, verbose);
      }
    } catch(final QueryException ex) {
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
  protected final void execute(final Command cmd, final boolean info)
      throws IOException {

    final Session ss = session();
    ss.execute(cmd);
    if(info) Util.out(ss.info());
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
   * @throws IOException I/O exception
   */
  protected abstract void parseArguments(final String[] args)
      throws IOException;
}
