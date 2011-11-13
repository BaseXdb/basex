package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * This class tests the client/server API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class ClientSessionTest extends SessionTest {
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void startServer() throws IOException {
    server = new BaseXServer("-z", "-p9999", "-e9998");
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    server.stop();
  }

  /** Starts a session. */
  @Before
  public void startSession() {
    try {
      session = new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN, out);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }
}
