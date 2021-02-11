package org.basex.http.rest;

import org.junit.jupiter.api.*;

/**
 * This class tests the server-based REST API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ServerRESTTest extends RESTTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_ROOT, false);
  }
}
