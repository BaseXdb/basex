package org.basex.examples.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import org.basex.BaseXServer;
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
   * @throws IOException exception
   */
  public static void main(final String[] args) throws IOException {
    System.out.println("=== Server StressTest ===");

    // run server instance
    System.out.println("\n* Start server.");
    new BaseXServer("-z");

    // create test database
    System.out.println("\n* Create test database.");

    final ClientSession cs = newSession();
    cs.execute("set info on");
    cs.execute("create db factbook etc/xml/factbook.xml");
    System.out.print(cs.info());
    cs.close();

    // run clients
    System.out.println("\n* Run " + NCLIENTS + " client threads.");
    for(int i = 0; i < NCLIENTS; i++) {
      new Client().start();
    }
  }

  /**
   * Stops the server.
   * @throws IOException exception
   */
  static void stopServer() throws IOException {
    // drop database and stop server
    System.out.println("\n* Stop server and drop test database.");

    final ClientSession cs = newSession();
    cs.execute("drop db factbook");
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
        for(int i = 0; i < NQUERIES; i++) {
          Performance.sleep((long) (50 * RND.nextDouble()));

          // return nth text of the database
          final int n = (RND.nextInt() & 0xFF) + 1;
          final String qu = "xquery basex:db('factbook')/descendant::text()" +
            "[position() = " + n + "]";

          final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          final String result = session.execute(qu, buffer) ?
              buffer.toString() : session.info();

          if(VERBOSE) System.out.print("[" + counter + "] Thread " +
              getId() + ", Pos " + n + ": " + result);
          counter++;
        }
        session.close();

        // server is stopped after last client has finished
        if(++finished == StressTest.NCLIENTS) stopServer();
      } catch(final IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
