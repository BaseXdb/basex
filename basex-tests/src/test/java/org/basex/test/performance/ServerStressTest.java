package org.basex.test.performance;

import java.util.*;

import org.basex.*;
import org.basex.server.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class performs a client/server stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ServerStressTest extends SandboxTest {
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
  /** Result counter. */
  int counter;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients20runs20() throws Exception {
    run(20, 20);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients20runs200() throws Exception {
    run(20, 200);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients200runs20() throws Exception {
    run(200, 20);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients200runs200() throws Exception {
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
    final ClientSession cs = createClient();
    cs.execute("create db test " + INPUT);
    // run clients
    final Client[] cl = new Client[clients];
    for(int i = 0; i < clients; ++i) cl[i] = new Client(runs);
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    // drop database and stop server
    cs.execute("drop db test");
    cs.close();
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
     * @param r number of runs
     * @throws Exception exception
     */
    public Client(final int r) throws Exception {
      runs = r;
      session = createClient();
    }

    @Override
    public void run() {
      try {
        // Perform some queries
        for(int i = 0; i < runs; ++i) {
          Performance.sleep((long) (50 * RND.nextDouble()));

          // Return nth text of the database
          final int n = RND.nextInt() % MAX + 1;
          final String qu = Util.info(QUERY, n);
          session.execute("xquery " + qu);
        }
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
