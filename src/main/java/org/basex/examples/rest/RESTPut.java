package org.basex.examples.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.basex.api.BaseXHTTP;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP PUT method.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class RESTPut {
  /**
   * Runs the example.
   * @throws IOException I/O exception
   */
  static void run() throws IOException {
    System.out.println("=== PUT: create a new database ===");

    // The java URL connection to the resource
    URL url = new URL("http://localhost:8984/rest/factbook");
    System.out.println("\n* URL: " + url);

    // Establish the connection to the URL
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // Set an output connection
    conn.setDoOutput(true);
    // Set as PUT request
    conn.setRequestMethod("PUT");

    // Get and cache output stream
    OutputStream out = new BufferedOutputStream(conn.getOutputStream());
    // Create and cache file input stream
    InputStream in = new BufferedInputStream(
        new FileInputStream("src/main/resources/xml/factbook.xml"));

    // Send document to server
    System.out.println("\n* Send document...");
    for(int i; (i = in.read()) != -1;) out.write(i);
    in.close();
    out.close();

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
    // Start servers
    final BaseXHTTP http = new BaseXHTTP("-Uadmin", "-Padmin");
    // Run example
    run();
    // Stop servers
    http.stop();
  }
}
