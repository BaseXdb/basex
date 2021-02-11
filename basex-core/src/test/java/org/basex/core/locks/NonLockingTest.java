package org.basex.core.locks;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * This class checks the execution order of non-locking queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class NonLockingTest extends SandboxTest {
  /** Query for returning all jobs except for the current one. */
  private static final String LIST_JOBS = _JOBS_LIST.args() + '[' + _JOBS_CURRENT.args() + "!= .]";
  /** Very slow query. */
  private static final String SLEEP_10_SECONDS = _PROF_SLEEP.args(10000);

  /** Test. */
  @Test public void nonLockingBeforeWrite() {
    execute(new CreateDB(NAME));
    try {
      // start slow query
      new Thread(() -> query(SLEEP_10_SECONDS)).start();

      // start sleeping query
      String id;
      do id = query(LIST_JOBS); while(id.isEmpty());

      // local locking: add document in parallel
      query(_DB_ADD.args(NAME, " <a/>", "a.xml"));

      // global locking: add document in parallel; see GH-1400
      query("let $db := <a>" + NAME + "</a> return " + _DB_ADD.args(" $db", " <a/>", "a.xml"));

      // stop sleeping process, wait for its completion
      query(_JOBS_STOP.args(id));
      query(_JOBS_WAIT.args(id));

    } finally {
      execute(new DropDB(NAME));
    }
  }

  /** Test. */
  @Test public void nonLockingAfterLocalWrite() {
    nonLockingAfterWrite(_DB_CREATE.args(NAME));
  }

  /** Test. */
  @Test public void nonLockingAfterGlobalWrite() {
    nonLockingAfterWrite(_DB_CREATE.args(" <_>" + NAME + "</_>"));
  }

  /**
   * Test.
   * @param query locking query to run
   */
  private static void nonLockingAfterWrite(final String query) {
    try {
      // start slow query, global write lock
      new Thread(() -> query(SLEEP_10_SECONDS + ',' + query)).start();

      // check if query execution causes a longer delay.
      assertEquals("1", query("1"));

      // stop sleeping jobs
      query(LIST_JOBS + '!' + _JOBS_STOP.args(" ."));

    } finally {
      execute(new DropDB(NAME));
    }
  }
}
