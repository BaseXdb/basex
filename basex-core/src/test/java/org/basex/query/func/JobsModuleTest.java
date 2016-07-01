package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the functions of the Job Module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsModuleTest extends AdvancedQueryTest {
  /** Very slow query. */
  private static final String VERY_SLOW_QUERY = "\"(1 to 10000000000)[.=1]\"";
  /** Slow query. */
  private static final String SLOW_QUERY = "\"(1 to 10000000)[.=1]\"";

  /** Wait until all queries have been processed. */
  @After
  public void clean() {
    // wait for running jobs
    while(!query(_JOBS_LIST.args() + "[. != " + _JOBS_CURRENT.args() + "]").isEmpty()) {
      Performance.sleep(10);
    }
    // consume cached results
    query("for $id in " + _JOBS_RESULTS.args() +
        " return try { " + _JOBS_RESULT.args(" $id") + " } catch * { }");
  }

  /** Test method. */
  @Test
  public void schedule() {
    query(_JOBS_SCHEDULE.args("1"));
    query(_JOBS_SCHEDULE.args(".", " map { '': '1' }"));
    query(_JOBS_SCHEDULE.args(".", " map { '': <a/> }"));
    query(_JOBS_SCHEDULE.args("declare variable $a external;$a", " map { 'a': <a/> }"));
    query(_JOBS_SCHEDULE.args("\"static-base-uri()\"", " map { 'base-uri': 'abc.xq' }"));
    // some errors will only be detected at runtime
    query(_JOBS_SCHEDULE.args("\"db:open('db')\""));
    query(_JOBS_SCHEDULE.args("1+<a/>"));

    // database creation
    error(_DB_OPEN.args("db"), BXDB_OPEN_X);
    query(_PROF_VOID.args(_JOBS_SCHEDULE.args("\"db:open('db')\"")) + ',' + _DB_CREATE.args("db"));
    query(_JOBS_SCHEDULE.args("\"db:drop('db')\"") + ',' + _PROF_VOID.args(_DB_OPEN.args("db")));
    query(_JOBS_SCHEDULE.args("delete node <a/>"));

    // errors
    error(_JOBS_SCHEDULE.args("1+"), CALCEXPR);
    error(_JOBS_SCHEDULE.args("1, delete node <a/>"), UPALL);
  }

  /** Test method. */
  @Test
  public void finished() {
    final String id = query(_JOBS_SCHEDULE.args(VERY_SLOW_QUERY));
    try {
      query(_JOBS_FINISHED.args(id), "false");
    } finally {
      query(_JOBS_STOP.args(id));
    }
    while(query(_JOBS_FINISHED.args(id)).equals("false")) Performance.sleep(10);
    query(_JOBS_FINISHED.args("12345"), "true");
  }

  /** Test method. */
  @Test
  public void list() {
    final String id = query(_JOBS_SCHEDULE.args(VERY_SLOW_QUERY));
    try {
      query(_JOBS_LIST.args() + " = '" + id + "'", "true");
    } finally {
      query(_JOBS_STOP.args(id));
    }
  }

  /** Test method. */
  @Test
  public void listDetails() {
    final String id = query(_JOBS_SCHEDULE.args(VERY_SLOW_QUERY));
    try {
      final String list = query(_JOBS_LIST_DETAILS.args() + "[@id = '" + id + "']");
      query(list + "/@user/string()", UserText.ADMIN);
      query(list + "/@state/string()", JobState.RUNNING.toString().toLowerCase(Locale.ENGLISH));
      query(list + "/@duration/string() castable as xs:dayTimeDuration", "true");
    } finally {
      query(_JOBS_STOP.args(id));
    }
  }

  /**
   * Test method.
   * @throws IOException I/O exception */
  @Test
  public void stop() throws IOException {
    final String id = query(_JOBS_SCHEDULE.args(VERY_SLOW_QUERY));
    query(_JOBS_STOP.args(id));

    // check if query was interrupted
    while(true) {
      try {
        eval(_JOBS_RESULT.args(id));
        fail("Query was not stopped.");
      } catch(final QueryException ex) {
        // query was successfully stopped
        if(ex.error() == JOBS_UNKNOWN_X) break;
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
    }
  }

  /**
   * Test method.
   * @throws IOException I/O exception */
  @Test
  public void result() throws IOException {
    // receive result of asynchronous execution
    query("let $q := " + _JOBS_SCHEDULE.args(SLOW_QUERY, " map{}", " map{'cache':true()}") +
        " return ("
        + _HOF_UNTIL.args(" function($r) { " + _JOBS_FINISHED.args("$q") + " },"
            + "function($c) { Q{java:java.lang.Thread}yield() }, ()") + ","
        + _JOBS_RESULT.args("$q") + ')', "1");

    // ensure that the result will not be cached
    String id = query(_JOBS_SCHEDULE.args(SLOW_QUERY));
    while(true) {
      try {
        eval(_JOBS_RESULT.args(id));
        fail("Result was cached.");
      } catch(final QueryException ex) {
        // query was successfully stopped
        if(ex.error() == JOBS_UNKNOWN_X) break;
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
      Thread.yield();
    }

    // receive cached result
    id = query(_JOBS_SCHEDULE.args(SLOW_QUERY, " map{}", " map{'cache':true()}"));
    while(true) {
      try {
        assertEquals("1", eval(_JOBS_RESULT.args(id)));
        break;
      } catch(final QueryException ex) {
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
      Thread.yield();
    }

    // receive cached error
    id = query(_JOBS_SCHEDULE.args("\"db:open('db')\"", " map{}", " map{'cache':true()}"));
    while(query(_JOBS_FINISHED.args(id)).equals("false")) Performance.sleep(10);
    error(_JOBS_RESULT.args(id), BXDB_OPEN_X);
  }

  /**
   * Test method.
   */
  @Test
  public void results() {
    // check if result is cached
    String id = query(_JOBS_SCHEDULE.args("1", " map{}", " map{'cache':true()}"));
    while(query(_JOBS_FINISHED.args(id)).equals("false")) Performance.sleep(10);
    query(_JOBS_RESULTS.args(), id);

    // check if error is cached
    id = query(_JOBS_SCHEDULE.args("\"db:open('db')\"", " map{}", " map{'cache':true()}"));
    while(query(_JOBS_FINISHED.args(id)).equals("false")) Performance.sleep(10);
    query(_JOBS_RESULTS.args() + " ='" + id + "'", true);
  }
}
