package org.basex.local.single;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * Performs bulk updates with standalone version.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class XQUFStressTest extends SandboxTest {
  /** Number of node updates. */
  private static final int NRNODES = 100;

  /** Tests the insert statement. */
  @Test public void insert10() {
    insert(10);
  }

  /** Tests the insert statement. */
  @Test public void insert100() {
    insert(100);
  }

  /** Tests the insert statement. */
  @Test public void insert1000() {
    insert(1000);
  }

  /**
   * Tests the insert statement.
   * @param runs number of runs
   */
  private void insert(final int runs) {
    for(int r = 0; r < runs; r++) {
      execute(new CreateDB(NAME, "<doc/>"));
      // insert query
      query(
        "for $i in 1 to " + NRNODES + " return insert node " +
        "<section><page/></section> into /doc");
      // actual query
      query(
        "for $page in //page " +
        "let $par := $page/.. " +
        "return (delete node $page, insert node $page before $par)");
      execute(new DropDB(NAME));
    }
  }

  /** Tests the delete statement. */
  @Test public void delete10() {
    delete(10);
  }

  /** Tests the delete statement. */
  @Test public void delete100() {
    delete(100);
  }

  /** Tests the delete statement. */
  @Test public void delete1000() {
    delete(1000);
  }

  /**
   * Tests the delete statement.
   * @param runs number of runs
   */
  private void delete(final int runs) {
    execute(new CreateDB(NAME, "<doc/>"));
    for(int r = 0; r < runs; r++) {
      query("for $i in 1 to " + NRNODES + " return insert node <node/> into /doc");
      query("delete nodes //node");
    }
    execute(new DropDB(NAME));
  }
}
