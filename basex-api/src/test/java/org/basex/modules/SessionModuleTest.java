package org.basex.modules;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the Session Module.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class SessionModuleTest extends HTTPTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_ROOT, true);
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void id() throws Exception {
    get("1", "", "query", "count(session:id())");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void names() throws Exception {
    get("a", "", "query", "session:set('a', 'b'), session:names()");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void get() throws Exception {
    get("", "", "query", "session:get('a')");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void set() throws Exception {
    final String query = "session:set('a', 'b'), session:get('a')";
    get("b", "", "query", query);
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void close() throws Exception {
    get("", "", "query", "session:close()");
  }
}
