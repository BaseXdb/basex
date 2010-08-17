package org.basex.examples.server;

import java.io.IOException;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.core.Main;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

/**
 * This class performs a client/server stress tests with a specified
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
  static final String QUERY =
    "basex:db('test')/descendant::text()[position() = %]";

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
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== Server StressTest ===");

    // run server instance
    System.out.println("\n* Start server.");
    new BaseXServer("-z");

    // create test database
    System.out.println("\n* Create test database.");

    final ClientSession cs = newSession();
    cs.execute("create db test " + INPUT);
    System.out.print(cs.info());
    cs.close();

    // run clients
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    for(int i = 0; i < NCLIENTS; ++i) {
      new Client().start();
    }
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  static void stopServer() throws Exception {
    // drop database and stop server
    System.out.println("\n* Stop server and drop test database.");

    final ClientSession cs = newSession();
    cs.execute("drop db test");
    System.out.print(cs.info());

    cs.close();
    new BaseXServer("stop");
  }

  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession("localhost", 1984, "admin", "admin");
  }

  /** Single client. */
  static class Client extends Thread {
    /** Client session. */
    private ClientSession session;

    @Override
    public void run() {
      try {
        session = newSession();

        // perform some queries
        for(int i = 0; i < NQUERIES; ++i) {
          Performance.sleep((long) (50 * RND.nextDouble()));

          // return nth text of the database
          final int n = (RND.nextInt() & 0xFF) + 1;
          final String qu = Main.info(QUERY, n);
          final String result = session.execute("xquery " + qu);

          if(VERBOSE) System.out.println("[" + counter++ + "] Thread " +
              getId() + ", Pos " + n + ": " + result);
        }
        session.close();

        // server is stopped after last client has finished
        if(++finished == StressTest.NCLIENTS) stopServer();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
