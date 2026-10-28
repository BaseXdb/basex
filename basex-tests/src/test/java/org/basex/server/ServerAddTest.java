package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * Adds documents to a database from concurrent client sessions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class ServerAddTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/input.xml";

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
  private static void run(final int clients, final int runs) throws Exception {
    final BaseXServer server = createServer();
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
        // every client must have added all of its documents; the database was created with one
        assertEquals(Integer.toString(clients * runs + 1),
            cs.execute("XQUERY count(db:get('" + NAME + "'))").trim());
        // drop database
        cs.execute("DROP DB " + NAME);
      }
    } finally {
      stopServer(server);
    }
  }
}
