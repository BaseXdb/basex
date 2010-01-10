package org.basex.test.examples;

import java.io.IOException;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

/**
 * This class connects multiple clients to one server instance.
 * It sets up three clients with read/write access to the database.
 * Database information will be shown before and after the
 * clients have been run.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class ServerConcurrencyExample {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new ServerConcurrencyExample().run();
  }

  /**
   * Runs the example code.
   * @throws Exception on error.
   */
  void run() throws Exception {

    System.out.println("=== ServerConcurrencyExample ===");

    // ------------------------------------------------------------------------
    // Start server in a new thread
    new Thread() {
      @Override
      public void run() {
        new BaseXServer();
      }
    }.start();

    // ------------------------------------------------------------------------
    // Wait some time for the server to be initialized
    Thread.sleep(1000);

    // ------------------------------------------------------------------------
    // Create a client session with host name, port, user name and password
    System.out.println("\n* Create a client session.");

    final ClientSession session =
      new ClientSession("localhost", 1984, "admin", "admin");

    // ------------------------------------------------------------------------
    // Create a database
    System.out.println("\n* Create a database.");

    // Set an option: turn verbose processing information on
    send("SET INFO true", session);
    send("CREATE DB \"input.xml\" input", session);

    // ------------------------------------------------------------------------
    // Setup some clients that simultaneously read and write from the database
    System.out.println("\n* Run one reader and two writers threads.");

    final ClientExample reader1 = new ClientExample("//li");
    final ClientExample writer1 = new ClientExample("insert node " +
        "<li>new node</li> as last into /html/body//ul");
    final ClientExample writer2 = new ClientExample("insert node " +
        "<new>One more</new> as last into /html/body");

    // -------------------------------------------------------------------------
    // Let the clients run their query several times:
    Performance.sleep(2000);

    // ------------------------------------------------------------------------
    // Stop the clients
    System.out.println("\n\n* Stop client threads.");

    reader1.stop();
    writer1.stop();
    writer2.stop();

    Performance.sleep(1000);

    // -------------------------------------------------------------------------
    // Show modified database contents
    System.out.println("\n* Show modified database contents:");

    send("XQUERY //li", session);

    // ----------------------------------------------------------------------
    // Drop the database
    System.out.println("\n* Drop the database.");

    send("DROP DB input", session);

    // ------------------------------------------------------------------------
    // Close the client session
    System.out.println("\n* Close the client session.");

    session.close();

    // ------------------------------------------------------------------------
    // Stop the server.
    System.out.println("\n* Stop the server:");

    new BaseXServer("STOP");
  }

  /**
   * Processes the specified command on the server and returns the output
   * or command info.
   * @param cmd command to be executed
   * @param cs client session reference
   * @throws IOException I/O exception
   */
  void send(final String cmd, final ClientSession cs) throws IOException {
    // ------------------------------------------------------------------------
    // Execute the process.
    cs.execute(cmd, System.out);
  }


  /**
   * This class simulates a database client.
   * Within a thread, a query is run several times until the thread is stopped.
   */
  final class ClientExample {
    /** Set to false to stop the Client from running.*/
    boolean running = true;
    /** Random sleep time generator.*/
    final Random r = new Random();

    /**
     * Constructor.
     * @param query query to be run
     * @throws IOException I/O exception
     */
    ClientExample(final String query) throws IOException {
      // ----------------------------------------------------------------------
      // Create a client session and open database
      final ClientSession session =
        new ClientSession("localhost", 1984, "admin", "admin");
      send("OPEN input", session);

      // ----------------------------------------------------------------------
      // Create and run the query thread

      new Thread() {
        @Override
        public void run() {
          try {
            // ----------------------------------------------------------------
            // Loop until the running flag has been invalidated
            while(running) {
              // Run the specified query
              send("XQUERY " + query, session);

              // Wait for a random time
              Thread.sleep(r.nextInt(500));
            }
            // ----------------------------------------------------------------
            // Close the client session
            session.close();
          } catch(final Exception ex) {
            ex.printStackTrace();
          }
        }
      }.start();
    }

    /**
     * Quits the client thread.
     */
    public void stop() {
      running = false;
    }
  }
}
