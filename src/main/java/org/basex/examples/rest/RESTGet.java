package org.basex.examples.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.api.BaseXHTTP;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP GET method.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class RESTGet {
  /**
   * Runs the example.
   * @throws IOException I/O exception
   */
  static void run() throws IOException {
    System.out.println("=== GET: execute a query ===");

    // The java URL connection to the resource
    String base = "http://localhost:8984/rest/";

    URL url = new URL(base + "factbook?query=(//city/name)[position()=1+to+5]");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // Print the HTTP response code
    int code = conn.getResponseCode();
    System.out.println("\n* HTTP response: " + code +
        " (" + conn.getResponseMessage() + ')');

    // Check if request was successful
    if(code == HttpURLConnection.HTTP_OK) {
      // Print the received result to standard output
      System.out.println("\n* Result:");

      // Get and cache input as UTF-8 encoded stream
      BufferedReader br = new BufferedReader(new InputStreamReader(
          conn.getInputStream(), "UTF-8"));

      // Print all lines of the result
      for(String line; (line = br.readLine()) != null;) {
        System.out.println(line);
      }
      br.close();
    }

    // Close connection
    conn.disconnect();
  }

  /**
   * Main method.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    // Start servers
    final BaseXHTTP http = new BaseXHTTP("-Uadmin", "-Padmin");
    // Run example
    run();
    // Stop servers
    http.stop();
  }
}
