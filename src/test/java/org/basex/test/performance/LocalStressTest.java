package org.basex.test.performance;

import java.util.Random;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.junit.Test;

/**
 * This class performs a local stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class LocalStressTest {
  /** Test database name. */
  private static final String DB = Util.name(LocalStressTest.class);
  /** Input document. */
  private static final String INPUT = "src/test/resources/factbook.zip";
  /** Query to be run ("%" may be used as placeholder for dynamic content). */
  private static final String QUERY = "(//text())[position() = %]";
  /** Maximum position to retrieve. */
  private static final int MAX = 1000;

  /** Global context. */
  static final Context CONTEXT = new Context();
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
    Command cmd = new CreateDB(DB, INPUT);
    cmd.execute(CONTEXT);

    // Start clients
    final Client[] cl = new Client[clients];
    for(int i = 0; i < clients; ++i) cl[i] = new Client(runs);
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    // Drop database
    cmd = new DropDB(DB);
    cmd.execute(CONTEXT);
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
          new XQuery(qu).execute(CONTEXT);
        }
      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
