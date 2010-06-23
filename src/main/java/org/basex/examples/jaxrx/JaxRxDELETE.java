package org.basex.examples.jaxrx;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is a simple Java client to demonstrate the JAX-RX implementation.
 * It shows the function of the HTTP DELETE method.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 */
public final class JaxRxDELETE {

  /** Private constructor. */
  private JaxRxDELETE() { }

  /**
   * This method demonstrates the available DELETE method. In this example, an
   * an XML database is deleted.
   * @param args (ignored) command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String[] args) throws IOException {

    System.out.println("=== DELETE request: delete a database ===");

    // The java URL connection to the resource.
    URL url = new URL("http://localhost:8984/basex/jax-rx/factbook");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL.
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Set as DELETE request.
    conn.setRequestMethod("DELETE");

    // Print the HTTP response code.
    System.out.println("\n* HTTP response: " + conn.getResponseCode() +
        " (" + conn.getResponseMessage() + ")");

    // Close connection.
    conn.disconnect();
  }
}
