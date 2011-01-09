package org.basex.test.server;

import static org.basex.core.Text.*;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * This class tests the database commands with the client/server
 * architecture.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class ServerCmdTest extends CmdTest {
  /** Server instance. */
  private static BaseXServer server;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");

    try {
      session = new ClientSession(CONTEXT, ADMIN, ADMIN);
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    try {
      session.close();
    } catch(final Exception ex) {
      fail(ex.toString());
    }
    // stop server instance
    server.stop();
  }
}
