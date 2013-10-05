package org.basex.test;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class SandboxTest {
  /** Default output stream. */
  public static final PrintStream OUT = System.out;
  /** Default error stream. */
  public static final PrintStream ERR = System.err;
  /** Null output stream. */
  public static final PrintStream NULL = new PrintStream(new NullOutput());
  /** Test name. */
  public static final String NAME = Util.name(SandboxTest.class);
  /** Database context. */
  protected static Context context;
  /** Clean up files. */
  protected static boolean cleanup;

  /**
   * Creates the sandbox.
   */
  @BeforeClass
  public static void createContext() {
    final IOFile sb = sandbox();
    sb.delete();
    assertTrue("Sandbox could not be created.", sb.md());
    context = new Context();
    initContext(context);
    cleanup = true;
  }

  /**
   * Initializes the specified context.
   * @param ctx context
   */
  protected static void initContext(final Context ctx) {
    final IOFile sb = sandbox();
    ctx.globalopts.set(GlobalOptions.DBPATH, sb.path() + "/data");
    ctx.globalopts.set(GlobalOptions.WEBPATH, sb.path() + "/webapp");
    ctx.globalopts.set(GlobalOptions.RESTXQPATH, sb.path() + "/webapp");
    ctx.globalopts.set(GlobalOptions.REPOPATH, sb.path() + "/repo");
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterClass
  public static void closeContext() {
    if(cleanup) {
      context.close();
      assertTrue("Sandbox could not be deleted.", sandbox().delete());
    }
  }

  /**
   * Creates a new, sandboxed server instance.
   * @param args additional arguments
   * @return server instance
   * @throws IOException I/O exception
   */
  protected static BaseXServer createServer(final String... args) throws IOException {
    try {
      System.setOut(NULL);
      final StringList sl = new StringList().add("-z").add("-p9999").add("-e9998");
      for(final String a : args) sl.add(a);
      final BaseXServer server = new BaseXServer(sl.toArray());
      server.context.globalopts.set(GlobalOptions.DBPATH, sandbox().path());
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
  protected static void stopServer(final BaseXServer server) throws IOException {
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
    final String user = login.length > 0 ? login[0] : ADMIN;
    final String pass = login.length > 1 ? login[1] : ADMIN;
    return new ClientSession(LOCALHOST, 9999, user, pass);
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  protected static IOFile sandbox() {
    return new IOFile(Prop.TMP, NAME);
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
        if(null != startSignal) startSignal.await();
        session.execute(cmd);
        session.close();
      } catch(final Throwable ex) {
        error = "\n" + cmd + "\n" + ex;
      } finally {
        if(null != stopSignal) stopSignal.countDown();
      }
    }
  }
}
