package org.basex.modules;

import static org.junit.Assert.*;

import java.net.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the Session Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class SessionTest extends HTTPTest {
  /** Root path. */
  private static final String ROOT = "http://" + Text.S_LOCALHOST + ":9998/rest/";

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(ROOT, true);
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void id() throws Exception {
    assertEquals("1", get("?query=" + request("count(S:id())")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void names() throws Exception {
    assertEquals("a", get("?query=" + request("S:set('a','b'), S:names()")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void get() throws Exception {
    assertEquals("", get("?query=" + request("S:get('a')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void set() throws Exception {
    final String query = "S:set('a','b'), S:get('a')";
    assertEquals("b", get("?query=" + request(query)));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void close() throws Exception {
    assertEquals("", get("?query=" + request("S:close()")));
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Returns an encoded version of the query, including a Request module import.
   * @param query query string
   * @return prepared query
   * @throws Exception exception
   */
  private static String request(final String query) throws Exception {
    return URLEncoder.encode("import module namespace " +
        "S='http://basex.org/modules/session';" + query, Token.UTF8);
  }
}
