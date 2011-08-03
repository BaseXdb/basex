package org.basex.test.performance;

import java.util.Random;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * This class performs a local stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class LocalStressTest {
  /** Verbose flag. */
  private static final boolean VERBOSE = false;
  /** Number of clients. */
  private static final int NCLIENTS = 100;
  /** Number of runs per client. */
  private static final int NQUERIES = 100;
  /** Input document. */
  private static final String INPUT = "etc/xml/factbook.xml";
  /** Query to be run ("%" serves as placeholder for dynamic content). */
  private static final String QUERY = "(//text())[position() = %]";

  /** Global context. */
  static final Context CONTEXT = new Context();
  /** Random number generator. */
  static final Random RND = new Random();
  /** Result counter. */
  static int counter;

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== LocalStressTest ===");

    final Performance perf = new Performance();

    // Create test database
    System.out.println("\n* Create test database.");
    final CreateDB cmd = new CreateDB("test", INPUT);
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());

    // Run clients
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    final Client[] cl = new Client[NCLIENTS];
    for(int i = 0; i < NCLIENTS; ++i) cl[i] = new Client();
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    dropDB();

    CONTEXT.close();

    System.out.println("\n* Time: " + perf);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  static void dropDB() throws BaseXException {
    // Drop database
    System.out.println("\n* Drop test database.");

    final DropDB cmd = new DropDB("test");
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());
  }

  /** Single client. */
  static class Client extends Thread {
    @Override
    public void run() {
      try {
        // Perform some queries
        for(int i = 0; i < NQUERIES; ++i) {
          Performance.sleep((long) (50 * RND.nextDouble()));

          // Return nth text of the database
          final int n = (RND.nextInt() & 0xFF) + 1;
          final String qu = Util.info(QUERY, n);
          final String result = new XQuery(qu).execute(CONTEXT);

          if(VERBOSE) System.out.println("[" + counter++ + "] Thread " +
              getId() + ", Pos " + n + ": " + result);
        }
      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
