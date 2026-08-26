package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

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
        cs.execute("CREATE DB " + NAME + " <test/>");
        // even clients read, odd clients write; the database starts out with a single document
        final int writers = clients / 2, documents = 1 + writers * runs;

        final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
        for(int i = 0; i < clients; i++) {
          final boolean read = i % 2 == 0;
          tasks.add(() -> {
            try(ClientSession session = createClient()) {
              session.execute("SET AUTOFLUSH false");
              for(int r = 0; r < runs; r++) {
                if(read) {
                  // readers observe a database that only grows, and only by whole documents
                  final int count = Integer.parseInt(
                      session.execute("XQUERY count(db:get('" + NAME + "'))").trim());
                  assertTrue(count >= 1 && count <= documents,
                      "Unexpected number of documents: " + count);
                } else {
                  session.execute("XQUERY db:add('" + NAME + "', <a/>, 'test.xml', " +
                      "{ 'intparse': true() })");
                }
              }
            }
            return null;
          });
        }
        parallel(tasks);

        // every write must have been applied
        assertEquals(Integer.toString(documents),
            cs.execute("XQUERY count(db:get('" + NAME + "'))").trim());
        // drop database
        cs.execute("DROP DB " + NAME);
      }
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
