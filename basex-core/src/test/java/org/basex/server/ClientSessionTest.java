package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the client/server session API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ClientSessionTest extends SessionTest {
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void startServer() throws IOException {
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
  }

  /** Starts a session. */
  @BeforeEach public void startSession() {
    try {
      session = createClient();
      session.setOutputStream(out);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }
}
