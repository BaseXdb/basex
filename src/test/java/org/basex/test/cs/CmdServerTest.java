package org.basex.test.cs;

import static org.basex.core.Text.*;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * This class tests the database commands with the client/server
 * architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CmdServerTest extends CmdTest {
  /** Server instance. */
  private static BaseXServer server;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");

    try {
      session = new ClientSession(CONTEXT, ADMIN, ADMIN);
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    try {
      session.close();
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
    // stop server instance
    server.stop();
  }
}
