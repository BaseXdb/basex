package org.basex.test.http;

import org.junit.BeforeClass;

/**
 * This class tests the embedded HTTP Client.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public class ServerHTTPClientTest extends HTTPClientTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(false);
  }
}
