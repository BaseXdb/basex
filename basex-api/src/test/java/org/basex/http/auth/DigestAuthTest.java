package org.basex.http.auth;

import org.junit.jupiter.api.*;

/**
 * Digest HTTP authentication tests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DigestAuthTest extends AuthTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init("Digest");
  }

  /**
   * Missing authentication method.
   * @throws Exception exception
   */
  @Test public void missing() throws Exception {
    test(REST_ROOT + "?query=1", 401);
  }

  /**
   * Wrong authentication method.
   * @throws Exception exception
   */
  @Test public void wrong() throws Exception {
    test(REST_ROOT.replace("://", "://user:unknown@"), 401);
  }
}
