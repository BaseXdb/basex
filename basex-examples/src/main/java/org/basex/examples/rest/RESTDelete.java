package org.basex.examples.rest;

import java.io.*;
import java.net.*;

import org.basex.*;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP DELETE method.
 *
 * @author BaseX Team, BSD License
 */
public final class RESTDelete {
  /**
   * Runs the example.
   * @throws IOException I/O exception
   */
  static void run() throws IOException {
    System.out.println("=== DELETE: delete a document or database ===");

    // The java URL connection to the resource
    URL url = URI.create("http://admin:admin@localhost:8080/rest/factbook/input.xml").toURL();
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Set as DELETE request
    conn.setRequestMethod("DELETE");

    // Print the HTTP response code
    System.out.println("\n* HTTP response: " + conn.getResponseCode() +
        " (" + conn.getResponseMessage() + ')');

    // The java URL connection to the resource
    url = URI.create("http://admin:admin@localhost:8080/rest/factbook").toURL();
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    conn = (HttpURLConnection) url.openConnection();
    // Set as DELETE request
    conn.setRequestMethod("DELETE");

    // Print the HTTP response code
    System.out.println("\n* HTTP response: " + conn.getResponseCode() +
        " (" + conn.getResponseMessage() + ')');

    // Close connection
    conn.disconnect();
  }

  /**
   * Main method.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    // Start server, set admin as default user
    final BaseXHTTP http = new BaseXHTTP("-U", "admin");
    // Run example
    run();
    // Stop server
    http.stop();
  }
}
