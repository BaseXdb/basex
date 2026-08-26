package org.basex.local.multiple;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * This class performs local stress tests with a specified number of threads and queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class MultipleQueryTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = "src/test/resources/factbook.zip";
  /** Maximum position to retrieve. */
  private static final int MAX = 1000;
  /** Maximum delay between two queries (ms). */
  private static final int DELAY = 50;

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
    execute(new CreateDB(NAME, INPUT));
    // only request positions that exist
    final int max = Math.min(MAX, Integer.parseInt(query("count(//text())")));

    // run clients, each retrieving the nth text of the database
    final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
    for(int c = 0; c < clients; c++) {
      // one generator per client: a shared instance would not be reproducible
      final Random rnd = new Random(c);
      tasks.add(() -> {
        for(int r = 0; r < runs; r++) {
          Performance.sleep(rnd.nextInt(DELAY));
          // the position exists, so exactly one text node must be returned
          query("count((//text())[position() = " + (rnd.nextInt(max) + 1) + "])", 1);
        }
        return null;
      });
    }
    parallel(tasks);
    // drop database
    execute(new DropDB(NAME));
  }
}
