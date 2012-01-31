package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;

import org.basex.core.Commands.Cmd;
import org.basex.core.cmd.Exit;
import org.basex.query.QueryException;
import org.basex.server.Session;
import org.basex.util.Util;
import org.basex.util.list.StringList;

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
    while(console) {
      Util.out("> ");
      for(final String in : inputs()) {
        if(in.isEmpty()) continue;
        try {
          if(!execute(in)) {
            Util.outln(BYE[new Random().nextInt(4)]);
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
      public String password() throws QueryException {
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
