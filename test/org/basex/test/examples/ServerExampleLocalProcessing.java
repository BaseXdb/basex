package org.basex.test.examples;

import org.basex.BaseXServer;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.QueryProcessor;
import org.basex.server.ClientSession;

/**
 * This class demonstrates how the database can be accessed via the
 * client/server architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */

public final class ServerExampleLocalProcessing {
  /** Session. */
  static ClientSession session;

  /** Private constructor. */
  private ServerExampleLocalProcessing() { }

  /** The current LOCAL database Context. */
  static final Context CONTEXT = new Context();
  
  /** Local database to process intermediate results. */
  private static final String LOCALDB = "ServerLocalProcessingExample1";

  /** XML Serializer. */
  private static XMLSerializer ser;
  
 /**
   * This class sets up a connection to a remote BaseX Server.
   * It then sends a query and stores the query results.
   * Then local BaseX functionality is used to process the query result.
   * For more information on BaseX server functionalities {@link ServerExample}
   * For more information on local query processing {@link QueryExample}.
   * @param args (ignored) command-line arguments
   * @throws Exception on error. 
   */
  public static void main(final String[] args) throws Exception {
    
    //set up serializer
    ser = new XMLSerializer(System.out);
    // Start server on port 16387 in a new thread.
    new Thread() {
      @Override
      public void run() {
        new BaseXServer();
      }
    }.start();

    // Wait for the thread to be started.
    Thread.sleep(1000);

    // Create client session, specifying a server name and port
    session = new ClientSession("localhost", 1984, "admin", "admin");
    
    // -------------------------------------------------------------------------
    // Create a stream to store the servers response in
    CachedOutput result = new CachedOutput();
    launch("xquery for $x in doc('input') return $x", result);

    // -------------------------------------------------------------------------
    // Create a new LOCAL database from the XML result string:    
    new CreateDB(result.toString(), LOCALDB).execute(CONTEXT, System.out);
    // -------------------------------------------------------------------------
    // 2) Query the newly created database:
    QueryProcessor qp = new QueryProcessor("//text()", CONTEXT);
    Nodes ns = qp.queryNodes(); 

    // Output all textual contents:
    ns.serialize(ser);
    
    // -------------------------------------------------------------------------
    System.out.println("\n\n=== Stop the server:");
    // Close the session.
    session.close();
    // Stop server instance.
    new BaseXServer("stop");
    
    // -------------------------------------------------------------------------
    // close & drop the LOCAL database
    new Close().execute(CONTEXT);
    new DropDB(LOCALDB).execute(CONTEXT);
  }
  
  /**
   * Processes the specified command on the server and returns the output
   * or command info verbosely by default.
   * @param cmd command to be executed
   * @param o OutputStream to write to
   * @throws Exception exception
   */
  private static void launch(final String cmd, 
      final CachedOutput o) throws Exception {
    // Execute the process.
    session.execute(cmd, o);
    o.flush();
   // Show optional process information.
    System.out.print(session.info());
  }

}
