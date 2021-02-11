package org.basex.http.auth;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * HTTP authentication tests.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param error expected error, or {@code null} if no error is expected
   */
  protected static void test(final String url, final String error) {
    try {
      final String request = request(url, "", "GET");
      if(error != null) fail("Error expected:\n" + request);
    } catch(final IOException ex) {
      if(error == null) fail("No error expected:\n" + ex);
      assertEquals(error, ex.getMessage());
    }
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
        "send-authorization='true' href='" + REST_ROOT + "'/>") +
        "[. instance of node()]/@status/string()", ctx)) {
      return qp.value().serialize().toString();
    }
  }
}
