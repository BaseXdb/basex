package org.basex.server;

import org.basex.*;
import org.basex.api.client.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server stress test with concurrent read and write operations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ServerReadWriteTest extends SandboxTest {
  /** Server reference. */
  BaseXServer server;
  /** Result counter. */
  int counter;

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
    // create test database
    try(ClientSession cs = createClient()) {
      cs.execute("create db test <test/>");
      // run clients
      final Client[] cl = new Client[clients];
      for(int i = 0; i < clients; ++i) cl[i] = new Client(runs, i % 2 == 0);
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
    /** Read flag. */
    private final boolean read;

    /**
     * Constructor.
     * @param runs number of runs
     * @param read read flag
     * @throws Exception exception
     */
    Client(final int runs, final boolean read) throws Exception {
      this.runs = runs;
      this.read = read;
      session = createClient();
      session.execute("set autoflush false");
    }

    @Override
    public void run() {
      try {
        // Perform some queries
        for(int i = 0; i < runs; ++i) {
          final String qu = read ? "count(db:open('test'))" :
            "db:add('test', <a/>, 'test.xml', map { 'intparse': true() })";
          session.execute("xquery " + qu);
        }
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
