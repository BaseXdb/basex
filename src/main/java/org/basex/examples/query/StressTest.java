package org.basex.examples.query;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.util.Performance;

/**
 * This class performs a small stress tests with multiple clients.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class StressTest {
  /** Number of clients. */
  static final int NTHREADS = 30;
  /** Number of runs per client. */
  static final int NRUNS = 30;

  /** Global context. */
  static Context context = new Context();
  /** Random number generator. */
  static Random rnd = new Random();
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
    System.out.println("=== StressTest ===");

    // create test database
    new Set("info", true).execute(context);
    new CreateDB("etc/xml/factbook.xml").execute(context);

    // run clients
    for(int i = 0; i < NTHREADS; i++) {
      new Client().start();
    }
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  static void dropDB() throws BaseXException {
    // drop database
    new DropDB("factbook").execute(context);
  }
  
  /** Single client. */
  static class Client extends Thread {
    @Override
    public void run() {
      try {
        Context ctx = new Context(context);
        ctx.user = ctx.users.get("admin");

        // perform some queries
        for(int i = 0; i < NRUNS; i++) {
          Performance.sleep((long) (50 * rnd.nextDouble()));
    
          // return nth text of the database
          final int n = (rnd.nextInt() & 0xFF) + 1;
          final String qu = "basex:db('factbook')/descendant::text()" +
            "[position() = " + n + "]";

          final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          new XQuery(qu).execute(ctx, buffer);

          System.out.println("[" + counter + "] Thread " + getId() +
              ", Pos " + n + ": " + buffer);
          counter++;
        }

        // server is stopped by last session
        if(++finished == StressTest.NTHREADS) dropDB();

      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
