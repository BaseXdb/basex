package org.basex.examples.server;

import java.io.IOException;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

/**
 * This class performs a client/server stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class StressMemTest {
  /** Number of clients. */
  private static final int NCLIENTS = 10;
  /** Number of parallel readers. */
  private static final int PARALLEL = 1;
  /** Query to be run ("%" serves as placeholder for dynamic content). */
  private static final String QUERY =
    "(for $i in 1 to 2000000 order by $i return $i)[1]";

  /** Server reference. */
  static BaseXServer server;
  /** Random number generator. */
  static final Random RND = new Random();

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== Server StressMemTest ===");

    final Performance perf = new Performance();

    // Run server instance
    System.out.println("\n* Start server with " + PARALLEL + " readers.");
    server = new BaseXServer("-zcset parallel " + PARALLEL);

    // Run clients
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    final Client[] cl = new Client[NCLIENTS];
    for(int i = 0; i < NCLIENTS; ++i) cl[i] = new Client();
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();

    // Stop server
    System.out.println("\n* Stop server.");
    server.stop();

    System.out.println("\n* Time: " + perf);
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
        // Perform query
        session.execute("xquery " + QUERY);
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
