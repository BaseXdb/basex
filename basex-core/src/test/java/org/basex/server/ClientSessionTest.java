package org.basex.server;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the client/server session API.
 *
 * @author BaseX Team 2005-14, BSD License
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
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    stopServer(server);
  }

  /** Starts a session. */
  @Before
  public void startSession() {
    try {
      session = createClient();
      session.setOutputStream(out);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }
}
