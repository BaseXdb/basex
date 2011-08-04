package org.basex.examples.jaxrx;

import org.basex.api.jaxrx.JaxRxServer;

/**
 * This class runs all JAX-RX examples.
 * It shows the function of the HTTP DELETE method.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class JaxRxAll {
  /**
   * This method demonstrates the available DELETE method. In this example, an
   * an XML database is deleted.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Start servers
    final JaxRxServer jaxrx = new JaxRxServer();

    // Create a database
    // Run all JAX-RX examples
    JaxRxPUT.run();
    System.out.println();

    // Send a query via GET
    JaxRxGET.run();
    System.out.println();

    // Send a query via POST
    JaxRxPOSTQuery.run();
    System.out.println();

    // Add a document
    JaxRxPOSTAdd.run();
    System.out.println();

    // Delete a document
    JaxRxDELETE.run();
    System.out.println();

    // Stop servers
    jaxrx.stop();
  }
}
