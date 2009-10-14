package org.basex.test.examples;

import org.basex.BaseXServer;
import org.basex.io.*;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

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
  /** Output stream reference. */
  static PrintOutput out;

  /** Private constructor. */
  private ServerExample() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Start server in a new thread.
    new Thread() {
      @Override
      public void run() {
        new BaseXServer();
      }
    }.start();

    // Wait for the thread to be started.
    Performance.sleep(100);

    // Create client session, specifying the server name and port
    session = new ClientSession("localhost", 1984, "admin", "admin");

    // Create a standard output stream.
    out = new PrintOutput(System.out);

    // Alternative: write results to disk
    //out = new PrintOutput("result.txt");

    out.println("\n=== Create a database:");

    // Set an option: activate command info output.
    launch("set info true");
    // Create a database from the specified file.
    launch("create db \"input.xml\" input");

    out.println("\n=== Run a query:");

    // Create a database for the specified input.
    launch("xquery //li");

    out.println("\n=== Show database information:");

    // Create a database for the specified input.
    launch("info db");

    out.println("\n=== Close and drop the database:");

    // Close the database.
    launch("close");
    // Drop the database.
    launch("drop db input");

    out.println("\n=== Stop the server:");

    // Close the session.
    session.close();

    // Close the output stream
    out.close();

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
    if(session.execute(cmd)) {
      // Serialize the output if execution was successful.
      session.output(out);
    }
    // Show optional process information.
    out.print(session.info());
  }
}
