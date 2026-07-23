package org.basex.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.basex.core.*;
import org.basex.http.web.*;
import org.junit.jupiter.api.*;

/**
 * Base class for tests of web applications: deploys a module tree into the sandbox webapp,
 * starts the HTTP server and sends requests via a session-aware client.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class WebappTest extends HTTPTest {
  /** Credentials of the sandbox admin user (for servlets without a default user). */
  private static final String CREDENTIALS = "Basic " + Base64.getEncoder().
      encodeToString(("admin:" + NAME).getBytes(StandardCharsets.UTF_8));

  /** Root URL of the deployed web application. */
  private static String root;
  /** Session-aware HTTP client (keeps cookies of a login). */
  private static HttpClient client;

  // INITIALIZATION ===============================================================================

  /**
   * Deploys a web application and starts the server.
   * @param dir directory of the module tree, relative to the webapp root
   * @throws Exception exception
   */
  protected static void init(final String dir) throws Exception {
    // deploy the modules into the sandbox webapp, keeping the JVM-global RESTXQ path unchanged
    deploy(dir);
    init(HTTP_ROOT, true);
    // invalidate a stale module cache left by another test class
    clearCache();

    root = HTTP_ROOT + dir + '/';
    client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).
        cookieHandler(new CookieManager()).build();
  }

  /**
   * Clears the module cache so subsequent test classes do not see the deployed modules.
   */
  @AfterAll public static void clearCache() {
    if(context != null) WebModules.get(context).init(false);
  }

  /**
   * Copies a module tree into the sandbox webapp.
   * @param dir directory of the module tree, relative to the webapp root
   * @throws IOException I/O exception
   */
  private static void deploy(final String dir) throws IOException {
    File src = new File("src/main/webapp/" + dir);
    if(!src.exists()) src = new File("basex-api/src/main/webapp/" + dir);
    final Path from = src.toPath();
    final Path to = new File(context.soptions.get(StaticOptions.WEBPATH), dir).toPath();
    try(Stream<Path> paths = Files.walk(from)) {
      for(final Path path : (Iterable<Path>) paths::iterator) {
        final Path dest = to.resolve(from.relativize(path).toString());
        if(Files.isDirectory(path)) {
          Files.createDirectories(dest);
        } else {
          Files.createDirectories(dest.getParent());
          Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  // PROTECTED METHODS ============================================================================

  /**
   * Sends a GET request.
   * @param path path relative to the application root
   * @return response body
   * @throws IOException I/O exception
   */
  protected static String get(final String path) throws IOException {
    return text(send(200, "GET", path, null, null));
  }

  /**
   * Sends a POST request with form parameters.
   * @param path path relative to the application root
   * @param form form parameters
   * @return response body (after redirects)
   * @throws IOException I/O exception
   */
  protected static String post(final String path, final Map<String, String> form)
      throws IOException {
    final StringJoiner sj = new StringJoiner("&");
    form.forEach((k, v) -> sj.add(enc(k) + '=' + enc(v)));
    return text(send(200, "POST", path, sj.toString(), "application/x-www-form-urlencoded"));
  }

  /**
   * Sends a POST request with a raw text body.
   * @param path path relative to the application root
   * @param body request body
   * @return response body
   * @throws IOException I/O exception
   */
  protected static String post(final String path, final String body) throws IOException {
    return text(send(200, "POST", path, body, "text/plain; charset=utf-8"));
  }

  /**
   * Sends a request without additional headers and checks the status code.
   * @param status expected status code
   * @param method HTTP method
   * @param path path relative to the application root
   * @param body request body ({@code null} to omit)
   * @param type content type ({@code null} to omit)
   * @return response
   * @throws IOException I/O exception
   */
  protected static HttpResponse<String> send(final int status, final String method,
      final String path, final String body, final String type) throws IOException {
    return send(status, method, path, body, type, Map.of());
  }

  /**
   * Sends a request and checks the status code.
   * @param status expected status code
   * @param method HTTP method
   * @param path path relative to the application root
   * @param body request body ({@code null} to omit)
   * @param type content type ({@code null} to omit)
   * @param headers additional request headers
   * @return response
   * @throws IOException I/O exception
   */
  protected static HttpResponse<String> send(final int status, final String method,
      final String path, final String body, final String type, final Map<String, String> headers)
      throws IOException {
    final HttpResponse<String> response = send(method, path, body, type, headers);
    assertEquals(status, response.statusCode(), method + ' ' + path + '\n' + response.body());
    return response;
  }

  /**
   * Sends a request with the session client.
   * @param method HTTP method
   * @param path path relative to the application root
   * @param body request body ({@code null} to omit)
   * @param type content type ({@code null} to omit)
   * @param headers additional request headers
   * @return response
   * @throws IOException I/O exception
   */
  protected static HttpResponse<String> send(final String method, final String path,
      final String body, final String type, final Map<String, String> headers)
      throws IOException {

    final HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(root + path));
    rb.header("Authorization", CREDENTIALS);
    if(type != null) rb.header("Content-Type", type);
    headers.forEach(rb::header);
    rb.method(method, body == null ? BodyPublishers.noBody() :
      BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    try {
      return client.send(rb.build(), BodyHandlers.ofString());
    } catch(final InterruptedException ex) {
      throw new IOException(ex);
    }
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns the body of a response with normalized line endings.
   * @param response response
   * @return body
   */
  private static String text(final HttpResponse<String> response) {
    return response.body().replace("\r", "");
  }

  /**
   * URL-encodes a string.
   * @param string string
   * @return encoded string
   */
  private static String enc(final String string) {
    return URLEncoder.encode(string, StandardCharsets.UTF_8);
  }
}
