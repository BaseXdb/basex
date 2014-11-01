package org.basex.examples.server;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.examples.local.*;

/**
 * This class demonstrates database access via the client/server architecture.
 * It shows how to add and modify files.
 * For further options see {@link QueryCollection}.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class ServerCollection {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== ServerCollection ===");

    // Start server
    System.out.println("\n* Start server.");
    BaseXServer server = new BaseXServer();

    // Create a client session with host name, port, user name and password
    try(ClientSession session = new ClientSession("localhost", 1984, "admin", "admin")) {

      System.out.println("\n* Create a Collection.");
      session.execute("CREATE DB input");

      // Add some 50 documents
      System.out.println("\n* Adding 50 documents");

      // XML fragment to add
      String xml1 = "<text version=\"draft\"><title>Chapter ";
      String xml2 = "</title></text>";

      for(int i = 0; i < 50; i++) {
        String path = "/book/chapters/" + i + "/Chapter-" + i + ".xml";
        add(session, path, xml1 + i + xml2);
      }
      // Add another Test Document in folder /book/chapters/0
      String path = "/book/chapters/0/Chapter-test.xml";
      add(session, path, xml1 + "test" + xml2);

      // Find some documents using the collection command
      find(session);

      // Modify specific document(s)
      System.out.println("\n* Modifying documents in folder /book/chapters/0:");
      modify(session);

      // Drop the database
      session.execute("DROP DB input");
    }

    // Stop the server
    System.out.println("\n* Stop server.");
    server.stop();
  }

  /**
   * This Methods performs a simple path based search in a collection.
   * @param session client session
   * @throws IOException I/O exception
   */
  private static void find(final ClientSession session) throws IOException {
    System.out.println("\n* Finding documents in folder /book/chapters/0:");
    System.out.println(session.execute(
        new XQuery(
         "for $doc in collection('input/book/chapters/0') " +
         "return $doc")));
  }

  /**
   * This method shows how to modify multiple documents at once.
   * It replaces the title of the matching documents with 1 2 3.
   * @param session client session
   * @throws IOException I/O exception
   */
  private static void modify(final ClientSession session) throws IOException {
    session.execute(new XQuery(
        "for $doc in collection('input/book/chapters/0/')" + " return "
            + "replace value of node $doc/text/title  "
            + "with (1 to 3)"));

    // Validate result
    System.out.println(session.execute(new XQuery(
        "for $doc in collection('input/book/chapters/0')" + " return $doc")));
  }

  /**
   * Adds a document to the collection.
   * @param target optional target path
   * @param session client session
   * @param xmlFragment XML Fragment
   * @throws IOException I/O exception
   */
  private static void add(final ClientSession session, final String target,
      final String xmlFragment) throws IOException {
    session.execute(new Add(target, xmlFragment));
  }
}
