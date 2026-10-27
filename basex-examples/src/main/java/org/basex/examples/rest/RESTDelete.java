package org.basex.examples.rest;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.util.*;

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
   * @throws Exception exception
   */
  static void run() throws Exception {
    System.out.println("=== DELETE: delete a document or database ===");

    try(HttpClient client = HttpClient.newHttpClient()) {
      // Delete a single document, and the database
      for(String url : List.of(
        "http://localhost:8080/rest/factbook/factbook.xml",
        "http://localhost:8080/rest/factbook"
      )) {
        URI uri = URI.create(url);
        System.out.println("\n* URL: " + uri);
        HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        // Print the HTTP status code and the info message
        System.out.println("\n* HTTP status: " + response.statusCode());
        if(response.statusCode() >= 400) throw new IOException(response.body());
        System.out.println(response.body());
      }
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
