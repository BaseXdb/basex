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
public class RequestTest extends HTTPTest {
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
  public void method() throws Exception {
    assertEquals("GET", get("?query=" + request("R:method()")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void scheme() throws Exception {
    assertEquals("http", get("?query=" + request("R:scheme()")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void port() throws Exception {
    assertEquals("9998", get("?query=" + request("R:port()")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void path() throws Exception {
    put(ROOT + NAME, null);
    assertEquals("/rest/", get("?query=" + request("R:path()")));
    assertEquals("/rest/" + NAME, get(NAME + "?query=" + request("R:path()")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void query() throws Exception {
    assertEquals("true", get("?query=" + request("starts-with(R:query(), 'query')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void uri() throws Exception {
    assertEquals("true", get("?query=" + request("starts-with(R:uri(), 'http')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void parameterNames() throws Exception {
    final String query = "count(R:parameter-names())";
    assertEquals("1", get("?query=" + request(query)));
    assertEquals("2", get("?query=" + request(query) + "&a=b"));
    assertEquals("2", get("?query=" + request(query) + "&a=b&a=b"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void parameter() throws Exception {
    assertEquals("b", get("?query=" + request("R:parameter('a')") + "&a=b"));
    assertEquals("b c", get("?query=" + request("R:parameter('a')") + "&a=b&a=c"));
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Returns an encoded version of the query, including a Request module import.
   * @param query query string
   * @return prepared query
   * @throws Exception exception
   */
  private static String request(final String query) throws Exception {
    return URLEncoder.encode(
        "import module namespace R='http://exquery.org/ns/request';" + query, Token.UTF8);
  }
}
