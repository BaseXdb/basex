package org.basex.query.func;

import static org.basex.query.func.ApiFunction.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the Request Module.
 *
 * @author BaseX Team 2005-22, BSD License
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
    get("GET", "", "query", func.args());
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void scheme() throws Exception {
    final ApiFunction func = _REQUEST_SCHEME;
    get("http", "", "query", func.args());
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void port() throws Exception {
    final ApiFunction func = _REQUEST_PORT;
    get(String.valueOf(HTTP_PORT), "", "query", func.args());
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void path() throws Exception {
    final ApiFunction func = _REQUEST_PATH;
    put(null, NAME);
    get("/rest/", "", "query", func.args());
    get("/rest/" + NAME, NAME + "", "query", func.args());
    delete(200, NAME);
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void query() throws Exception {
    final ApiFunction func = _REQUEST_QUERY;
    get("true", "", "query", "starts-with(" + func.args() + ", 'query')");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void uri() throws Exception {
    final ApiFunction func = _REQUEST_URI;
    get("true", "", "query", "starts-with(" + func.args() + ", 'http')");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void contextPath() throws Exception {
    final ApiFunction func = _REQUEST_CONTEXT_PATH;
    get("", "", "query", func.args());
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void parameterNames() throws Exception {
    final ApiFunction func = _REQUEST_PARAMETER_NAMES;
    final String query = "count(" + func.args() + ")";
    get("1", "", "query", query);
    get("2", "", "query", query, "a", "b");
    get("2", "", "query", query, "a", "b", "a", "b");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void parameter() throws Exception {
    final ApiFunction func = _REQUEST_PARAMETER;
    get("b", "", "query", func.args("a"), "a", "b");
    get("b,c", "", "query", "string-join(" + func.args("a") + ", ',')", "a", "b", "a", "c");
    get("b", "", "query", func.args("a", "c"), "a", "b");
    get("c", "", "query", func.args("x", "c"), "a", "b");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void headerNames() throws Exception {
    final ApiFunction func = _REQUEST_HEADER_NAMES;
    final String result = get(200, "", "query", func.args());
    assertEquals("true", query("let $names := tokenize(``[" + result + "]``)" +
        "return every $n in ('Connection', 'Host', 'User-Agent') satisfies $n = $names"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void header() throws Exception {
    final ApiFunction func = _REQUEST_HEADER;
    get("localhost:" + HTTP_PORT, "", "query", func.args("Host"));
    get("def", "", "query", func.args("ABC", "def"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void cookieNames() throws Exception {
    final ApiFunction func = _REQUEST_COOKIE_NAMES;
    get("0", "", "query", "count(" + func.args() + ")");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void cookie() throws Exception {
    final ApiFunction func = _REQUEST_COOKIE;
    get("0", "", "query", "count(" + func.args("X") + ")");
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void attribute() throws Exception {
    final ApiFunction func = _REQUEST_ATTRIBUTE;
    get("", "", "query", func.args("A"));
    get("B", "", "query", "request:set-attribute('A', 'B')," + func.args("A"));
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void attributeNames() throws Exception {
    final ApiFunction func = _REQUEST_ATTRIBUTE_NAMES;
    get("", "", "query", func.args());
    get("A", "", "query", "request:set-attribute('A', 1)," + func.args());
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void setAttribute() throws Exception {
    final ApiFunction func = _REQUEST_SET_ATTRIBUTE;
    get("1", "", "query", func.args("A", 1) + "," + "request:attribute('A')");
  }
}
