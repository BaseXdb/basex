package org.basex.examples.rest;

import org.basex.api.BaseXHTTP;

/**
 * This class runs all REST examples.
 * It shows the function of the HTTP DELETE method.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class RESTAll {
  /**
   * This method demonstrates the available DELETE method. In this example, an
   * an XML database is deleted.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Start servers
    final BaseXHTTP http = new BaseXHTTP("-W -Uadmin -Padmin");

    // Create a database
    // Run all REST examples
    RESTPut.run();
    System.out.println();

    // Send a query via GET
    RESTGet.run();
    System.out.println();

    // Send a query via POST
    RESTPost.run();
    System.out.println();

    // Delete a document
    RESTDelete.run();
    System.out.println();

    // Stop servers
    http.stop();
  }
}
