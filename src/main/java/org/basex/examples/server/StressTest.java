package org.basex.examples.server;

import java.io.IOException;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * This class performs a client/server stress tests with a specified
 * number of threads and queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class StressTest {
  /** Verbose flag. */
  static final boolean VERBOSE = false;
  /** Number of clients. */
  static final int NCLIENTS = 50;
  /** Number of runs per client. */
  static final int NQUERIES = 50;
  /** Input document. */
  static final String INPUT = "etc/xml/factbook.xml";
  /** Query to be run ("%" serves as placeholder for dynamic content). */
  static final String QUERY =
    "doc('test')/descendant::text()[position() = %]";

  /** Server reference. */
  static BaseXServer server;
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
    System.out.println("=== Server StressTest ===");

    // Run server instance
    System.out.println("\n* Start server.");
    server = new BaseXServer("-z");

    // Create test database
    System.out.println("\n* Create test database.");

    final ClientSession cs = newSession();
    cs.execute("create db test " + INPUT);
    System.out.print(cs.info());
    cs.close();

    // Run clients
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    final Client[] cl = new Client[NCLIENTS];
    for(int i = 0; i < NCLIENTS; ++i) cl[i] = new Client();
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    stopServer();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  static void stopServer() throws Exception {
    // Drop database and stop server
    System.out.println("\n* Stop server and drop test database.");

    final ClientSession cs = newSession();
    cs.execute("drop db test");
    System.out.print(cs.info());

    cs.close();
    server.stop();
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

        // Perform some queries
        for(int i = 0; i < NQUERIES; ++i) {
          Performance.sleep((long) (50 * RND.nextDouble()));

          // Return nth text of the database
          final int n = (RND.nextInt() & 0xFF) + 1;
          final String qu = Util.info(QUERY, n);
          final String result = session.execute("xquery " + qu);

          if(VERBOSE) System.out.println("[" + counter++ + "] Thread " +
              getId() + ", Pos " + n + ": " + result);
        }
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
