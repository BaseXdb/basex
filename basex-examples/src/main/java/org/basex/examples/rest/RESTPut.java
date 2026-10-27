package org.basex.examples.rest;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.nio.file.*;

import org.basex.*;

/**
 * This class is a simple example to demonstrate the REST implementation.
 * It shows the function of the HTTP PUT method.
 *
 * @author BaseX Team, BSD License
 */
public final class RESTPut {
  /**
   * Runs the example.
   * @throws Exception exception
   */
  static void run() throws Exception {
    System.out.println("=== PUT: create a new database ===");

    URI uri = URI.create("http://localhost:8080/rest/factbook");
    System.out.println("\n* URL: " + uri);

    // Send the document to the server
    System.out.println("\n* Send document...");
    try(HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder(uri).
          PUT(BodyPublishers.ofFile(Path.of("src/main/resources/xml/factbook.xml"))).build();
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

      // Print the HTTP status code
      System.out.println("\n* HTTP status: " + response.statusCode());
      if(response.statusCode() >= 400) throw new IOException(response.body());
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
