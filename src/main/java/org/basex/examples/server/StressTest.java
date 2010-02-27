package org.basex.examples.server;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;

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

  /** Random generator. */
  static Random generator = new Random();
  /** Result counter. */
  static int counter;
  /** Finished counter. */
  static int finished;


  /** Server reference. */
  static BaseXServer server;

  /** Private constructor. */
  private StressTest() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== ServerTest ===");

    // run server instance
    new Thread() {
      @Override
      public void run() {
        server = new BaseXServer();
      }
    }.start();
    Thread.sleep(1000);

    // create test database
    ClientSession session =
      new ClientSession("localhost", 1984, "admin", "admin");
    session.execute("set info on");

    session.execute("create db etc/xml/factbook.xml");
    System.out.println(session.info());
    session.close();

    // run clients
    for(int i = 0; i < NTHREADS; i++) {
      new ClientTest().start();
    }
  }

  /**
   * Single client.
   */
  static class ClientTest extends Thread {
    /** Client session. */
    private ClientSession session;

    @Override
    public void run() {
      try {
        session = new ClientSession("localhost", 1984, "admin", "admin");

        for(int i = 0; i < NRUNS; i++) {
          Thread.sleep((long) (50 * generator.nextDouble()));
    
          // return nth text of the database
          int n = (generator.nextInt() & 0xFF) + 1;
          String query = "xquery basex:db('factbook')/descendant::text()" +
            "[position() = " + n + "]";

          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          String result = session.execute(query, buffer) ?
              buffer.toString() : session.info();

          System.out.println("[" + counter + "] Thread " + getId() +
              ", Pos " + n + ": " + result);
          counter++;
        }

        session.close();

        // Server is stopped by last session
        if(++finished == StressTest.NTHREADS) {
          new BaseXServer("stop");
        }

      } catch(Exception e) {
        System.out.printf("\n******************* Exception: %s\n\n",
            e.getMessage());
      }
    }
  }
}
