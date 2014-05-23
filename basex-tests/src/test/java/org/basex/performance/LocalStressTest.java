package org.basex.performance;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * This class performs a local stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class LocalStressTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/factbook.zip";
  /** Query to be run ("%" may be used as placeholder for dynamic content). */
  private static final String QUERY = "(//text())[position() = %]";
  /** Maximum position to retrieve. */
  private static final int MAX = 1000;

  /** Random number generator. */
  static final Random RND = new Random();
  /** Result counter. */
  static int counter;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients10runs10() throws Exception {
    run(10, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients10runs100() throws Exception {
    run(10, 100);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients100runs10() throws Exception {
    run(100, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void clients100runs100() throws Exception {
    run(100, 100);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private static void run(final int clients, final int runs) throws Exception {
    // Create test database
    Command cmd = new CreateDB(NAME, INPUT);
    cmd.execute(context);

    // Start clients
    final Client[] cl = new Client[clients];
    for(int i = 0; i < clients; ++i) cl[i] = new Client(runs);
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    // Drop database
    cmd = new DropDB(NAME);
    cmd.execute(context);
  }

  /** Single client. */
  static class Client extends Thread {
    /** Number of runs. */
    private final int runs;

    /**
     * Constructor.
     * @param r number of runs
     */
    Client(final int r) {
      runs = r;
    }

    @Override
    public void run() {
      try {
        for(int i = 0; i < runs; ++i) {
          Performance.sleep((long) (50 * RND.nextDouble()));
          // Return nth text of the database
          final int n = RND.nextInt() % MAX + 1;
          final String qu = Util.info(QUERY, n);
          new XQuery(qu).execute(context);
        }
      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
