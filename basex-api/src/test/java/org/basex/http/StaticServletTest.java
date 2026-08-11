package org.basex.http;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.time.*;

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
