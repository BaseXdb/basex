package org.basex.test.server;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.Text;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Kill;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class tests the database commands with the client/server
 * architecture.
 *
 * @author BaseX Team 2005-11, BSD License
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
    server = new BaseXServer("-z -p9999 -e9998");
    session = new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN);
    cleanUp();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
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
    no(new Kill(Text.ADMIN));
    no(new Kill(Text.ADMIN + "2"));
    no(new Kill("ha*"));
    ok(new CreateUser(NAME2, Token.md5("test")));
    final ClientSession cs = new ClientSession(LOCALHOST, 9999, NAME2, "test");
    ok(new Kill(NAME2));
    no(new Kill(NAME2 + "?"));
    cs.close();
    // may be superfluous
    Performance.sleep(100);
  }
}
