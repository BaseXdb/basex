package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.junit.jupiter.api.*;

/**
 * Base class for DBA tests: deploys the DBA into the sandbox webapp and holds a login session.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class DBATest extends HTTPTest {
  /** DBA root path. */
  private static final String DBA = "dba/";
  /** Session-aware HTTP client (keeps the DBA login cookie). */
  private static HttpClient client;

  /**
   * Deploys the DBA, starts the server and logs in.
   * @throws Exception exception
   */
  @BeforeAll public static void startDBA() throws Exception {
    // deploy the DBA into the sandbox webapp, keeping the JVM-global RESTXQ path unchanged
    deploy();
    init(HTTP_ROOT, true);
    // invalidate a stale module cache left by another test class
    WebModules.get(context).init(false);

    client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).
        cookieHandler(new CookieManager()).build();
    final String page = post("login", Map.of("_name", "admin", "_pass", NAME));
    assertFalse(page.contains("_pass"), "DBA login failed");
  }

  /**
   * Clears the module cache so subsequent test classes do not see the deployed DBA modules.
   */
  @AfterAll public static void resetDBA() {
    if(context != null) WebModules.get(context).init(false);
  }

  /**
   * Copies the DBA module tree into the sandbox webapp.
   * @throws IOException I/O exception
   */
  private static void deploy() throws IOException {
    File src = new File("src/main/webapp/dba");
    if(!src.exists()) src = new File("basex-api/src/main/webapp/dba");
    final Path from = src.toPath();
    final Path to = new File(context.soptions.get(StaticOptions.WEBPATH), "dba").toPath();
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

  /**
   * Sends a GET request to a DBA page.
   * @param path path relative to the DBA root
   * @return response body
   * @throws IOException I/O exception
   */
  protected static String get(final String path) throws IOException {
    return run("GET", path, HttpRequest.BodyPublishers.noBody(), null);
  }

  /**
   * Sends a POST request with form parameters to a DBA page.
   * @param path path relative to the DBA root
   * @param form form parameters
   * @return response body (after redirects)
   * @throws IOException I/O exception
   */
  protected static String post(final String path, final Map<String, String> form)
      throws IOException {
    final StringJoiner sj = new StringJoiner("&");
    form.forEach((k, v) -> sj.add(enc(k) + '=' + enc(v)));
    return run("POST", path, HttpRequest.BodyPublishers.ofString(sj.toString()),
        "application/x-www-form-urlencoded");
  }

  /**
   * Sends a POST request with a raw text body.
   * @param path path relative to the DBA root
   * @param body request body
   * @return response body
   * @throws IOException I/O exception
   */
  protected static String post(final String path, final String body) throws IOException {
    return run("POST", path, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8),
        "text/plain; charset=utf-8");
  }

  /**
   * Sends a request with the session client and checks for a 200 status.
   * @param method HTTP method
   * @param path path relative to the DBA root
   * @param body request body publisher
   * @param type content type ({@code null} to omit)
   * @return response body
   * @throws IOException I/O exception
   */
  private static String run(final String method, final String path,
      final HttpRequest.BodyPublisher body, final String type) throws IOException {

    final HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(HTTP_ROOT + DBA + path));
    if(type != null) rb.header("Content-Type", type);
    rb.method(method, body);
    try {
      final HttpResponse<String> resp = client.send(rb.build(), BodyHandlers.ofString());
      assertEquals(200, resp.statusCode(), method + ' ' + path + '\n' + resp.body());
      return resp.body().replace("\r", "");
    } catch(final InterruptedException ex) {
      throw new IOException(ex);
    }
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
