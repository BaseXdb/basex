package org.basex.server;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class ServerAddTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/input.xml";

  /** Server reference. */
  BaseXServer server;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients10runs10() throws Exception {
    run(10, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients10runs100() throws Exception {
    run(10, 100);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients100runs10() throws Exception {
    run(100, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients100runs100() throws Exception {
    run(100, 100);
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
        // run clients, each adding the input document repeatedly
        parallel(clients, () -> {
          try(ClientSession session = createClient()) {
            session.execute("SET " + MainOptions.AUTOFLUSH.name() + " false");
            session.execute("SET " + MainOptions.INTPARSE.name() + " true");
            session.execute("OPEN " + NAME);
            for(int r = 0; r < runs; r++) session.execute("ADD " + INPUT);
          }
          return null;
        });
        // drop database
        cs.execute("DROP DB " + NAME);
      }
    } finally {
      // stop server
      stopServer(server);
    }
  }
}
