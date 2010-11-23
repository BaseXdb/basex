package org.basex.examples.jaxrx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.basex.api.jaxrx.JaxRxServer;

/**
 * This class is a simple Java client to demonstrate the JAX-RX implementation.
 * It shows the function of the HTTP GET method.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 */
public final class JaxRxGET {
  /**
   * Constructor.
   * @throws IOException I/O exception
   */
  JaxRxGET() throws IOException {
    System.out.println("=== GET: execute a query ===");

    // The java URL connection to the resource
    String base = "http://localhost:8984/basex/jax-rx/";

    URL url = new URL(base + "factbook?query=//city/name&count=3");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // Print the HTTP response code
    int code = conn.getResponseCode();
    System.out.println("\n* HTTP response: " + code +
        " (" + conn.getResponseMessage() + ")");

    // Check if request was successful
    if(code == HttpURLConnection.HTTP_OK) {
      // Print the received result to standard output
      System.out.println("\n* Result:");

      // Get and cache input as UTF-8 encoded stream
      BufferedReader br = new BufferedReader(new InputStreamReader(
          conn.getInputStream(), "UTF-8"));

      // Print all lines of the result
      String line;
      while((line = br.readLine()) != null) {
        System.out.println(line);
      }
      br.close();
    }

    // Close connection
    conn.disconnect();
  }

  /**
   * This method demonstrates the GET method. In this example, a query on a
   * resource is processed.
   * @param args (ignored) command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String... args) throws IOException {
    // Start servers
    JaxRxServer jaxrx = new JaxRxServer();
    // Run example
    new JaxRxGET();
    // Stop servers
    jaxrx.stop();
  }
}
