package org.basex.http.auth;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.http.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * HTTP authentication tests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class AuthTest extends HTTPTest {
  /** Local database context. */
  private static Context ctx;
  /** Authentication method. */
  protected static String method;

  /**
   * Initializes the test.
   * @param meth method
   * @throws Exception exception
   */
  protected static void init(final String meth) throws Exception {
    Prop.put(StaticOptions.AUTHMETHOD, meth);
    method = meth;
    init(REST_ROOT, true, true);
    ctx = new Context();
  }

  /**
   * Stops the test.
   */
  @AfterAll public static void close() {
    Prop.clear();
    ctx.close();
  }

  /**
   * Successful request.
   * @throws Exception Exception
   */
  @Test public void sendRequestOk() throws Exception {
    assertEquals("200", sendRequest("admin", "admin"));
  }

  /**
   * Failed request.
   * @throws Exception Exception
   */
  @Test public void sendRequestFail() throws Exception {
    assertEquals("401", sendRequest("unknown", "wrong"));
  }

  /**
   * Calls the specified URL and checks the error message.
   * @param url URL
   * @param status status code to check
   * @throws Exception Exception
   */
  protected static void test(final String url, final int status) throws Exception {
    final IOUrl io = new IOUrl(url);
    assertEquals(status, io.response(HttpResponse.BodyHandlers.discarding()).statusCode());
  }

  /**
   * Tests authentication.
   * @param user user
   * @param pass password
   * @return code
   * @throws Exception Exception
   */
  private static String sendRequest(final String user, final String pass) throws Exception {
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request xmlns:http='http://expath.org/ns/http-client' method='GET' " +
        "auth-method='" + method + "' username='" + user + "' password='" + pass + "' " +
        "href='" + REST_ROOT + "'/>") +
        "[. instance of node()]/@status/string()", ctx)) {
      return qp.value().serialize().toString();
    }
  }
}
