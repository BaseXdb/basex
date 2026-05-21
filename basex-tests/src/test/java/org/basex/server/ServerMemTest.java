package org.basex.server;

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
  /** Query to be run. */
  private static final String QUERY = "(for $i in 1 to 50000 order by $i return $i)[1]";
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
          session.execute("XQUERY " + QUERY);
        }
        return null;
      });
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
