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
    new JaxRxPUT();
    System.out.println();

    // Send a query via GET
    new JaxRxGET();
    System.out.println();

    // Send a query via POST
    new JaxRxPOSTQuery();
    System.out.println();

    // Add a document
    new JaxRxPOSTAdd();
    System.out.println();

    // Delete a document
    new JaxRxDELETE();
    System.out.println();

    // Stop servers
    jaxrx.stop();
  }
}
