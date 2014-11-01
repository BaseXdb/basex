package org.basex.performance;

import static org.junit.Assert.*;

import java.util.*;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test class performs some incremental updates.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
@RunWith(Parameterized.class)
public final class UpdIndexTest extends SandboxTest {
  /**
   * Test parameters.
   * @return parameters
   */
  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(new Object[][] { { false }, { true } });
  }

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
  public UpdIndexTest(final boolean u) {
    ixupdate = u;
  }

  /**
   * Initializes the test.
   * @throws Exception exception
   */
  @Before
  public void init() throws Exception {
    new Set(MainOptions.UPDINDEX, ixupdate).execute(context);
    new CreateDB(NAME, "<xml/>").execute(context);
    new Set(MainOptions.AUTOFLUSH, false).execute(context);
  }

  /**
   * Finishes the test.
   * @throws Exception exception
   */
  @After
  public void finish() throws Exception {
    new DropDB(NAME).execute(context);
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
      query("replace value of node /* with '" + sb + '\'');
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
    try(final QueryProcessor qp = new QueryProcessor(query, context)) {
      return qp.execute().toString().replaceAll("(\\r|\\n) *", "");
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query to be run
   * @param result query result
   * @throws QueryException database exception
   */
  protected static void query(final String query, final Object result) throws QueryException {
    assertEquals(result.toString(), query(query));
  }
}
