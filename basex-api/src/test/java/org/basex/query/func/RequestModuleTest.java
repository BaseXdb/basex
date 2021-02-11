package org.basex.query.func;

import static org.basex.query.func.ApiFunction.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.*;

import org.basex.http.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the Request Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RequestModuleTest extends HTTPTest {
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
  @Test public void method() throws Exception {
    final ApiFunction func = _REQUEST_METHOD;
    assertEquals("GET", get(queryString(func.args())));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void scheme() throws Exception {
    final ApiFunction func = _REQUEST_SCHEME;
    assertEquals("http", get(queryString(func.args())));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void port() throws Exception {
    final ApiFunction func = _REQUEST_PORT;
    assertEquals(String.valueOf(HTTP_PORT), get(queryString(func.args())));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void path() throws Exception {
    final ApiFunction func = _REQUEST_PATH;
    put(NAME, null);
    assertEquals("/rest/", get(queryString(func.args())));
    assertEquals("/rest/" + NAME, get(NAME + queryString(func.args())));
    delete(NAME);
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void query() throws Exception {
    final ApiFunction func = _REQUEST_QUERY;
    assertEquals("true", get(queryString("starts-with(" + func.args() + ", 'query')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void uri() throws Exception {
    final ApiFunction func = _REQUEST_URI;
    assertEquals("true", get(queryString("starts-with(" + func.args() + ", 'http')")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void contextPath() throws Exception {
    final ApiFunction func = _REQUEST_CONTEXT_PATH;
    assertEquals("", get(queryString(func.args())));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void parameterNames() throws Exception {
    final ApiFunction func = _REQUEST_PARAMETER_NAMES;
    final String query = "count(" + func.args() + ")";
    assertEquals("1", get(queryString(query)));
    assertEquals("2", get(queryString(query) + "&a=b"));
    assertEquals("2", get(queryString(query) + "&a=b&a=b"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void parameter() throws Exception {
    final ApiFunction func = _REQUEST_PARAMETER;
    assertEquals("b", get(queryString(func.args("a")) + "&a=b"));
    assertEquals("b,c", get(queryString("string-join(" + func.args("a") + ", ',')") + "&a=b&a=c"));
    assertEquals("b", get(queryString(func.args("a", "c")) + "&a=b"));
    assertEquals("c", get(queryString(func.args("x", "c")) + "&a=b"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void headerNames() throws Exception {
    final ApiFunction func = _REQUEST_HEADER_NAMES;
    final String result = get(queryString("string-join(" + func.args() + ", ',')"));
    assertEquals("Accept,Connection,Host,User-Agent",
        query("``[" + result + "]`` => tokenize(',') => sort() => string-join(',')"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void header() throws Exception {
    final ApiFunction func = _REQUEST_HEADER;
    assertEquals("localhost:" + HTTP_PORT, get(queryString(func.args("Host"))));
    assertEquals("def", get(queryString(func.args("ABC", "def"))));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void cookieNames() throws Exception {
    final ApiFunction func = _REQUEST_COOKIE_NAMES;
    assertEquals("0", get(queryString("count(" + func.args() + ")")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void cookie() throws Exception {
    final ApiFunction func = _REQUEST_COOKIE;
    assertEquals("0", get(queryString("count(" + func.args("X") + ")")));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void attribute() throws Exception {
    final ApiFunction func = _REQUEST_ATTRIBUTE;
    assertEquals("", get(queryString(func.args("A"))));
    assertEquals("B", get(queryString("request:set-attribute('A','B')," + func.args("A"))));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void attributeNames() throws Exception {
    final ApiFunction func = _REQUEST_ATTRIBUTE_NAMES;
    assertEquals("", get(queryString(func.args())));
    assertEquals("A", get(queryString("request:set-attribute('A',1)," + func.args())));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void setAttribute() throws Exception {
    final ApiFunction func = _REQUEST_SET_ATTRIBUTE;
    assertEquals("1", get(queryString(func.args("A", 1) + "," +
        "request:attribute('A')")));
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns an encoded query string.
   * @param string query string
   * @return prepared query
   * @throws Exception exception
   */
  private static String queryString(final String string) throws Exception {
    return "?query=" + URLEncoder.encode(string, Strings.UTF8);
  }
}
