package org.basex.server;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ServerAddTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/input.xml";

  /** Server reference. */
  BaseXServer server;
  /** Result counter. */
  int counter;

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
    // create test database
    try(ClientSession cs = createClient()) {
      cs.execute("create db " + NAME + ' ' + INPUT);
      // run clients
      final Client[] cl = new Client[clients];
      for(int i = 0; i < clients; ++i) cl[i] = new Client(runs);
      for(final Client c : cl) c.start();
      for(final Client c : cl) c.join();
      // drop database and stop server
      cs.execute("drop db test");
    }
    stopServer(server);
  }

  /** Single client. */
  static final class Client extends Thread {
    /** Client session. */
    private final ClientSession session;
    /** Number of runs. */
    private final int runs;

    /**
     * Constructor.
     * @param runs number of runs
     * @throws Exception exception
     */
    Client(final int runs) throws Exception {
      this.runs = runs;
      session = createClient();
    }

    @Override
    public void run() {
      try {
        session.execute("set " + MainOptions.AUTOFLUSH.name() + " false");
        session.execute("set " + MainOptions.INTPARSE.name() + " true");
        session.execute("open " + NAME);
        for(int i = 0; i < runs; ++i) session.execute("add " + INPUT);
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
