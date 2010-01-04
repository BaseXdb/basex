package org.basex.test.examples;

import java.io.OutputStream;
import java.util.Random;
import org.basex.BaseXServer;
import org.basex.io.BufferedOutput;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

/**
 * This class demonstrates how multiple clients connect to one server instance.
 * It sets up 3 clients with RW access to the database.
 * Database information will be shown before and after the clients have run.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class ServerExampleConcurrency {
  /** The Client Session to talk to the server. */
  protected static ClientSession session;

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    ServerExampleConcurrency serverExample = new ServerExampleConcurrency();
    serverExample.run();
  }

  /**
   * Runs the Client-Server Example.
   * @throws Exception on error.
   */
  public void run() throws Exception {
    // Start server on standard port 1984 in a new thread.
    new Thread() {
      @Override
      public void run() {
        new BaseXServer();
      }
    }.start();

    // Wait for the server thread to be started.
    Thread.sleep(1000);

    // -------------------------------------------------------------------------
    // Create client session, specifying a server name and port
    session = new ClientSession("localhost", 1984, "admin", "admin");


    // -------------------------------------------------------------------------
    // Set an option: shows command info output.
    launch("set info true");

    // -------------------------------------------------------------------------
    // Create a database from the specified file.
    System.out.println("\n=== Create a database:");
    launch("create db \"input.xml\" input");

    System.out.println("\n=== Run a query:");
    launch("xquery //li", true);


    // -------------------------------------------------------------------------
    // Show general database information.
    System.out.println("\n=== Show database information:");
    launch("info db");


    // -------------------------------------------------------------------------
    // Setup some clients that simultaneously read and write from the databse.
    Client reader = new Client("//li", false);
    Client writer = new Client("insert node <li>One more </li> " +
        "as last into /html/body//ul",
         false);

    Client writer2 = new Client("insert node <strong>One more </strong>" +
        " as last into /html/body",
          false);

    // -------------------------------------------------------------------------
    // Let the clients run the query several times:
    Performance.sleep(2000);
    // -------------------------------------------------------------------------
    // Let the clients quit.
    writer.quit();
    writer2.quit();
    reader.quit();
    Performance.sleep(400);

    // -------------------------------------------------------------------------
    // Show info and close the database.
    System.out.println("\n=== Information on the changed database:");
    launch("info db");
    System.out.println("\n=== Contents of the changed database:");
    launch("xquery //li");

    System.out.println("\n=== Close and drop the database:");
    launch("close", false);
    // Drop the database.
    launch("drop db input", false);

    System.out.println("\n=== Stop the server:");

    // Close the session.
    session.close();

    // Stop server instance.
    new BaseXServer("stop");
  }

  /**
   * Processes the specified command on the server and returns the output
   * or command info.
   * @param cmd command to be executed
   * @param verbose show verbose output
   * @throws Exception exception
   */
  void launch(final String cmd, final boolean verbose) throws Exception {
    // Execute the process.
    OutputStream out = verbose ? System.out : new BufferedOutput(System.out);
    session.execute(cmd, out);
  }

  /**
   * Processes the specified command on the server and returns the output
   * or command info verbosely by default.
   * @param cmd command to be executed
   * @throws Exception exception
   */
  void launch(final String cmd) throws Exception {
    launch(cmd, true);
  }

  /**
   * Simulates a database client. In this example the client(s) share a
   * ClientSession in the static variable ({@link ServerExample#session}). The
   * query is scheduled to launch after max 500ms.
   */
  public final class Client implements Runnable {
    /** Set to false to stop the Client from running.*/
    private boolean running = true;

    /** Query to execute. */
    private String query;

    /** Verbose output. */
    private boolean verbose;

    /** Random sleep time generator.*/
    private final Random r = new Random();

    @Override
    public void run() {
      int i = 1;
      while(true) {
        if(!running) return;
        try {
          launch("xquery " + this.query, verbose);
          i++;
        } catch(Exception e) {
          e.printStackTrace();
        }
        Performance.sleep(r.nextInt(500));
      }
    }

    /**
     * Public constructor starts the Client thread.
     * @param q Query for the client.
     * @param v Verbose output.
     */
    public Client(final String q, final boolean v) {
      this.query = q;
      this.verbose = v;
      new Thread(this).start();
    }

    /**
     * Quits the Client thread.
     */
    public void quit() {
      this.running = false;
    }
  }
}
