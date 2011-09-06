package org.basex.test.server;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.Text;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Kill;
import org.basex.server.ClientSession;
import org.basex.util.Token;
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

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");
    try {
      session = new ClientSession(CONTEXT, ADMIN, ADMIN);
      cleanUp();
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
    final ClientSession cs = new ClientSession(CONTEXT, NAME2, "test");
    ok(new Kill(NAME2));
    no(new Kill(NAME2 + "?"));
    cs.close();
  }
}
