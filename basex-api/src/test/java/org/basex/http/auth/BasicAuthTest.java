package org.basex.http.auth;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.nio.charset.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.query.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Basic HTTP authentication tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BasicAuthTest extends AuthTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init();
  }

  /**
   * Successful response.
   * @throws Exception exception
   */
  @Test public void ok() throws Exception {
    responseOk(REST_ROOT.replace("://", "://admin:" + NAME + "@") + "?query=1");
  }

  /**
   * Insufficient permissions in a query.
   * @throws Exception exception
   */
  @Test public void forbidden() throws Exception {
    final Context ctx = HTTPContext.get().context();
    final String user = NAME + "user";
    new CreateUser(user, NAME).execute(ctx);
    try {
      responseFail(REST_ROOT.replace("://", "://" + user + ':' + NAME + '@') + "?query=" +
          URLEncoder.encode(_DB_CREATE.args(NAME), StandardCharsets.UTF_8), "403");
    } finally {
      new DropUser(user).execute(ctx);
    }
  }

  /** Missing authentication method. */
  @Test public void missing() {
    responseFail(REST_ROOT);
  }

  /** Access denied. */
  @Test public void wrong() {
    responseFail(REST_ROOT.replace("://", "://user:unknown@"));
  }

  /**
   * Malformed credentials.
   * @throws Exception exception
   */
  @Test public void malformed() throws Exception {
    try(QueryProcessor qp = new QueryProcessor(_HTTP_SEND_REQUEST.args(
        " <http:request xmlns:http='http://expath.org/ns/http-client' method='GET' href='" +
        REST_ROOT + "'><http:header name='Authorization' value='Basic abc'/></http:request>") +
        "[. instance of node()]/@status/string()", HTTPContext.get().context())) {
      assertEquals("401", qp.value().serialize().toString());
    }
  }
}
