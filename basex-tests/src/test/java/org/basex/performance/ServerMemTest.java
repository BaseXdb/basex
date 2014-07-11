package org.basex.performance;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.SandboxTest;
import org.junit.*;

/**
 * This class performs a client/server memory stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ServerMemTest extends SandboxTest {
  /** Query to be run. */
  private static final String QUERY = "(for $i in 1 to 50000 order by $i return $i)[1]";
  /** Server reference. */
  BaseXServer server;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients10() throws Exception {
    run(10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients100() throws Exception {
    run(100);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @throws Exception exception
   */
  private void run(final int clients) throws Exception {
    //run server instance
    server = createServer();
    // run clients
    final Client[] cl = new Client[clients];
    for(int i = 0; i < clients; ++i) cl[i] = new Client();
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    // stop server
    stopServer(server);
  }

  /** Single client. */
  static final class Client extends Thread {
    /** Client session. */
    private final ClientSession session;

    /**
     * Default constructor.
     * @throws Exception exception
     */
    public Client() throws Exception {
      session = createClient();
    }

    @Override
    public void run() {
      try {
        // Perform query
        session.execute("xquery " + QUERY);
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
