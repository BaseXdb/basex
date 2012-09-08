package org.basex.test.http;

import static org.junit.Assert.*;

import java.net.*;

import org.basex.core.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the Request Module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class SessionTest extends HTTPTest {
  /** Root path. */
  protected static final String ROOT = "http://" + Text.LOCALHOST + ":9998/rest/";

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
  public void sessionId() throws Exception {
    assertEquals("1", get("?query=" + request("count(S:session-id())")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void attribute() throws Exception {
    final String query = "S:update-attribute('a','b'), S:attribute('a')";
    assertEquals("b", get("?query=" + request(query)));
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
