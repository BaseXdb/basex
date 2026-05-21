package org.basex.server;

import java.util.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class ServerQueryTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/factbook.zip";
  /** Query to be run ("%" may be used as placeholder for dynamic content). */
  private static final String QUERY = "(doc('test')//text())[position() = %]";
  /** Maximum position to retrieve. */
  private static final int MAX = 1000;

  /** Random number generator. */
  static final Random RND = new Random();
  /** Server reference. */
  BaseXServer server;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients20runs20() throws Exception {
    run(20, 20);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients20runs200() throws Exception {
    run(20, 200);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients200runs20() throws Exception {
    run(200, 20);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients200runs200() throws Exception {
    run(200, 200);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private void run(final int clients, final int runs) throws Exception {
    // run server instance
    server = createServer();
    try {
      // create test database
      try(ClientSession cs = createClient()) {
        cs.execute("CREATE DB test " + INPUT);
        // run clients, each retrieving the nth text of the database
        parallel(clients, () -> {
          try(ClientSession session = createClient()) {
            for(int r = 0; r < runs; r++) {
              Performance.sleep((long) (50 * RND.nextDouble()));
              session.execute("XQUERY " + Util.info(QUERY, RND.nextInt() % MAX + 1));
            }
          }
          return null;
        });
        // drop database
        cs.execute("DROP DB test");
      }
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
