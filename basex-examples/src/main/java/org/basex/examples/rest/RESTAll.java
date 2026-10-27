package org.basex.examples.rest;

import org.basex.*;

/**
 * This class runs all REST examples.
 *
 * @author BaseX Team, BSD License
 */
public final class RESTAll {
  /**
   * Runs all examples.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    // Start the HTTP server with admin as default user, so requests need no credentials
    final BaseXHTTP http = new BaseXHTTP("-U", "admin");
    try {
      // Create a database
      RESTPut.run();
      System.out.println();
      // Send a query via GET
      RESTGet.run();
      System.out.println();
      // Send a query via POST
      RESTPost.run();
      System.out.println();
      // Delete a document and the database
      RESTDelete.run();
      System.out.println();
    } finally {
      http.stop();
    }
  }
}
