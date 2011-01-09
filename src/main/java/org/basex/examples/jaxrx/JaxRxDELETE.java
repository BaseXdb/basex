package org.basex.examples.jaxrx;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.basex.api.jaxrx.JaxRxServer;

/**
 * This class is a simple Java client to demonstrate the JAX-RX implementation.
 * It shows the function of the HTTP DELETE method.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Lukas Lewandowski
 */
public final class JaxRxDELETE {
  /**
   * Constructor.
   * @throws IOException I/O exception
   */
  JaxRxDELETE() throws IOException {
    System.out.println("=== DELETE: delete a document or database ===");

    // The java URL connection to the resource
    URL url = new URL("http://localhost:8984/basex/jax-rx/factbook/input.xml");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Set as DELETE request
    conn.setRequestMethod("DELETE");

    // Print the HTTP response code
    System.out.println("\n* HTTP response: " + conn.getResponseCode() +
        " (" + conn.getResponseMessage() + ")");

    // The java URL connection to the resource
    url = new URL("http://localhost:8984/basex/jax-rx/factbook");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    conn = (HttpURLConnection) url.openConnection();
    // Set as DELETE request
    conn.setRequestMethod("DELETE");

    // Print the HTTP response code
    System.out.println("\n* HTTP response: " + conn.getResponseCode() +
        " (" + conn.getResponseMessage() + ")");

    // Close connection
    conn.disconnect();
  }

  /**
   * This method demonstrates the DELETE method. In this example, an XML
   * database is deleted.
   * @param args (ignored) command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String... args) throws IOException {
    // Start servers
    JaxRxServer jaxrx = new JaxRxServer();
    // Run example
    new JaxRxDELETE();
    // Stop servers
    jaxrx.stop();
  }
}
