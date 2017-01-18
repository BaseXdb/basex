package org.basex.core.locks;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.Test;

/**
 * This class checks the execution order of non-locking queries.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class NonLockingTest extends SandboxTest {
  /** Query for returning all jobs except for the current one. */
  private static final String LIST_JOBS = _JOBS_LIST.args() + '[' + _JOBS_CURRENT.args() + "!= .]";
  /** Very slow query. */
  private static final String VERY_SLOW_QUERY = _PROF_SLEEP.args(1000000);

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void nonLockingBeforeWrite() throws Exception {
    execute(new CreateDB(NAME));
    try {
      // start slow query
      new Thread() {
        @Override
        public void run() {
          try {
            new XQuery(VERY_SLOW_QUERY).execute(context);
          } catch(final Exception ignored) { }
        }
      }.start();

      // start sleeping query
      String id;
      do id = new XQuery(LIST_JOBS).execute(context); while(id.isEmpty());

      // local locking: add document in parallel
      new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")).execute(context);

      // global locking: add document in parallel; see GH-1400
      new XQuery("let $db := <a>" + NAME + "</a> return " + _DB_ADD.args("$db", "<a/>", "a.xml")).
        execute(context);

      // stop sleeping process, wait for its completion
      new XQuery(_JOBS_STOP.args(id)).execute(context);
      new XQuery(_JOBS_WAIT.args(id)).execute(context);

    } finally {
      new DropDB(NAME).execute(context);
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void noLimitForNonLocking() throws Exception {
    // start a bunch of small queries
    final AtomicInteger ai = new AtomicInteger();
    final int count = 10;
    for(int c = 0; c < count; c++) {
      new Thread() {
        @Override
        public void run() {
          try {
            ai.incrementAndGet();
            new XQuery(VERY_SLOW_QUERY).execute(context);
          } catch(final Exception ignored) { }
        }
      }.start();
    }
    // wait until all threads have been started
    while(ai.get() < count) Thread.yield();

    // check if this query is run before the sleeping queries are finished
    assertEquals("1", new XQuery("1").execute(context));

    // stop sleeping jobs
    execute(new XQuery(LIST_JOBS + '!' + _JOBS_STOP.args(" .")));
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void nonLockingAfterLocalWrite() throws Exception {
    nonLockingAfterWrite(_DB_CREATE.args(NAME));
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void nonLockingAfterGlobalWrite() throws Exception {
    nonLockingAfterWrite(_DB_CREATE.args("<_>" + NAME + "</_>"));
  }

  /**
   * Test.
   * @param query locking query to run
   * @throws Exception exception
   */
  private static void nonLockingAfterWrite(final String query) throws Exception {
    try {
      // start slow query, global write lock
      new Thread() {
        @Override
        public void run() {
          try {
            new XQuery(VERY_SLOW_QUERY + ',' + query).execute(context);
          } catch(final Exception ignored) { }
        }
      }.start();

      // check if this query is run before the sleeping queries are finished
      assertEquals("1", new XQuery("1").execute(context));

      // stop sleeping jobs
      execute(new XQuery(LIST_JOBS + '!' + _JOBS_STOP.args(" .")));

    } finally {
      new DropDB(NAME).execute(context);
    }
  }
}
