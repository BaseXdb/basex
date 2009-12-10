package org.basex.test.examples;

import org.basex.BaseXServer;
import org.basex.server.ClientSession;

/**
 * This class demonstrates how the database can be accessed via the
 * client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class ServerExample {
  /** Session. */
  static ClientSession session;

  /** Private constructor. */
  private ServerExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Start server on port 16387 in a new thread.
    new Thread() {
      @Override
      public void run() {
        new BaseXServer();
      }
    }.start();

    // Wait for the thread to be started.
    Thread.sleep(100);

    // Create client session, specifying a server name and port
    session = new ClientSession("localhost", 1984, "admin", "admin");

    System.out.println("\n=== Create a database:");

    // Set an option: shows command info output.
    launch("set info true");
    // Create a database from the specified file.
    launch("create db \"input.xml\" input");

    System.out.println("\n=== Run a query:");

    // Create a database for the specified input.
    launch("xquery //li");

    System.out.println("\n=== Show database information:");

    // Create a database for the specified input.
    launch("info db");

    System.out.println("\n=== Close and drop the database:");

    // Close the database.
    launch("close");
    // Drop the database.
    launch("drop db input");

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
   * @throws Exception exception
   */
  private static void launch(final String cmd) throws Exception {
    // Execute the process.
    session.execute(cmd, System.out);
    // Show optional process information.
    System.out.print(session.info());
  }
}
