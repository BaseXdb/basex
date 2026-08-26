package org.basex.local.multiple;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class updates several databases in parallel. Write locks are granted per database, so
 * clients that work on distinct databases must not block each other; the measured runtimes are
 * output for comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class MultipleDatabaseTest extends SandboxTest {
  /** Number of updates per client. */
  private static final int RUNS = 200;

  /**
   * Runs the test with 4 clients.
   * @throws Exception exception
   */
  @Test public void clients4() throws Exception {
    run(4);
  }

  /**
   * Runs the test with 16 clients.
   * @throws Exception exception
   */
  @Test public void clients16() throws Exception {
    run(16);
  }

  /**
   * Updates the same database from all clients, and then one database per client.
   * @param clients number of clients
   * @throws Exception exception
   */
  private static void run(final int clients) throws Exception {
    // all clients update a single database: the updates are serialized
    for(int c = 0; c < clients; c++) execute(new CreateDB(name(c), "<root/>"));
    final long shared = update(clients, 0);
    query("count(db:get('" + name(0) + "')//x)", clients * RUNS);

    // every client updates its own database: the updates may run in parallel
    for(int c = 0; c < clients; c++) execute(new DropDB(name(c)));
    for(int c = 0; c < clients; c++) execute(new CreateDB(name(c), "<root/>"));
    final long separate = update(clients, -1);
    for(int c = 0; c < clients; c++) query("count(db:get('" + name(c) + "')//x)", RUNS);
    for(int c = 0; c < clients; c++) execute(new DropDB(name(c)));

    Util.println(clients + " clients, " + RUNS + " updates each: " +
        "one database: " + Performance.formatNano(shared) + ", " +
        "one database per client: " + Performance.formatNano(separate));
  }

  /**
   * Runs concurrent updates and returns the elapsed time.
   * @param clients number of clients
   * @param database index of the database to update, or {@code -1} for one database per client
   * @return elapsed time in nanoseconds
   * @throws Exception exception
   */
  private static long update(final int clients, final int database) throws Exception {
    final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
    for(int c = 0; c < clients; c++) {
      final String db = name(database == -1 ? c : database);
      tasks.add(() -> {
        for(int r = 0; r < RUNS; r++) {
          query("insert node <x/> into db:get('" + db + "')/root");
        }
        return null;
      });
    }
    final Performance p = new Performance();
    parallel(tasks);
    return p.nanoRuntime();
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
