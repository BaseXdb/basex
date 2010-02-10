package org.basex.test.examples;

import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.server.ClientSession;

/**
 * This class demonstrates database access via the
 * client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class ServerExample {
  /** Reference to the client session. */
  static ClientSession session;

  /** Private constructor. */
  private ServerExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== ServerExample ===");

    // ------------------------------------------------------------------------
    // Start server on default port 1984 in a new thread.
    // In a usual scenario, the server will only be run once
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

    session = new ClientSession("localhost", 1984, "admin", "admin");

    // ------------------------------------------------------------------------
    // Create a database
    System.out.println("\n* Create a database.");

    // Set an option: turn verbose processing information on
    send("SET INFO true");
    send("CREATE DB \"etc/xml/input.xml\" input");

    // ------------------------------------------------------------------------
    // Run a query
    System.out.println("\n* Run a query:");

    send("XQUERY //li");

    // ------------------------------------------------------------------------
    // Drop the database
    System.out.println("\n* Close and drop the database:");

    send("DROP DB input");

    // ------------------------------------------------------------------------
    // Close the client session
    System.out.println("\n* Close the client session.");

    session.close();

    // ------------------------------------------------------------------------
    // Stop the server
    System.out.println("\n* Stop the server:");

    new BaseXServer("STOP");
  }

  /**
   * Sends the specified command to the server and
   * returns the output or command info.
   * @param command command to be executed
   * @throws IOException I/O exception
   */
  static void send(final String command) throws IOException {
    // ------------------------------------------------------------------------
    // Execute the process
    session.execute(command, System.out);

    // ------------------------------------------------------------------------
    // If available, print process information or error output
    System.out.print(session.info());
  }
}
