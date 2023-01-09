package org.basex.http.auth;

import org.junit.jupiter.api.*;

/**
 * Digest HTTP authentication tests.
 *
 * @author BaseX Team 2005-23, BSD License
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

  /** Missing authentication method. */
  @Test public void missing() {
    responseFail(REST_ROOT + "?query=1");
  }

  /** Wrong authentication method. */
  @Test public void wrong() {
    responseFail(REST_ROOT.replace("://", "://user:unknown@"));
  }
}
