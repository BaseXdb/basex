package org.basex.modules;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;

import org.basex.http.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the Session Module.
 *
 * @author BaseX Team 2005-21, BSD License
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
    assertEquals("1", get("?query=" + request("count(S:id())")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void names() throws Exception {
    assertEquals("a", get("?query=" + request("S:set('a','b'), S:names()")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void get() throws Exception {
    assertEquals("", get("?query=" + request("S:get('a')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void set() throws Exception {
    final String query = "S:set('a','b'), S:get('a')";
    assertEquals("b", get("?query=" + request(query)));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void close() throws Exception {
    assertEquals("", get("?query=" + request("S:close()")));
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns an encoded version of the query, including a Request module import.
   * @param query query string
   * @return prepared query
   * @throws Exception exception
   */
  private static String request(final String query) throws Exception {
    return URLEncoder.encode("import module namespace " +
        "S='http://basex.org/modules/session';" + query, Strings.UTF8);
  }
}
