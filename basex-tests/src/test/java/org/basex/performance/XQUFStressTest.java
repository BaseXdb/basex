package org.basex.performance;

import org.basex.core.cmd.*;
import org.basex.*;
import org.junit.*;

/**
 * Performs bulk updates with standalone version.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class XQUFStressTest extends SandboxTest {
  /** Number of node updates. */
  private static final int NRNODES = 100;

  /**
   * Tests the insert statement.
   * @throws Exception exception
   */
  @Test
  public void insert10() throws Exception {
    insert(10);
  }

  /**
   * Tests the insert statement.
   * @throws Exception exception
   */
  @Test
  public void insert100() throws Exception {
    insert(100);
  }

  /**
   * Tests the insert statement.
   * @throws Exception exception
   */
  @Test
  public void insert1000() throws Exception {
    insert(1000);
  }

  /**
   * Tests the insert statement.
   * @param runs number of runs
   * @throws Exception exception
   */
  private void insert(final int runs) throws Exception {
    for(int r = 0; r < runs; r++) {
      new CreateDB(NAME, "<doc/>").execute(context);
      // insert query
      new XQuery("for $i in 1 to " + NRNODES + " return insert node " +
          "<section><page/></section> into /doc").execute(context);
      // actual query
      new XQuery(
        "for $page in //page " +
        "let $par := $page/.. " +
        "return (delete node $page, insert node $page before $par)").
        execute(context);
      new DropDB(NAME).execute(context);
    }
  }

  /**
   * Tests the delete statement.
   * @throws Exception exception
   */
  @Test
  public void delete10() throws Exception {
    delete(10);
  }

  /**
   * Tests the delete statement.
   * @throws Exception exception
   */
  @Test
  public void delete100() throws Exception {
    delete(100);
  }

  /**
   * Tests the delete statement.
   * @throws Exception exception
   */
  @Test
  public void delete1000() throws Exception {
    delete(1000);
  }

  /**
   * Tests the delete statement.
   * @param runs number of runs
   * @throws Exception exception
   */
  private void delete(final int runs) throws Exception {
    new CreateDB(NAME, "<doc/>").execute(context);
    for(int r = 0; r < runs; r++) {
      new XQuery("for $i in 1 to " + NRNODES +
          " return insert node <node/> into /doc").execute(context);
      new XQuery("delete nodes //node").execute(context);
    }
    new DropDB(NAME).execute(context);
  }
}
