package org.basex.test.http;

import org.junit.BeforeClass;

/**
 * This class tests the server-based REST implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ServerRESTTest extends RESTTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(ROOT, false);
  }
}
