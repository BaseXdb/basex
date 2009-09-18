package org.basex.test.examples;

import org.basex.BaseXServer;
import org.basex.core.*;
import org.basex.core.Process;
import org.basex.core.proc.*;
import org.basex.io.*;
import org.basex.server.ClientLauncher;
import org.basex.util.Performance;

/**
 * This class demonstrates how the database can be accessed via the
 * client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class ServerExample {
  /** Database context. */
  static Context context;
  /** Output stream reference. */
  static PrintOutput out;
  /** Process launcher. */
  static ClientLauncher launcher;

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

    // Create a new database context, referencing the database.
    context = new Context();
    // Create command launcher
    launcher = new ClientLauncher(context);
    // Create a standard output stream.
    out = new PrintOutput(System.out);

    out.println("\n=== Create a database:");

    // Set an option: activate command info output.
    launch(new Set("info", true));
    // Create a database from the specified file.
    launch(new CreateDB("input.xml", "input"));

    out.println("\n=== Run a query:");

    // Create a database for the specified input.
    launch(new XQuery("//li"));

    out.println("\n=== Show database information:");

    // Create a database for the specified input.
    launch(new InfoDB());

    out.println("\n=== Close and drop the database:");

    // Close the database.
    launch(new Close());
    // Drop the database.
    launch(new DropDB("input"));

    out.println("\n=== Stop the server:");

    // Stop server instance.
    new BaseXServer("stop");
  }

  /**
   * Processes the specified command on the server and returns the output
   * or command info.
   * @param proc process to be executed
   * @throws Exception exception
   */
  private static void launch(final Process proc) throws Exception {
    // Execute the process.
    if(launcher.execute(proc)) {
      // Serialize the output if execution was successful.
      launcher.output(out);
    }
    // Show process information.
    launcher.info(out);
  }
}
