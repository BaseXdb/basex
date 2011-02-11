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
 * @author BaseX Team 2005-11, ISC License
 */
public final class ServerStressTest {
  /** Verbose flag. */
  private static final boolean VERBOSE = false;
  /** Number of clients. */
  private static final int NCLIENTS = 100;
  /** Number of runs per client. */
  private static final int NQUERIES = 100;
  /** Input document. */
  private static final String INPUT = "etc/xml/factbook.xml";
  /** Query to be run ("%" serves as placeholder for dynamic content). */
  private static final String QUERY = "(doc('test')//text())[position() = %]";

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
    System.out.println("=== ServerStressTest ===");

    final Performance perf = new Performance();

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

    System.out.println("\n* Time: " + perf);
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  static void stopServer() throws Exception {
    // Drop database and stop server
    System.out.println("\n* Stop server and drop test database.");
    final ClientSession cs = newSession();
    try {
      cs.execute("drop db test");
    } catch(final Exception ex) {
      System.out.println(cs.info());
      ex.printStackTrace();
    }
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
  static final class Client extends Thread {
    /** Client session. */
    private ClientSession session;

    /**
     * Default constructor.
     */
    public Client() {
      try {
        session = newSession();
      } catch(final IOException ex) {
        ex.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
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
