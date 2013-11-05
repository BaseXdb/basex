package org.basex;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.modules.*;
import org.basex.util.*;
import org.expath.ns.*;
import org.exquery.ns.*;
import org.junit.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class SandboxTest {
  /** Default output stream. */
  protected static final PrintStream OUT = System.out;
  /** Default error stream. */
  public static final PrintStream ERR = System.err;
  /** Null output stream. */
  protected static final PrintStream NULL = new PrintStream(new NullOutput());
  /** Test name. */
  protected static final String NAME = Util.className(SandboxTest.class);
  /** Database context. */
  protected static Context context;
  /** Clean up files. */
  private static boolean cleanup;

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
   * Returns the sandbox database path.
   * @return database path
   */
  private static IOFile sandbox() {
    return new IOFile(Prop.TMP, NAME);
  }

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
}
