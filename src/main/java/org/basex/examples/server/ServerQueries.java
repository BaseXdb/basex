package org.basex.examples.server;

import org.basex.BaseXServer;
import org.basex.server.ClientSession;
import org.basex.server.ClientQuery;

/**
 * This class demonstrates query execution via the client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class ServerQueries {
  /** Session reference. */
  private static ClientSession session;

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== ServerQueryExample ===\n");

    // ------------------------------------------------------------------------
    // Start server
    System.out.println("* Start server.");

    BaseXServer server = new BaseXServer();

    // ------------------------------------------------------------------------
    // Create a client session with host name, port, user name and password
    System.out.println("* Create a client session.");

    session = new ClientSession("localhost", 1984, "admin", "admin");

    // ------------------------------------------------------------------------
    // Run a query
    System.out.println("* Run a query:");

    System.out.println(session.execute("XQUERY 1"));

    // ------------------------------------------------------------------------
    // Run a query, specifying an output stream
    System.out.println("* Run a query (faster):");
    session.setOutputStream(System.out);

    session.execute("XQUERY 1 to 2");
    System.out.println();

    // Reset output stream
    session.setOutputStream(null);

    // ------------------------------------------------------------------------
    // Iteratively run a query
    System.out.println("* Iterate a query:");

    // Create query instance
    ClientQuery query = session.query("1 to 3");

    // Loop through all results
    while(query.more()) {
      System.out.print(query.next() + " ");
    }
    System.out.println();

    // Close iterator
    query.close();

    // ------------------------------------------------------------------------
    // Iteratively run a query, specifying an output stream
    System.out.println("* Iterate a query (faster):");
    session.setOutputStream(System.out);

    // Create query instance
    query = session.query("1 to 4");

    // Loop through all results (faster)
    while(query.more()) {
      query.next();
      System.out.print(" ");
    }
    System.out.println();

    // Close iterator
    query.close();

    // Reset output stream
    session.setOutputStream(null);

    // ------------------------------------------------------------------------
    // Close the client session
    System.out.println("* Close the client session.");

    session.close();

    // ------------------------------------------------------------------------
    // Stop the server
    System.out.println("* Stop the server.");

    server.stop();
  }
}
