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
    // "sessions" omitted: non-resident sessions from other classes make sessions:accessed throw
    for(final String page : new String[] { "databases", "users", "editor", "files", "jobs",
        "logs", "settings" }) {
      final String html = get(page);
      assertTrue(html.contains("<title>DBA"), page + ": not an authenticated DBA page:\n" + html);
    }
  }
}
