package org.basex.core;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Sandbox {
  /** Database port. */
  protected static final int DB_PORT = 9996;

  /** Default output stream. */
  public static final PrintStream OUT = System.out;
  /** Default error stream. */
  public static final PrintStream ERR = System.err;
  /** Null output stream. */
  public static final PrintStream NULL = new PrintStream(new NullOutput());
  /** Test name. */
  protected static final String NAME = Util.className(Sandbox.class);
  /** Database context. */
  protected static Context context;

  /**
   * Creates the sandbox.
   */
  public static void initSandbox() {
    final IOFile sb = sandbox();
    sb.delete();
    if(!sb.md()) throw Util.notExpected("Sandbox could not be created.");

    final String path = sb.path();
    Prop.put(StaticOptions.DBPATH, path + "/data");
    Prop.put(StaticOptions.WEBPATH, path + "/webapp");
    Prop.put(StaticOptions.RESTXQPATH, path + "/webapp");
    Prop.put(StaticOptions.REPOPATH, path + "/repo");
    Prop.put(StaticOptions.SERVERPORT, DB_PORT);
    context = new Context();
  }

  /**
   * Removes test databases and closes the database context.
   */
  public static void finishSandbox() {
    context.close();
    Prop.remove(StaticOptions.DBPATH);
    Prop.remove(StaticOptions.WEBPATH);
    Prop.remove(StaticOptions.RESTXQPATH);
    Prop.remove(StaticOptions.REPOPATH);
    Prop.remove(StaticOptions.SERVERPORT);
    if(!sandbox().delete()) throw Util.notExpected("Sandbox could not be created.");
  }

  /**
   * Creates a new, sandboxed server instance.
   * @param args additional arguments
   * @return server instance
   * @throws IOException I/O exception
   */
  public static BaseXServer createServer(final String... args) throws IOException {
    try {
      System.setOut(NULL);
      final StringList sl = new StringList("-z", "-p" + DB_PORT, "-q");
      for(final String arg : args) sl.add(arg);
      final BaseXServer server = new BaseXServer(sl.finish());
      server.context.soptions.set(StaticOptions.DBPATH, sandbox().path());
      return server;
    } finally {
      System.setOut(OUT);
    }
  }

  /**
   * Stops a server instance.
   * @param server server
   * @throws IOException I/O exception
   */
  public static void stopServer(final BaseXServer server) throws IOException {
    try {
      System.setOut(NULL);
      if(server != null) server.stop();
    } finally {
      System.setOut(OUT);
    }
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
    return new IOFile(Prop.TMP, NAME);
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
