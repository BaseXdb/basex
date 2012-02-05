package org.basex.examples.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.api.BaseXHTTP;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP DELETE method.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class RESTPost {
  /**
   * Runs the example.
   * @throws IOException I/O exception
   */
  static void run() throws IOException {
    System.out.println("=== POST: execute a query ===");

    // The java URL connection to the resource
    URL url = new URL("http://localhost:8984/rest/factbook");
    System.out.println("\n* URL: " + url);

    // Query to be sent to the server
    String request =
      "<query xmlns='http://basex.org/rest'>\n" +
      "  <text>(//city/name)[position() le 3]</text>\n" +
      "  <parameter name='wrap' value='yes'/>\n" +
      "</query>";
    System.out.println("\n* Query:\n" + request);

    // Establish the connection to the URL
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Set an output connection
    conn.setDoOutput(true);
    // Set as PUT request
    conn.setRequestMethod("POST");
    // Specify content type
    conn.setRequestProperty("Content-Type", "application/query+xml");

    // Get and cache output stream
    OutputStream out = conn.getOutputStream();

    // Send UTF-8 encoded query to server
    out.write(request.getBytes("UTF-8"));
    out.close();

    // Print the HTTP response code
    int code = conn.getResponseCode();
    System.out.println("\n* HTTP response: " + code +
        " (" + conn.getResponseMessage() + ')');

    // Check if request was successful
    if(code == HttpURLConnection.HTTP_OK) {
      // Print the received result to standard output (same as GET request)
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
    final BaseXHTTP http = new BaseXHTTP("-W", "-Uadmin", "-Padmin");
    // Run example
    run();
    // Stop servers
    http.stop();
  }
}
