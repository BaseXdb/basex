package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Runs parallel queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ParallelQueryTest extends SandboxTest {
  /** Query. */
  private static final String QUERY = "count((for $i in 1 to 50000 return <a><b/></a>)/b)";
  /** Number of parallel queries. */
  private static final int THREADS = 10;

  /**
   * Runs the same query in parallel and compares all results to a reference result.
   * @throws Exception exception
   */
  @Test @Timeout(60) public void test() throws Exception {
    final String expected = query(QUERY);
    parallel(THREADS, () -> {
      assertEquals(expected, query(QUERY));
      return null;
    });
  }
}
