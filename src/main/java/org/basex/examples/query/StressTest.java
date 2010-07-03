package org.basex.examples.query;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.util.Performance;

/**
 * This class performs a local stress tests with a specified
 * number of threads and queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class StressTest {
  /** Verbose flag. */
  private static final boolean VERBOSE = false;
  /** Number of clients. */
  static final int NCLIENTS = 30;
  /** Number of runs per client. */
  static final int NQUERIES = 30;

  /** Global context. */
  static final Context CONTEXT = new Context();
  /** Random number generator. */
  static final Random RND = new Random();
  /** Result counter. */
  static int counter;
  /** Finished counter. */
  static int finished;

  /** Private constructor. */
  private StressTest() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException exception
   */
  public static void main(final String[] args) throws BaseXException {
    System.out.println("=== Local StressTest ===");

    // create test database
    System.out.println("\n* Create test database.");
    new Set("info", true).execute(CONTEXT);
    new CreateDB("factbook", "etc/xml/factbook.xml").execute(CONTEXT);

    // run clients
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    for(int i = 0; i < NCLIENTS; i++) new Client().start();
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  static void dropDB() throws BaseXException {
    // drop database
    System.out.println("\n* Drop test database.");
    new DropDB("factbook").execute(CONTEXT);
  }

  /** Single client. */
  static class Client extends Thread {
    @Override
    public void run() {
      try {
        // perform some queries
        for(int i = 0; i < NQUERIES; i++) {
          Performance.sleep((long) (50 * RND.nextDouble()));

          // return nth text of the database
          final int n = (RND.nextInt() & 0xFF) + 1;
          final String qu = "descendant::text()[position() = " + n + "]";

          final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          new XQuery(qu).execute(CONTEXT, buffer);

          if(VERBOSE) System.out.print("[" + counter + "] Thread " +
              getId() + ", Pos " + n + ": " + buffer);
          counter++;
        }

        // database is dropped after last client has finished
        if(++finished == StressTest.NCLIENTS) dropDB();
      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
