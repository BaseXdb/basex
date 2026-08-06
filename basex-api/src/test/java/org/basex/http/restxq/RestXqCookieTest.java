package org.basex.http.restxq;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.junit.jupiter.api.*;

/**
 * This class tests the cookie handling of the HTTP Client.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqCookieTest extends RestXqTest {
  /** HTTP client namespace declaration. */
  private static final String HTTP_NS = "xmlns:http='http://expath.org/ns/http-client'";
  /** Service that assigns a cookie and redirects, service that returns the received cookies. */
  private static final String SERVICES =
    "declare %R:path('cookie-set') function m:set() {"
    + "<R:response><http:response status='302' " + HTTP_NS + ">"
    + "<http:header name='Set-Cookie' value='a=1; Path=/'/>"
    + "<http:header name='Location' value='" + HTTP_ROOT + "cookie-echo'/>"
    + "</http:response></R:response>"
    + "};"
    + "declare %R:path('cookie-echo') %output:method('text') function m:echo() {"
    + "request:header('Cookie', 'none')"
    + "};";

  /**
   * Registers the services.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void services() throws IOException {
    register(SERVICES);
  }

  /** Cookies of a redirecting response are attached to the redirected request. */
  @Test public void redirect() {
    query(request("cookies='true'", "cookie-set"), "a=1");
  }

  /** Cookies are discarded if the attribute is not supplied. */
  @Test public void noCookies() {
    query(request("", "cookie-set"), "none");
  }

  /** Cookies are shared by all requests of a query. */
  @Test public void singleQuery() {
    query(request("cookies='true'", "cookie-set") + ',' +
      request("cookies='true'", "cookie-echo"), "a=1\na=1");
  }

  /** Cookies are not shared by different queries. */
  @Test public void multipleQueries() {
    query(request("cookies='true'", "cookie-set"), "a=1");
    query(request("cookies='true'", "cookie-echo"), "none");
  }

  /**
   * Returns a query that sends a request and returns the response body.
   * @param attribute additional request attribute
   * @param path path of the addressed service
   * @return query
   */
  private static String request(final String attribute, final String path) {
    return _HTTP_SEND_REQUEST.args(
      " <http:request " + HTTP_NS + " method='get' " + attribute + "/>", HTTP_ROOT + path) + "[2]";
  }
}
