package org.basex.core;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This is the abstract main class for the starter classes.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class CLI extends Main {
  /** Database context. */
  public final Context context;

  /** Output file for queries. */
  protected OutputStream out = System.out;
  /** Verbose mode. */
  protected boolean verbose;

  /** Password reader. */
  private static final PasswordReader PWREADER = new PasswordReader() {
    @Override
    public String password() {
      Util.out(PASSWORD + COLS);
      return Util.password();
    }
  };
  /** Session. */
  private Session session;

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  protected CLI(final String[] args) throws IOException {
    this(args, null);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @param ctx database context or {@code null}
   * @throws IOException I/O exception
   */
  protected CLI(final String[] args, final Context ctx) throws IOException {
    super(args);
    context = ctx != null ? ctx : new Context();
    parseArgs();

    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public synchronized void run() {
        context.close();
      }
    });
  }

  /**
   * Parses and executes the input string.
   * @param in input commands
   * @throws IOException database exception
   */
  protected final void execute(final String in) throws IOException {
    execute(new CommandParser(in, context).pwReader(PWREADER));
  }

  /**
   * Execute the commands from the given command parser.
   * @param parser command parser
   * @return {@code false} if the exit command was sent
   * @throws IOException database exception
   */
  protected final boolean execute(final CommandParser parser) throws IOException {
    try {
      for(final Command cmd : parser.parse()) {
        if(cmd instanceof Exit) return false;
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
  protected final void execute(final Command cmd, final boolean info) throws IOException {
    final Session ss = session();
    ss.execute(cmd);
    if(info) Util.out(ss.info());
  }

  /**
   * Returns the session.
   * @return session
   * @throws IOException I/O exception
   */
  protected final Session session() throws IOException {
    if(session == null) session = init();
    session.setOutputStream(out);
    return session;
  }

  /**
   * Initializes and returns a session.
   * @return session instance
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected Session init() throws IOException {
    return new LocalSession(context, out);
  }
}
