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
 * @author BaseX Team, BSD License
 */
public final class ServerCollection {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    System.out.println("=== ServerCollection ===");

    // Start a server with a known admin password
    System.out.println("\n* Start server.");
    BaseXServer server = new BaseXServer("-c", "PASSWORD admin");

    // Create a client session with host name, port, username and password
    System.out.println("\n* Create a client session.");
    try(ClientSession session = new ClientSession("localhost", 1984, "admin", "admin")) {
      // Create an empty collection
      System.out.println("\n* Create a collection.");
      session.execute(new CreateDB("input"));

      // Add 50 documents
      System.out.println("\n* Add 50 documents.");
      for(int i = 0; i < 50; i++) {
        add(session, "book/chapters/" + i + "/Chapter-" + i + ".xml", Integer.toString(i));
      }
      // Add another document to the folder book/chapters/0
      add(session, "book/chapters/0/Chapter-test.xml", "test");

      // Find documents in a folder
      String docs = "collection('input/book/chapters/0')";
      System.out.println("\n* Find documents in folder book/chapters/0:");
      System.out.println(session.execute(new XQuery(docs)));

      // Modify all documents in a folder
      System.out.println("\n* Modify documents in folder book/chapters/0:");
      session.execute(new XQuery(
          "for $doc in " + docs + " return " +
          "replace value of node $doc/text/title with 'Chapter 0'"));
      System.out.println(session.execute(new XQuery(docs)));

      // Drop the database
      session.execute(new DropDB("input"));
    } finally {
      // Stop the server
      System.out.println("\n* Stop server.");
      server.stop();
    }
  }

  /**
   * Adds a document to the collection.
   * @param session client session
   * @param path target path
   * @param title chapter title
   * @throws IOException I/O exception
   */
  private static void add(final ClientSession session, final String path, final String title)
      throws IOException {
    session.execute(new Add(path, "<text version='draft'><title>Chapter " + title +
        "</title></text>"));
  }
}
