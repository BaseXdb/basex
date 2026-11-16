package org.basex.http;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the servlet that returns static resources.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StaticServletTest extends HTTPTest {
  /** Contents of the served file. */
  private static final String CONTENT = "body { color: red }";
  /** Contents of the precompressed file (dummy, not decoded by the client). */
  private static final String BROTLI = "brotli";
  /** Contents of the gzipped file (dummy, not decoded by the client). */
  private static final String GZIP = "gzip";

  /**
   * Starts the HTTP server and creates a static resource.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(HTTP_ROOT, true);
    final IOFile dir = new IOFile(
      HTTPContext.get().context().soptions.get(StaticOptions.WEBPATH), "static");
    assertTrue(dir.md());
    new IOFile(dir, "test.css").write(CONTENT);
    new IOFile(dir, "test.css.br").write(BROTLI);
    new IOFile(dir, "test.css.gz").write(GZIP);
  }

  /**
   * A precompressed sibling is returned if the client accepts its encoding.
   * @throws Exception exception
   */
  @Test public void precompressed() throws Exception {
    encoded("br", BROTLI, "br");
    encoded("gzip", GZIP, "gzip");
    encoded("gzip, deflate, br, zstd", BROTLI, "br");
    encoded("BR;q=0.5", BROTLI, "br");
    encoded("br;q=0, gzip", GZIP, "gzip");
    encoded("deflate", CONTENT, "");
    encoded(null, CONTENT, "");
  }

  /**
   * A file of the static directory is returned, with the media type of its suffix.
   * @throws Exception exception
   */
  @Test public void file() throws Exception {
    final HttpResponse<String> response = send(null);
    assertEquals(200, response.statusCode(), response.body());
    assertEquals(CONTENT, response.body());
    // the container may append a charset
    final String type = response.headers().firstValue(HTTPText.CONTENT_TYPE).orElse("");
    assertTrue(type.startsWith("text/css"), type);
  }

  /**
   * A file that does not exist yields 404.
   * @throws Exception exception
   */
  @Test public void unknown() throws Exception {
    get(404, "static/missing.css");
  }

  /**
   * A path that points outside the static directory does not return the addressed file.
   * Servlet containers normalize such paths before dispatching them, so the check of the
   * servlet is a second line of defense.
   * @throws Exception exception
   */
  @Test public void outside() throws Exception {
    final HttpRequest request = HttpRequest.newBuilder(
      URI.create(HTTP_ROOT + "static/%2e%2e/WEB-INF/web.xml")).build();
    final HttpResponse<String> response = HttpClient.newHttpClient().
      send(request, HttpResponse.BodyHandlers.ofString());
    assertNotEquals(200, response.statusCode());
    assertFalse(response.body().contains("servlet-class"), response.body());
  }

  /**
   * A resource that has not been modified yields 304 and no body.
   * @throws Exception exception
   */
  @Test public void notModified() throws Exception {
    final String modified = send(null).headers().
      firstValue(HTTPText.LAST_MODIFIED).orElseThrow();
    final HttpResponse<String> response = send(modified);
    assertEquals(304, response.statusCode());
    assertEquals("", response.body());
  }

  /**
   * A malformed {@code If-Modified-Since} header is ignored.
   * @throws Exception exception
   */
  @Test public void malformedModified() throws Exception {
    assertEquals(200, send("yesterday").statusCode());
  }

  /**
   * Requests the static test resource with the specified accepted encodings and checks the result.
   * @param accept value of the {@code Accept-Encoding} header (can be {@code null})
   * @param body expected body
   * @param encoding expected content encoding (empty if none)
   * @throws Exception exception
   */
  private static void encoded(final String accept, final String body, final String encoding)
      throws Exception {
    final Map<String, String> headers = accept != null ?
      Map.of(HTTPText.ACCEPT_ENCODING, accept) : Map.of();
    assertEquals(body, send(200, "GET", null, null, headers, "static/test.css"), accept);
    assertEquals(encoding, Objects.toString(header(HTTPText.CONTENT_ENCODING), ""), accept);
    assertEquals(HTTPText.ACCEPT_ENCODING, header(HTTPText.VARY));
    final String type = header(HTTPText.CONTENT_TYPE);
    assertTrue(type.startsWith("text/css"), type);
  }

  /**
   * Requests the static test resource.
   * @param modified value of the {@code If-Modified-Since} header (can be {@code null})
   * @return response
   * @throws Exception exception
   */
  private static HttpResponse<String> send(final String modified) throws Exception {
    final HttpRequest.Builder builder = HttpRequest.newBuilder(
      URI.create(HTTP_ROOT + "static/test.css")).timeout(Duration.ofSeconds(10));
    if(modified != null) builder.header(HTTPText.IF_MODIFIED_SINCE, modified);
    return HttpClient.newHttpClient().send(builder.build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
