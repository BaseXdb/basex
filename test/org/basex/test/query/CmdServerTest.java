package org.basex.test.query;

import org.basex.BaseXServer;
import org.basex.core.ALauncher;
import org.basex.server.ClientLauncher;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * This class tests the database commands with the client/server
 * architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CmdServerTest extends CmdTest {
  /** Socket reference. */
  private static ALauncher launcher;
  /** Server reference. */
  static BaseXServer server;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    new Thread() {
      @Override
      public void run() {
        server = new BaseXServer();
      }
    }.start();

    try {
      launcher = new ClientLauncher(CONTEXT);
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }

  /** Stops the server. */
  @AfterClass
  public static void quit() {
    server.quit();
  }

  @Override
  protected ALauncher launcher() {
    return launcher;
  }
}
