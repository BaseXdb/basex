package org.basex.http.auth;

import org.junit.jupiter.api.*;

/**
 * Basic HTTP authentication tests.
 *
 * @author BaseX Team 2005-22, BSD License
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
    test(REST_ROOT.replace("://", "://admin:admin@") + "?query=1", 200);
  }

  /**
   * Missing authentication method.
   * @throws Exception exception
   */
  @Test public void missing() throws Exception {
    test(REST_ROOT, 401);
  }

  /**
   * Access denied.
   * @throws Exception exception
   */
  @Test public void wrong() throws Exception {
    test(REST_ROOT.replace("://", "://user:unknown@"), 401);
  }
}
