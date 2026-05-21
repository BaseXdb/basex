package org.basex.local.multiple;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * This class performs local stress tests with a specified number of threads and queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class MultipleAddTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/input.xml";

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients10runs10() throws Exception {
    run(10, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients10runs100() throws Exception {
    run(10, 100);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients100runs10() throws Exception {
    run(100, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients100runs100() throws Exception {
    run(100, 100);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private static void run(final int clients, final int runs) throws Exception {
    // create test database
    execute(new CreateDB(NAME));
    // run clients, each with its own context, adding the input document repeatedly
    parallel(clients, () -> {
      final Context ctx = new Context(context);
      new Set(MainOptions.AUTOFLUSH, false).execute(ctx);
      new Set(MainOptions.INTPARSE, true).execute(ctx);
      new Open(NAME).execute(ctx);
      try {
        for(int r = 0; r < runs; r++) new Add("", INPUT).execute(ctx);
      } finally {
        new Close().execute(ctx);
      }
      return null;
    });
    // drop database
    execute(new DropDB(NAME));
  }
}
