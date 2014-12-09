package org.basex;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.modules.*;
import org.basex.modules.Session;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;
import org.expath.ns.*;
import org.exquery.ns.*;
import org.junit.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-14, BSD License
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
  protected static final String NAME = Util.className(SandboxTest.class);
  /** Database context. */
  protected static Context context;

  /**
   * Dummy method; avoids that visibility of query modules gets weakened.
   */
  static void visibility() {
    new Restxq();
    new Request();
    new Response();
    new Session();
    new Sessions();
    new Geo();
  }

  /**
   * Creates the sandbox.
   */
  @BeforeClass
  public static void initSandbox() {
    final IOFile sb = sandbox();
    sb.delete();
    assertTrue("Sandbox could not be created.", sb.md());
    context = newContext();
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterClass
  public static void closeContext() {
    context.close();
    assertTrue("Sandbox could not be deleted.", sandbox().delete());
  }

  /**
   * Creates a new specified context.
   * @return context
   */
  public static Context newContext() {
    final IOFile sb = sandbox();
    Options.setSystem(StaticOptions.DBPATH.name(), sb.path() + "/data");
    Options.setSystem(StaticOptions.WEBPATH.name(), sb.path() + "/webapp");
    Options.setSystem(StaticOptions.RESTXQPATH.name(), sb.path() + "/webapp");
    Options.setSystem(StaticOptions.REPOPATH.name(), sb.path() + "/repo");
    try {
      return new Context();
    } finally {
      Options.setSystem(StaticOptions.DBPATH.name(), "");
      Options.setSystem(StaticOptions.WEBPATH.name(), "");
      Options.setSystem(StaticOptions.RESTXQPATH.name(), "");
      Options.setSystem(StaticOptions.REPOPATH.name(), "");
    }
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
      final StringList sl = new StringList().add("-z").add("-p9999").add("-e9998").add("-q");
      for(final String a : args) sl.add(a);
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
    return new ClientSession(S_LOCALHOST, 9999, user, pass);
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  public static IOFile sandbox() {
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
