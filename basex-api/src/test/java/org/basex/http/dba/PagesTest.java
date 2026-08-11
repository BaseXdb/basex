package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

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
   * The views that were merged into others are gone; their addresses must not resolve any more.
   * @throws IOException I/O exception
   */
  @Test public void mergedPages() throws IOException {
    for(final String page : new String[] { "database", "user", "jobs", "sessions", "editor" }) {
      send(404, "GET", page, null, null);
    }
  }
}
