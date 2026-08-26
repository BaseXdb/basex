package org.basex.core.locks;

import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Runs updating queries that lock two databases in random order. Databases are locked in a global
 * order, so the queries must never deadlock: a test that runs into its time-out is a failure.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LockOrderTest extends SandboxTest {
  /** Number of databases. */
  private static final int DBS = 5;
  /** Number of items in a database. */
  private static final int ITEMS = 20;

  /**
   * Creates the test databases.
   */
  @BeforeEach public void init() {
    final StringBuilder input = new StringBuilder("<source>");
    for(int i = 0; i < ITEMS; i++) input.append("<item>").append(i).append("</item>");
    input.append("</source>");
    for(int d = 0; d < DBS; d++) {
      query(_DB_CREATE.args(name(d), " " + input, "source.xml"));
    }
  }

  /**
   * Drops the test databases.
   */
  @AfterEach public void finish() {
    for(int d = 0; d < DBS; d++) query(_DB_DROP.args(name(d)));
  }

  /**
   * Locks that are determined at compile time.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void staticLocks() throws Exception {
    run(20, 20, false);
  }

  /**
   * Database names that are computed at runtime, and hence lock all databases.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void runtimeLocks() throws Exception {
    run(20, 20, true);
  }

  /**
   * Locks that are determined at compile time, requested by many clients.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void manyClients() throws Exception {
    run(50, 10, false);
  }

  /**
   * Runs the test.
   * @param clients number of clients
   * @param runs number of runs per client
   * @param runtime resolve database names at runtime
   * @throws Exception exception
   */
  private static void run(final int clients, final int runs, final boolean runtime)
      throws Exception {

    final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
    for(int c = 0; c < clients; c++) {
      // one generator per client: a shared instance would not be reproducible
      final Random rnd = new Random(c);
      tasks.add(() -> {
        for(int r = 0; r < runs; r++) {
          // pick two distinct databases; the order of the pair is random, the order in which
          // the locks are acquired must not be
          final int s = rnd.nextInt(DBS);
          final int t = (s + 1 + rnd.nextInt(DBS - 1)) % DBS;
          final String source = runtime ? wrap(name(s)) : '\'' + name(s) + '\'';
          final String target = runtime ? wrap(name(t)) : '\'' + name(t) + '\'';
          query(_DB_PUT.args(" " + target, " <copy>{ count(" + _DB_GET.args(" " + source) +
              "//item) }</copy>", "copy.xml"));
        }
        return null;
      });
    }
    parallel(tasks);

    // no query may have seen a partially written database
    for(int d = 0; d < DBS; d++) {
      query("count(" + _DB_GET.args(name(d)) + "//item)", ITEMS);
      query("every $copy in " + _DB_GET.args(name(d)) + "/copy satisfies $copy = " + ITEMS, true);
    }
  }

  /**
   * Returns the name of the nth test database.
   * @param n database index
   * @return database name
   */
  private static String name(final int n) {
    return NAME + n;
  }
}
