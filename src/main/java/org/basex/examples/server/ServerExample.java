package org.basex.examples.server;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;

/**
 * This class demonstrates database access via the client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class ServerExample {
  /** Session reference. */
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
    // Start server on default port 1984.
    BaseXServer server = new BaseXServer();

    // ------------------------------------------------------------------------
    // Create a client session with host name, port, user name and password
    System.out.println("\n* Create a client session.");

    session = new ClientSession("localhost", 1984, "admin", "admin");

    // ------------------------------------------------------------------------
    // Create a database
    System.out.println("\n* Create a database.");

    session.execute("CREATE DB input etc/xml/input.xml");

    // ------------------------------------------------------------------------
    // Run a query
    System.out.println("\n* Run a query:");

    System.out.println(session.execute("XQUERY //li"));

    // ------------------------------------------------------------------------
    // Run a query, specifying an output stream
    System.out.println("\n* Run a query (faster):");

    session.execute("XQUERY //li", System.out);

    // ------------------------------------------------------------------------
    // Run a query
    System.out.println("\n\n* Run a buggy query:");

    try {
      session.execute("XQUERY ///");
    } catch(final BaseXException ex) {
      System.out.println(ex.getMessage());
    }

    // ------------------------------------------------------------------------
    // Drop the database
    System.out.println("\n* Close and drop the database.");

    session.execute("DROP DB input");

    // ------------------------------------------------------------------------
    // Close the client session
    System.out.println("\n* Close the client session.");

    session.close();

    // ------------------------------------------------------------------------
    // Stop the server
    System.out.println("\n* Stop the server.");

    server.stop();
  }
}
