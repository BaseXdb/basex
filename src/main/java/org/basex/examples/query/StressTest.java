package org.basex.examples.query;

import java.util.Random;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
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
  /** Input document. */
  static final String INPUT = "etc/xml/factbook.xml";
  /** Query to be run ("%" serves as placeholder for dynamic content). */
  static final String QUERY = "descendant::text()[position() = %]";

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
    final CreateDB cmd = new CreateDB("test", INPUT); 
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());

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

    final DropDB cmd = new DropDB("test"); 
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());
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
          final String qu = Main.info(QUERY, n);
          final String result = new XQuery(qu).execute(CONTEXT);

          if(VERBOSE) System.out.println("[" + counter++ + "] Thread " +
              getId() + ", Pos " + n + ": " + result);
        }

        // database is dropped after last client has finished
        if(++finished == StressTest.NCLIENTS) dropDB();
      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
