package org.exquery.ns;

import static org.junit.Assert.*;

import java.net.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the Request Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RequestTest extends HTTPTest {
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
    put(NAME, null);
    assertEquals("/rest/", get("?query=" + request("R:path()")));
    assertEquals("/rest/" + NAME, get(NAME + "?query=" + request("R:path()")));
    delete(NAME);
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
  public void contextPath() throws Exception {
    assertEquals("", get("?query=" + request("R:context-path()")));
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
    assertEquals("b", get("?query=" + request("R:parameter('a','c')") + "&a=b"));
    assertEquals("c", get("?query=" + request("R:parameter('x','c')") + "&a=b"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void headerNames() throws Exception {
    final String query = "R:header-names()";
    assertEquals("Host Accept Connection User-Agent", get("?query=" + request(query)));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void header() throws Exception {
    assertEquals("localhost:9998", get("?query=" + request("R:header('Host')")));
    assertEquals("def", get("?query=" + request("R:header('ABC', 'def')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void cookieNames() throws Exception {
    final String query = "count(R:cookie-names())";
    assertEquals("0", get("?query=" + request(query)));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test
  public void cookie() throws Exception {
    final String query = "count(R:cookie('x'))";
    assertEquals("0", get("?query=" + request(query)));
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
        "import module namespace R='http://exquery.org/ns/request';" + query, Strings.UTF8);
  }
}
