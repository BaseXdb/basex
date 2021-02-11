package org.basex.local.multiple;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.junit.jupiter.api.Test;

/**
 * This class performs local stress tests with a specified number of threads and queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MultipleAddTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/input.xml";

  /** Result counter. */
  static int counter;

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
    // Create test database
    execute(new CreateDB(NAME));
    // Start clients
    final Client[] cl = new Client[clients];
    for(int i = 0; i < clients; ++i) cl[i] = new Client(context, runs);
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    // Drop database
    execute(new DropDB(NAME));
  }

  /** Single client. */
  static class Client extends Thread {
    /** Client context. */
    private final Context ctx;
    /** Number of runs. */
    private final int runs;

    /**
     * Constructor.
     * @param ctx database context
     * @param runs number of runs
     */
    Client(final Context ctx, final int runs) {
      this.runs = runs;
      this.ctx = new Context(ctx);
      this.ctx.user(ctx.users.get(UserText.ADMIN));
    }

    @Override
    public void run() {
      try {
        new Set(MainOptions.AUTOFLUSH, false).execute(ctx);
        new Set(MainOptions.INTPARSE, true).execute(ctx);
        new Open(NAME).execute(ctx);
        try {
          for(int r = 0; r < runs; ++r) {
            new Add("", INPUT).execute(ctx);
          }
        } finally {
          new Close().execute(ctx);
        }
      } catch(final BaseXException ex) {
        ex.printStackTrace();
      }
    }
  }
}
