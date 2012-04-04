package org.basex.test;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class SandboxTest {
  /** Database context. */
  protected static final Context CONTEXT = new Context();
  /** Test name. */
  public static final String NAME = Util.name(SandboxTest.class);
  /** Clean up files (default: {@code true}. */
  public static boolean cleanup = true;

  /**
   * Creates the sandbox.
  */
  @BeforeClass
  public static void createContext() {
    final IOFile sb = sandbox();
    sb.delete();
    assertTrue("Sandbox could not be created.", sb.md());
    CONTEXT.mprop.set(MainProp.DBPATH, sb.path());
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterClass
  public static void closeContext() {
    if(cleanup) {
      assertTrue("Sandbox could not be deleted.", sandbox().delete());
      CONTEXT.close();
    }
  }

  /**
   * Creates a new, sandboxed server instance.
   * @return server instances
   * @throws IOException I/O exception
   */
  protected static BaseXServer createServer() throws IOException {
    final BaseXServer server = new BaseXServer("-z", "-p9999", "-e9998");
    server.context.mprop.set(MainProp.DBPATH, sandbox().path());
    return server;
  }

  /**
   * Returns the sandbox database path.
   * @return database path
   */
  protected static IOFile sandbox() {
    return new IOFile(Prop.TMP, NAME);
  }
}
