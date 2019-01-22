package org.basex.core;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class Sandbox {
  /** Base uri. */
  private static final String BASEURI = new File(".").getAbsolutePath();

  /** Database port. */
  protected static final int DB_PORT = 9996;
  /** HTTP stop port. */
  protected static final int STOP_PORT = 9999;
  /** HTTP port. */
  protected static final int HTTP_PORT = 9998;

  /** REST identifier. */
  protected static final String REST = "rest";
  /** Root path. */
  protected static final String HTTP_ROOT = "http://" + S_LOCALHOST + ':' + HTTP_PORT + '/';
  /** Root path. */
  protected static final String REST_ROOT = HTTP_ROOT + REST + '/';

  /** Default output stream. */
  protected static final PrintStream OUT = System.out;
  /** Default error stream. */
  protected static final PrintStream ERR = System.err;
  /** Test name. */
  protected static final String NAME = Util.className(Sandbox.class);
  /** Database context. */
  protected static Context context;

  /**
   * Executes a command and returns exceptions into assertion errors.
   * @param cmd command to be run
   * @return string result
   */
  protected static String execute(final Command cmd) {
    try {
      return cmd.execute(context);
    } catch(final BaseXException ex) {
      Util.stack(ex);
      throw new AssertionError(ex.getMessage(), ex);
    }
  }

  /**
   * Sets an option and returns exceptions into assertion errors.
   * @param option option to be set
   * @param value value to be assigned
   */
  protected static void set(final Option<?> option, final Object value) {
    execute(new Set(option.name(), value));
  }

  /**
   * Runs a query and returns exceptions into assertion errors.
   * @param query query to be evaluated
   * @return string result
   */
  protected static String query(final String query) {
    try {
      return eval(query);
    } catch(final JobException ex) {
      return "";
    } catch(final QueryException | IOException ex) {
      Util.stack(ex);
      final AssertionError err = new AssertionError("Query failed:\n" + query);
      err.initCause(ex);
      throw err;
    }
  }

  /**
   * Runs a query.
   * @param query query string
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected static String eval(final String query) throws QueryException, IOException {
    final ArrayOutput ao = new ArrayOutput();
    try(QueryProcessor qp = new QueryProcessor(query, BASEURI, context)) {
      // update flag will be set in parsing step
      qp.parse();
      qp.register(context);
      try(Serializer ser = qp.getSerializer(ao)) {
        qp.value().serialize(ser);
      } finally {
        qp.unregister(context);
      }
    }
    return ao.toString();
  }

  /**
   * Writes a test file.
   * @param file file
   * @param data data to write
   */
  protected static void write(final IOFile file, final String data) {
    try {
      file.write(data);
    } catch(final IOException ex) {
      Util.stack(ex);
      throw new AssertionError(ex.getMessage(), ex);
    }
  }

  /**
   * Creates the sandbox.
   */
  public static void initSandbox() {
    final IOFile sb = sandbox();
    //sb.delete();
    if(!sb.md()) throw Util.notExpected("Sandbox could not be created.");

    final String path = sb.path();
    Prop.put(StaticOptions.DBPATH, path + "/data");
    Prop.put(StaticOptions.WEBPATH, path + "/webapp");
    Prop.put(StaticOptions.RESTXQPATH, path + "/webapp");
    Prop.put(StaticOptions.REPOPATH, path + "/repo");
    Prop.put(StaticOptions.SERVERPORT, Integer.toString(DB_PORT));
    context = new Context();
  }

  /**
   * Removes test databases and closes the database context.
   */
  public static void finishSandbox() {
    context.close();
    Prop.clear();
    if(!sandbox().delete()) throw Util.notExpected("Sandbox could not be deleted.");
  }

  /**
   * Creates a new, sandboxed server instance.
   * @param args additional arguments
   * @return server instance
   * @throws IOException I/O exception
   */
  public static BaseXServer createServer(final String... args) throws IOException {
    final StringList sl = new StringList("-z", "-p" + DB_PORT, "-q");
    for(final String arg : args) sl.add(arg);
    final BaseXServer server = new BaseXServer(sl.finish());
    server.context.soptions.set(StaticOptions.DBPATH, sandbox().path());
    return server;
  }

  /**
   * Stops a server instance.
   * @param server server
   */
  public static void stopServer(final BaseXServer server) {
    if(server != null) server.stop();
  }

  /**
   * Creates a client instance.
   * @param login optional login data
   * @return client instance
   * @throws IOException I/O exception
   */
  public static ClientSession createClient(final String... login) throws IOException {
    final String user = login.length > 0 ? login[0] : UserText.ADMIN;
    final String pass = login.length > 1 ? login[1] : UserText.ADMIN;
    return new ClientSession(S_LOCALHOST, DB_PORT, user, pass);
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  public static IOFile sandbox() {
    return new IOFile(Prop.TEMPDIR, NAME + '/');
  }

  /**
   * Normalizes newlines in a query result.
   * @param result input string
   * @return normalized string
   */
  public static String normNL(final String result) {
    return result.replaceAll("(\r?\n|\r) *", "\n");
  }

  /** Client. */
  public static final class Client extends Thread {
    /** Start signal. */
    private final CountDownLatch startSignal;
    /** Stop signal. */
    private final CountDownLatch stopSignal;
    /** Client session. */
    private final ClientSession session;
    /** Command string. */
    private final Command cmd;
    /** Fail flag. */
    public String error;

    /**
     * Client constructor.
     * @param c command string to execute
     * @param start start signal
     * @param stop stop signal
     * @throws IOException I/O exception while establishing the session
     */
    public Client(final Command c, final CountDownLatch start, final CountDownLatch stop)
        throws IOException {

      session = createClient();
      cmd = c;
      startSignal = start;
      stopSignal = stop;
      start();
    }

    @Override
    public void run() {
      try {
        if(startSignal != null) startSignal.await();
        session.execute(cmd);
        session.close();
      } catch(final Throwable ex) {
        error = "\n" + cmd + '\n' + ex;
      } finally {
        if(stopSignal != null) stopSignal.countDown();
      }
    }
  }
}
