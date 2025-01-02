package org.basex.http;

import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded HTTP Client locally (without database server instance.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class LocalFnHttpTest extends FnHttpTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_URL, true);
    ctx = new Context();
  }
}
