package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.api.client.*;
import org.junit.jupiter.api.*;

/**
 * Runs memory-intensive queries from concurrent client sessions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(300)
public final class ServerMemTest extends SandboxTest {
  /** Number of items to be sorted by a single client. */
  private static final int ITEMS = 50000;
  /** Query to be run: the random values keep the sequence from being pre-evaluated. */
  private static final String QUERY =
      "count(sort((1 to " + ITEMS + ") ! random:integer()))";

  /**
   * Runs the test with 10 clients.
   * @throws Exception exception
   */
  @Test public void clients10() throws Exception {
    run(10);
  }

  /**
   * Runs the test with 100 clients.
   * @throws Exception exception
   */
  @Test public void clients100() throws Exception {
    run(100);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @throws Exception exception
   */
  private static void run(final int clients) throws Exception {
    final BaseXServer server = createServer();
    try {
      // run clients, each executing one memory-intensive query
      parallel(clients, () -> {
        try(ClientSession session = createClient()) {
          assertEquals(Integer.toString(ITEMS), session.execute("XQUERY " + QUERY).trim());
        }
        return null;
      });
    } finally {
      stopServer(server);
    }
  }
}
