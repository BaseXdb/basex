package org.basex.examples.server;

import org.basex.*;
import org.basex.api.client.*;

/**
 * This class demonstrates query execution via the client/server architecture.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class ServerQueries {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    System.out.println("=== ServerQueries ===");

    // Start server
    System.out.println("\n* Start server.");

    BaseXServer server = new BaseXServer();

    // Create a client session with host name, port, user name and password
    System.out.println("\n* Create a client session.");

    try(ClientSession session = new ClientSession("localhost", 1984, "admin", "admin")) {

      // Run a query
      System.out.print("\n* Run a query: ");

      System.out.println(session.execute("XQUERY 1"));

      // Run a query, specifying an output stream
      System.out.print("\n* Run a query (faster): ");
      session.setOutputStream(System.out);

      session.execute("XQUERY 1 to 2");
      System.out.println();

      // Reset output stream
      session.setOutputStream(null);

      // Run a query, specifying an output stream (second version)
      System.out.print("\n* Use query instance: ");

      session.execute("set queryinfo on");

      try(ClientQuery query = session.query("1 to 3")) {
        System.out.println(query.execute());

        System.out.print("\n* Show query info: \n");
        System.out.println(query.info());
      }

      // Iteratively run a query
      System.out.print("\n* Iterate over results: ");

      try(ClientQuery query = session.query("1 to 4")) {
        while(query.more()) {
          System.out.print(query.next() + ' ');
        }
        System.out.println();
      }

      // Bind a variable
      System.out.print("\n* Bind variable $a: ");

      session.execute("set queryinfo on");

      try(ClientQuery query = session.query("declare variable $a as xs:int external; 1 to $a")) {
        query.bind("$a", "5");

        System.out.println(query.execute());
      }

      // Close the client session
      System.out.println("\n* Close the client session.");
    }

    // Stop the server
    System.out.println("\n* Stop the server.");

    server.stop();
  }
}
