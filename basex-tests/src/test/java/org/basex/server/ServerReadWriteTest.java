package org.basex.server;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server stress test with concurrent read and write operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class ServerReadWriteTest extends SandboxTest {
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
        cs.execute("CREATE DB test <test/>");
        // run clients; even clients read, odd clients write
        final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
        for(int i = 0; i < clients; i++) {
          final boolean read = i % 2 == 0;
          tasks.add(() -> {
            try(ClientSession session = createClient()) {
              session.execute("SET AUTOFLUSH false");
              for(int r = 0; r < runs; r++) {
                final String query = read ? "count(db:get('test'))" :
                  "db:add('test', <a/>, 'test.xml', { 'intparse': true() })";
                session.execute("XQUERY " + query);
              }
            }
            return null;
          });
        }
        parallel(tasks);
        // drop database
        cs.execute("DROP DB test");
      }
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
