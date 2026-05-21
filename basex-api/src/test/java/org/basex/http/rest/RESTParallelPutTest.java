package org.basex.http.rest;

import org.basex.http.*;
import org.basex.io.in.*;
import org.junit.jupiter.api.*;

/**
 * This class sends parallel PUT requests to the REST API.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RESTParallelPutTest extends HTTPTest {
  /** Client count. */
  private static final int CLIENTS = 10;
  /** Runs per client. */
  private static final int RUNS = 10;

  // INITIALIZERS =================================================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_ROOT, true);
  }

  // TEST METHODS =================================================================================

  /**
   * Concurrency test.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void test() throws Exception {
    put(new ArrayInput(""), NAME);

    parallel(CLIENTS, () -> {
      for(int i = 0; i < RUNS; i++) {
        put(new ArrayInput("<x/>"), NAME + '/' + i + ".xml");
      }
      return null;
    });

    delete(200, NAME);
  }
}
