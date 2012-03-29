package org.basex.test.core;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.server.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the database commands with the client/server
 * architecture.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ServerCommandTest extends CommandTest {
  /** Server instance. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = createServer();
    session = new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN);
    cleanUp();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void finish() throws IOException {
    try {
      if(session != null) session.close();
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
    // stop server instance
    if(server != null) server.stop();
  }

  /**
   * Kill test.
   * @throws IOException on server error
   */
  @Test
  public void kill() throws IOException {
    ok(new Kill(ADMIN));
    ok(new Kill(ADMIN + '2'));
    ok(new Kill(Prop.NAME + '*'));
    ok(new CreateUser(NAME2, Token.md5("test")));
    final ClientSession cs = new ClientSession(LOCALHOST, 9999, NAME2, "test");
    ok(new Kill(NAME2));
    ok(new Kill(NAME2 + '?'));
    cs.close();
    // may be superfluous
    Performance.sleep(100);
  }
}
