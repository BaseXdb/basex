package org.basex.http.auth;

import org.basex.core.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Digest HTTP authentication tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DigestAuthTest extends AuthTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    // digest authentication requires the matching password algorithm
    Prop.put(StaticOptions.AUTHALGORITHMS, "salted-sha256,digest");
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
