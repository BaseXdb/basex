package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.core.cmd.XQuery;
import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the caches panel of the Activity view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CachesTest extends DBATest {
  /** Test cache. */
  private static final String CACHE = "dba-junit-cache";

  /**
   * Fills a cache before each test.
   * @throws Exception exception
   */
  @BeforeEach public void fill() throws Exception {
    execute("cache:put('key', 'value', '" + CACHE + "')");
  }

  /**
   * Deletes the caches after each test.
   * @throws Exception exception
   */
  @AfterEach public void cleanup() throws Exception {
    execute("cache:clear()");
  }

  /**
   * A cache is listed with what it holds.
   * @throws Exception exception
   */
  @Test public void listed() throws Exception {
    final String page = get("activity");
    assertTrue(page.contains(CACHE), "cache missing from the panel");
    assertTrue(page.contains("(default)"), "default cache not listed");
  }

  /**
   * A cache is deleted.
   * @throws Exception exception
   */
  @Test public void delete() throws Exception {
    assertTrue(post("caches/delete", Map.of("cache", CACHE)).
        contains("Cache \"" + CACHE + "\" was deleted."), "cache not deleted");
    assertFalse(get("activity").contains(CACHE), "cache still listed");
  }

  /**
   * All caches are cleared.
   * @throws Exception exception
   */
  @Test public void clear() throws Exception {
    assertTrue(post("caches/clear", Map.of()).contains("All caches were cleared."),
        "caches not cleared");
    assertFalse(get("activity").contains(CACHE), "cache still listed");
  }

  /**
   * Runs a query in the context of the HTTP server.
   * @param query query
   * @throws Exception exception
   */
  private static void execute(final String query) throws Exception {
    new XQuery(query).execute(HTTPContext.get().context());
  }
}
