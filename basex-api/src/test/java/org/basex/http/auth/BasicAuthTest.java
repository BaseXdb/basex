package org.basex.http.auth;

import static org.basex.query.func.Function.*;

import java.net.*;
import java.nio.charset.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
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
    init("Basic");
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
}
