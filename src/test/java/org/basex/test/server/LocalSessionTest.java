package org.basex.test.server;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.server.*;
import org.junit.*;

/**
 * This class tests the local session API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class LocalSessionTest extends SessionTest {
  /** Database context. */
  private static Context context;

  /** Starts the test. */
  @BeforeClass
  public static void startContext() {
    context = new Context();
    context.mprop.set(MainProp.DBPATH, sandbox().path());
  }

  /** Stops the test. */
  @AfterClass
  public static void stopContext() {
    context.close();
    assertTrue(sandbox().delete());
  }

  /** Starts a session. */
  @Before
  public void startSession() {
    session = new LocalSession(context, out);
  }
}
