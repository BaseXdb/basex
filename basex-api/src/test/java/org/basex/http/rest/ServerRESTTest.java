package org.basex.http.rest;

import org.junit.*;

/**
 * This class tests the server-based REST API.
 *
 * @author BaseX Team 2005-14, BSD License
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
