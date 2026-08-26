package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.api.client.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server memory stress tests with a specified number of threads and
 * queries.
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
  /** Server reference. */
  BaseXServer server;

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
  private void run(final int clients) throws Exception {
    // run server instance
    server = createServer();
    try {
      // run clients, each executing one memory-intensive query
      parallel(clients, () -> {
        try(ClientSession session = createClient()) {
          assertEquals(Integer.toString(ITEMS), session.execute("XQUERY " + QUERY).trim());
        }
        return null;
      });
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
