package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

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
  /** Maximum position to retrieve. */
  private static final int MAX = 1000;
  /** Maximum delay between two queries (ms). */
  private static final int DELAY = 50;

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
        cs.execute("CREATE DB " + NAME + ' ' + INPUT);
        // only request positions that exist
        final int max = Math.min(MAX, Integer.parseInt(
            cs.execute("XQUERY count(db:get('" + NAME + "')//text())").trim()));

        // run clients, each retrieving the nth text of the database
        final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
        for(int c = 0; c < clients; c++) {
          // one generator per client: a shared instance would not be reproducible
          final Random rnd = new Random(c);
          tasks.add(() -> {
            try(ClientSession session = createClient()) {
              for(int r = 0; r < runs; r++) {
                Performance.sleep(rnd.nextInt(DELAY));
                // the position exists, so exactly one text node must be returned
                final String result = session.execute("XQUERY count((db:get('" + NAME +
                    "')//text())[position() = " + (rnd.nextInt(max) + 1) + "])");
                assertEquals("1", result.trim());
              }
            }
            return null;
          });
        }
        parallel(tasks);
        // drop database
        cs.execute("DROP DB " + NAME);
      }
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
