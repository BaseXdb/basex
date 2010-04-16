package org.basex.examples.jaxrx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is a simple Java client to demonstrate the JAX-RX implementation.
 * It shows the function of the HTTP GET method.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski, University of Konstanz
 */
public final class JaxRxGET {

  /** Private constructor. */
  private JaxRxGET() { }

  /**
   * This method demonstrates the available GET method. In this example,
   * a query on a resource is processed.
   *
   * @param args (ignored) command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String[] args) throws IOException {

    System.out.println("=== GET request: process a query ===");

    // The java URL connection to the resource.
    URL url = new URL(
      "http://localhost:8984/basex/jax-rx/factbook?query=//city/name&count=5");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL.
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // Print the HTTP response code.
    int code = conn.getResponseCode();
    System.out.println("\n* HTTP response: " + code +
        " (" + conn.getResponseMessage() + ")");

    // Check if request was successful.
    if(code == HttpURLConnection.HTTP_OK) {
      // Print the received result to standard output.
      System.out.println("\n* Result:");

      // Get and cache input as UTF-8 encoded stream.
      BufferedReader br = new BufferedReader(new InputStreamReader(
          conn.getInputStream(), "UTF-8"));

      // Print all lines of the result.
      String line;
      while((line = br.readLine()) != null) {
        System.out.println(line);
      }
      br.close();
    }

    // Close connection.
    conn.disconnect();
  }
}
