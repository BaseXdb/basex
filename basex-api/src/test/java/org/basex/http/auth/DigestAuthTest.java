package org.basex.http.auth;

import org.basex.http.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Digest HTTP authentication tests.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class DigestAuthTest extends AuthTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init("Digest");
  }

  /**
   * Missing authentication method.
   */
  @Test
  public void missingAuth() {
    test(REST_ROOT + "?query=1", Util.info(HTTPText.WRONGAUTH_X, method));
  }

  /**
   * Wrong authentication method.
   */
  @Test
  public void wrongAuth() {
    test(REST_ROOT.replace("://", "://user:unknown@"), Util.info(HTTPText.WRONGAUTH_X, method));
  }
}
