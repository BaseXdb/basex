package org.basex.http.rest;

import static org.basex.query.func.Function.*;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * This class sends parallel GET requests to the REST API.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RESTParallelGetTest extends HTTPTest {
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
    get(200, "", "command", "CREATE DB " + REST + " <a/>");

    parallel(CLIENTS, () -> {
      for(int i = 0; i < RUNS; i++) {
        final double rnd = Math.random();
        final boolean query = rnd < 1 / 3.0d, delete = rnd > 2 / 3.0d;
        if(query) get(200, REST, "query", "count(.)");
        else if(delete) get(200, REST, "query", _DB_DELETE.args("rest", "/"));
        else get(200, REST, "query", _DB_ADD.args("rest", " <a/>", "x"));
      }
      return null;
    });

    get(200, "", "command", "DROP DB " + REST);
  }
}
