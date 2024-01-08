package org.basex.http.auth;

import org.junit.jupiter.api.*;

/**
 * Basic HTTP authentication tests.
 *
 * @author BaseX Team 2005-24, BSD License
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

  /** Missing authentication method. */
  @Test public void missing() {
    responseFail(REST_ROOT);
  }

  /** Access denied. */
  @Test public void wrong() {
    responseFail(REST_ROOT.replace("://", "://user:unknown@"));
  }
}
