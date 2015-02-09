package org.basex.http;

import org.basex.core.*;
import org.junit.*;

/**
 * This class tests the embedded HTTP Client.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 */
public final class ServerFnHttpTest extends FnHttpTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(RESTURL, false);
    ctx = new Context();
  }
}
