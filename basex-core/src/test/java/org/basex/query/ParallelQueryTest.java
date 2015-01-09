package org.basex.query;

import static org.junit.Assert.*;

import java.util.*;

import org.basex.*;
import org.junit.*;

/**
 * Runs parallel queries.
 *
 * @author BaseX Team 2005-15, BSD License
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
  @Test
  public void test() throws Throwable {
    // generate reference result
    result = query();
    // generate results to be compared
    final ArrayList<Query> queries = new ArrayList<>();
    for(int i = 0; i < 10; i++) queries.add(new Query());
    for(final Query q : queries) q.start();
    for(final Query q : queries) q.join();
    if(error != null) throw error;
  }

  /**
   * Runs a single query.
   * @return result
   * @throws QueryException exception
   */
  private String query() throws QueryException {
    try(final QueryProcessor qp = new QueryProcessor(QUERY, context)) {
      return qp.value().toString();
    }
  }

  /**
   * Query instance.
   */
  private class Query extends Thread {
    @Override
    public void run() {
      try {
        assertEquals(result, query());
      } catch(final Throwable th) {
        error = th;
      }
    }
  }
}
