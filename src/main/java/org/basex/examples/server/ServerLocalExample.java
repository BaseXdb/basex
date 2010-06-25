package org.basex.examples.server;

import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.examples.query.QueryExample;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;

/**
 * This class sends a query to a database server instance.
 * The query result is used to create and query a local database.
 * For more information on server functionalities, see {@link ServerExample}
 * For more information on local query processing, see {@link QueryExample}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class ServerLocalExample {
  /** Local database context. */
  static final Context CONTEXT = new Context();
  /** Reference to the client session. */
  static ClientSession session;

  /** Private constructor. */
  private ServerLocalExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== ServerLocalExample ===");

    // ------------------------------------------------------------------------
    // Start server
    new BaseXServer();

    // ------------------------------------------------------------------------
    // Create a client session with host name, port, user name and password
    System.out.println("\n* Create a client session.");

    session = new ClientSession("localhost", 1984, "admin", "admin");

    // -------------------------------------------------------------------------
    // Locally cache the result of a server-side query
    System.out.println("\n* Cache server-side query results.");

    CachedOutput result = new CachedOutput();
    send("XQUERY for $x in doc('etc/xml/input.xml') return $x", result);

    // -------------------------------------------------------------------------
    // Create a local database from the XML result string
    System.out.println("\n* Create a local database.");

    new CreateDB("LocalDB", result.toString()).execute(CONTEXT, System.out);

    // -------------------------------------------------------------------------
    // Run a query on the locally cached results
    System.out.println("\n* Run a local query:");

    new XQuery("//title").execute(CONTEXT, System.out);

    // ------------------------------------------------------------------------
    // Close the client session
    System.out.println("\n\n* Close the client session.");

    session.close();

    // ------------------------------------------------------------------------
    // Stop the server
    System.out.println("\n* Stop the server:");

    new BaseXServer("STOP");

    // ----------------------------------------------------------------------
    // Drop the local database
    System.out.println("\n* Drop the local database.");

    new DropDB("LocalDB").execute(CONTEXT);
  }

  /**
   * Processes the specified command on the server and writes the
   * response to out.
   * Command info is printed to System.out by default.
   * @param command command to be executed
   * @param out OutputStream to write to
   * @throws IOException I/O exception
   */
  private static void send(final String command, final CachedOutput out)
      throws IOException {

    // ------------------------------------------------------------------------
    // Execute the process
    session.execute(command, out);

    // ------------------------------------------------------------------------
    // If available, print process information or error output
    System.out.print(session.info());
  }
}
