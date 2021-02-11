package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Runs parallel queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ParallelQueryTest extends SandboxTest {
  /** Query. */
  private static final String QUERY = "count((for $i in 1 to 50000 return <a><b/></a>)/b)";
  /** Error. */
  private Throwable error;
  /** Reference result. */
  private String result;

  /**
   * Test.
   * @throws Throwable throwable
   */
  @Test public void test() throws Throwable {
    // generate reference result
    result = query(QUERY);
    // generate results to be compared
    final ArrayList<Query> queries = new ArrayList<>();
    for(int i = 0; i < 10; i++) queries.add(new Query());
    for(final Query q : queries) q.start();
    for(final Query q : queries) q.join();
    if(error != null) throw error;
  }

  /**
   * Query instance.
   */
  private class Query extends Thread {
    @Override
    public void run() {
      try {
        assertEquals(result, query(QUERY));
      } catch(final Throwable th) {
        error = th;
      }
    }
  }
}
