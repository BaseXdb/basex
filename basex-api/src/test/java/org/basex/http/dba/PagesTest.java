package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.http.*;
import java.nio.charset.*;

import org.junit.jupiter.api.*;

/**
 * Smoke tests for the top-level DBA pages.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PagesTest extends DBATest {
  /**
   * Requests every top-level page and checks for an authenticated HTML response.
   * @throws IOException I/O exception
   */
  @Test public void pages() throws IOException {
    for(final String page : new String[] { "databases", "users", "files", "logs", "activity",
        "settings" }) {
      final String html = get(page);
      assertTrue(html.contains("<title>DBA"), page + ": not an authenticated DBA page:\n" + html);
    }
  }

  /**
   * Serves a static resource from the file system, and rejects an unknown one.
   * @throws IOException I/O exception
   */
  @Test public void staticResource() throws IOException {
    final HttpResponse<String> response = send(200, "GET", ".static/style.css", null, null);
    final String body = response.body();
    assertTrue(body.contains("{"), body);
    assertEquals(String.valueOf(body.getBytes(StandardCharsets.UTF_8).length),
        response.headers().firstValue("Content-Length").orElse(null));
    send(404, "GET", ".static/unknown.css", null, null);
  }

  /**
   * The views that were merged into others are gone; their addresses must not resolve any more.
   * @throws IOException I/O exception
   */
  @Test public void mergedPages() throws IOException {
    for(final String page : new String[] { "database", "user", "jobs", "sessions", "editor" }) {
      send(404, "GET", page, null, null);
    }
  }
}
