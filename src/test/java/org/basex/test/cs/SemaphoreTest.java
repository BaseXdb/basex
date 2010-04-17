package org.basex.test.cs;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
  /** Server reference. */
  static BaseXServer server;
  /** Number of done tests. */
  static int tdone;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer();
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    // Stop server instance.
    new BaseXServer("stop");
  }
  
  /** Runs the test. */
  @Test
  public void run() {
    /*final String [] queries1 = {
        "xquery for $n in doc('factbook')//city "+
        "  return insert node <test/> into $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n",
        "xquery for $n in doc('factbook')//city " +
        "  return insert node <test/> into $n"
    };
    final String [] queries2 = {
        "xquery for $n in doc('factbook')//city " +
        "  return insert node <test/> into $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n",
        "xquery for $n in doc('factbook')//city " +
        "  return insert node <test/> into $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n"
    };*/
    final String [] queries3 = {
        "xquery for $n in doc('factbook')//city " +
        "  return insert node <test/> into $n",
        "xquery for $n in doc('factbook')//city " +
        "  return insert node <test/> into $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n",
        "xquery for $n in doc('factbook')//city " +
        "  return insert node <test/> into $n",
        "xquery for $n in 1 to 1000000 where $n = 999999 return $n"
    };

    //runClients(queries1);
    //runClients(queries2);
    runClients(queries3);
  }

  /**
   * Runs the different tests.
   * @param q array of queries
   */
  private void runClients(final String[] q) {
    for(int n = 1; n <= q.length; n++) {
      final int j = n;
      Performance.sleep(2000);

      new Thread() {
        @Override
        public void run() {
          try {
            final ClientSession cs =
              new ClientSession("localhost", 1984, "admin", "admin");
            cs.execute(q[j - 1]);
            System.out.println("=== Client Done: " + j + " ===");
            tdone++;
          } catch(final IOException e) {
            e.printStackTrace();
          }
        }
      }.start();
    }

    // wait until all test have been finished
    while(tdone < q.length) Performance.sleep(200);
  }
}
