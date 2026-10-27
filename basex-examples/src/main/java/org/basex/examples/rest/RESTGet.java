package org.basex.examples.rest;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.*;

import org.basex.*;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP GET method.
 *
 * @author BaseX Team, BSD License
 */
public final class RESTGet {
  /**
   * Runs the example.
   * @throws Exception exception
   */
  static void run() throws Exception {
    System.out.println("=== GET: execute a query ===");

    // Create the URL, with a URL-encoded query
    String query = URLEncoder.encode("(//city/name)[position() = 1 to 5]", StandardCharsets.UTF_8);
    URI uri = URI.create("http://localhost:8080/rest/factbook?query=" + query);
    System.out.println("\n* URL: " + uri);

    // Send the request
    try(HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder(uri).build();
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

      // Print the HTTP status code and the result
      System.out.println("\n* HTTP status: " + response.statusCode());
      if(response.statusCode() >= 400) throw new IOException(response.body());
      System.out.println("\n* Result:");
      System.out.println(response.body());
    }
  }

  /**
   * Main method.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    // Start the HTTP server with admin as default user, so requests need no credentials
    final BaseXHTTP http = new BaseXHTTP("-U", "admin");
    try {
      run();
    } finally {
      http.stop();
    }
  }
}
