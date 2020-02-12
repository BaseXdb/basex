package org.basex.http.auth;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Basic HTTP authentication tests.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class BasicAuthTest extends AuthTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass public static void start() throws Exception {
    init("Basic");
  }

  /**
   * Successful response.
   */
  @Test public void simple() {
    test(REST_ROOT.replace("://", "://admin:admin@") + "?query=1", null);
  }

  /**
   * Missing authentication method.
   */
  @Test public void missingAuth() {
    test(REST_ROOT, Util.info(HTTPText.WRONGAUTH_X, method));
  }

  /**
   * Access denied.
   */
  @Test public void accessDenied() {
    test(REST_ROOT.replace("://", "://user:unknown@"), Text.ACCESS_DENIED);
  }
}
