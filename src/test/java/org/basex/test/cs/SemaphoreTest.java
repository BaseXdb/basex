package org.basex.test.cs;

import java.io.IOException;
import java.util.Random;

import org.basex.server.ClientSession;

/**
 * Testing the semaphore.
 *
 * Prerequisites:
 * Run BaseXServer...
 * Create Factbook DB...
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public class SemaphoreTest {

  /** Create random number. */
  static Random rand = new Random();
  
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   */
  public static void main(final String[] args) {
    new SemaphoreTest().run();
  }

  /**
   * Runs the test.
   */
  void run() {
    System.out.println("=== Semaphore Test ===");

    final String [] queries = {"xquery for $n in doc('factbook')//city " +
        "return insert node <test/> into $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n"
    };

    runClients(queries);
  }

  /**
   * Runs the different tests.
   * @param q array of queries
   */
  private void runClients(final String[] q) {
    for (int n = 1; n <= 10; n++) {
      final int j = n;
      try {
        Thread.sleep(2000);
      } catch(final InterruptedException e1) {
        e1.printStackTrace();
      }
      new Thread() {
        @Override
        public void run() {
          try {
            final ClientSession session =
              new ClientSession("localhost", 1984, "admin", "admin");
            int t = rand.nextInt(2);
            session.execute(q[t]);
            System.out.println("=== Client " + j + " with query " + t +
                " done ===");
          } catch(final IOException e) {
            e.printStackTrace();
          }
        }
      }.start();
    }
  }
}
