package org.basex.examples.rest;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;

import org.basex.*;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP POST method.
 *
 * @author BaseX Team, BSD License
 */
public final class RESTPost {
  /**
   * Runs the example.
   * @throws Exception exception
   */
  static void run() throws Exception {
    System.out.println("=== POST: execute a query ===");

    URI uri = URI.create("http://localhost:8080/rest/factbook");
    System.out.println("\n* URL: " + uri);

    // Query to be sent to the server
    String body = """
        <query xmlns='http://basex.org/rest'>
          <text>(//city/name)[position() = 1 to 3]</text>
        </query>""";
    System.out.println("\n* Query:\n" + body);

    // Send the request
    try(HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder(uri).
          header("Content-Type", "application/query+xml").
          POST(BodyPublishers.ofString(body)).build();
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
