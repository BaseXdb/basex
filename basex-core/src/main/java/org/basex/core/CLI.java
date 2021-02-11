package org.basex.core;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This is the abstract main class for the starter classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class CLI extends Main {
  /** Database context. */
  public Context context;

  /** Cached initial commands. */
  protected final ArrayList<Pair<String, String>> commands = new ArrayList<>();
  /** Output file for queries. */
  protected OutputStream out = System.out;
  /** Verbose mode. */
  protected boolean verbose;

  /** Password reader. */
  private static final PasswordReader PWREADER = () -> {
    Util.out(PASSWORD + COLS);
    return Util.password();
  };
  /** Session. */
  private Session session;

  /**
   * Constructor, assigning the specified context.
   * @param args command-line arguments
   * @param ctx database context (if {@code null}, must be assigned later on)
   * @throws IOException I/O exception
   */
  protected CLI(final Context ctx, final String... args) throws IOException {
    super(args);
    context = ctx;
    parseArgs();
    // guarantee correct shutdown of database context
    if(context != null) Runtime.getRuntime().addShutdownHook(new Thread(context::close));
  }

  /**
   * Parses and executes the input string.
   * @param command base uri (name) and command string (value)
   * @return {@code false} if the exit command was sent
   * @throws IOException database exception
   */
  protected final boolean execute(final Pair<String, String> command) throws IOException {
    final CommandParser cp = CommandParser.get(command.value(), context);
    return execute(cp.baseURI(command.name()).pwReader(PWREADER));
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
  private Session init() throws IOException {
    if(local()) return new LocalSession(context, out);

    // user/password input
    String user = context.soptions.get(StaticOptions.USER);
    String pass = context.soptions.get(StaticOptions.PASSWORD);
    while(user.isEmpty()) {
      Util.out(USERNAME + COLS);
      user = Util.input();
    }
    while(pass.isEmpty()) {
      Util.out(PASSWORD + COLS);
      pass = Util.password();
    }

    final String host = context.soptions.get(StaticOptions.HOST);
    final int port = context.soptions.get(StaticOptions.PORT);
    try {
      return new ClientSession(host, port, user, pass, out);
    } catch(final ConnectException ex) {
      Util.debug(ex);
      throw new BaseXException(CONNECTION_ERROR_X, port);
    }
  }

  /**
   * Returns the base URI and the query string for the specified input.
   * @param input input
   * @return return base URI and query string
   * @throws IOException I/O exception
   */
  protected static Pair<String, String> input(final String input) throws IOException {
    final IO io = IO.get(input);
    final boolean file = !(io instanceof IOContent) && io.exists() && !io.isDir();
    return new Pair<>(file ? io.path() : "./", file ? io.string() : input);
  }

  /**
   * Indicates if this is a local client.
   * @return local mode
   */
  protected boolean local() {
    return true;
  }
}
