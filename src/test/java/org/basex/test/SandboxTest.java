package org.basex.test;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
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
  /** Test name. */
  public static final String NAME = Util.name(SandboxTest.class);
  /** Database context. */
  public static Context context;
  /** Clean up files. */
  public static boolean cleanup = true;

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
  }

  /**
   * Initializes the specified context.
   * @param ctx context
   */
  protected static void initContext(final Context ctx) {
    final IOFile sb = sandbox();
    ctx.mprop.set(MainProp.DBPATH, sb.path() + "/data");
    ctx.mprop.set(MainProp.HTTPPATH, sb.path() + "/http");
    ctx.mprop.set(MainProp.REPOPATH, sb.path() + "/repo");
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
    final StringList sl = new StringList().add("-z").add("-p9999").add("-e9998");
    for(final String a : args) sl.add(a);
    final BaseXServer server = new BaseXServer(sl.toArray());
    server.context.mprop.set(MainProp.DBPATH, sandbox().path());
    return server;
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
}
