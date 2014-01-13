package org.basex.server;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.junit.*;

/**
 * Runs parallel queries.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ParallelLocalSessionTest {
  /** Query. */
  private static final String QUERY = "count((for $i in 1 to 50000 return <a><b/></a>)/b)";

  /** Context. */
  private final Context context = new Context();
  /** Error. */
  private Throwable error;
  /** Reference result. */
  String result;

  /**
   * Test.
   * @throws Throwable throwable
   */
  @Ignore
  @Test
  public void test() throws Throwable {
    // generate reference result
    result = query();
    // generate results to be compared
    final List<LocalQuery> queries = new ArrayList<LocalQuery>();
    for(int i = 0; i < 10; i++) queries.add(new LocalQuery());
    for(final LocalQuery q : queries) q.start();
    for(final LocalQuery q : queries) q.join();
    if(error != null) throw error;
  }

  /**
   * Runs a single query.
   * @return result
   * @throws IOException exception
   */
  private String query() throws IOException {
    final LocalSession cl = new LocalSession(context);
    final Query query = cl.query(QUERY);
    try {
      return query.next();
    } finally {
      cl.close();
    }
  }

  /**
   * Query instance.
   */
  private class LocalQuery extends Thread {
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
