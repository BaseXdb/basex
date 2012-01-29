package org.basex.test.performance;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test class performs some incremental updates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
@RunWith(Parameterized.class)
public final class IncUpdateTest {
  /**
   * Test parameters.
   * @return parameters
   */
  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(new Object[][] { {false}, {true} });
  }

  /** Test database name. */
  private static final String DB = Util.name(IncUpdateTest.class);
  /** Database context. */
  protected static final Context CONTEXT = new Context();
  /** Number of steps. */
  private static final int STEPS = 10;
  /** Maximum number of entries. */
  private static final int MAX = 2000 / STEPS;

  /** Incremental index update flag. */
  private final boolean ixupdate;

  /**
   * Constructor.
   * @param u incremental index update flag.
   */
  public IncUpdateTest(final boolean u) {
    ixupdate = u;
  }

  /**
   * Initializes the test.
   * @throws Exception exception
   */
  @Before
  public void init() throws Exception {
    new Set(Prop.UPDINDEX, ixupdate).execute(CONTEXT);
    new CreateDB(DB, "<xml/>").execute(CONTEXT);
    new Set(Prop.AUTOFLUSH, false).execute(CONTEXT);
  }

  /**
   * Finishes the test.
   * @throws Exception exception
   */
  @After
  public void finish() throws Exception {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Incremental test.
   * @throws Exception exception
   */
  @Test
  public void insertInto() throws Exception {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> into /*");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   * @throws Exception exception
   */
  @Test
  public void insertBefore() throws Exception {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> before /*[1]");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   * @throws Exception exception
   */
  @Test
  public void insertAfter() throws Exception {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> after /*[last()]");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   * @throws Exception exception
   */
  @Test
  public void insertDeep() throws Exception {
    for(int a = 0; a < STEPS; a++) {
      final int n = MAX * a;
      for(int i = 0; i < n; i++) {
        query("insert node <x/> into //*[not(*)]");
      }
      query("count(//x)", n);
      query("delete node //x");
      query("count(//x)", 0);
    }
  }

  /**
   * Incremental test.
   * @throws Exception exception
   */
  @Test
  public void replaceValue() throws Exception {
    final Random rnd = new Random();
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < MAX * STEPS; i++) {
      sb.append((char) ('@' + (rnd.nextInt() & 0x1F)));
      query("replace value of node /* with '" + sb + "'");
      query("string-length(/*)", sb.length());
    }
  }

  /**
   * Runs the specified query.
   * @param query query string
   * @return result
   * @throws QueryException database exception
   */
  protected static String query(final String query) throws QueryException {
    final QueryProcessor qp = new QueryProcessor(query, CONTEXT);
    try {
      return qp.execute().toString().replaceAll("(\\r|\\n) *", "");
    } finally {
      try { qp.close(); } catch(final QueryException ex) { }
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query to be run
   * @param result query result
   * @throws QueryException database exception
   */
  protected static void query(final String query, final Object result)
      throws QueryException {
    assertEquals(result.toString(), query(query));
  }
}
