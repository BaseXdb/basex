package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.query.*;
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
  private static final String SLOW_QUERY = "\"(1 to 1000000)[.=1]\"";

  /** Test method. */
  @Test
  public void eval() {
    query(_JOBS_EVAL.args("1"));
    query(_JOBS_EVAL.args(".", " map { '': '1' }"));
    query(_JOBS_EVAL.args(".", " map { '': <a/> }"));
    query(_JOBS_EVAL.args("declare variable $a external;$a", " map { 'a': <a/> }"));
    query(_JOBS_EVAL.args("\"static-base-uri()\"", " map { 'base-uri': 'abc.xq' }"));
    // some errors will only be detected at runtime
    query(_JOBS_EVAL.args("\"db:open('db')\""));
    query(_JOBS_EVAL.args("1+<a/>"));

    // database creation
    error(_DB_OPEN.args("db"), BXDB_OPEN_X);
    query(_PROF_VOID.args(_JOBS_EVAL.args("\"db:open('db')\"")) + ',' + _DB_CREATE.args("db"));
    query(_JOBS_EVAL.args("\"db:drop('db')\"") + ',' + _PROF_VOID.args(_DB_OPEN.args("db")));
    query(_JOBS_EVAL.args("delete node <a/>"));

    // errors
    error(_JOBS_EVAL.args("1+"), CALCEXPR);
    error(_JOBS_EVAL.args("1, delete node <a/>"), UPALL);
  }

  /** Test method. */
  @Test
  public void finished() {
    final String id = query(_JOBS_EVAL.args(VERY_SLOW_QUERY));
    query(_JOBS_FINISHED.args(id), "false");
    query(_JOBS_STOP.args(id));
    while(query(_JOBS_STOP.args(id)).equals("false")) Thread.yield();
    query(_JOBS_FINISHED.args("12345"), "true");
  }

  /** Test method. */
  @Test
  public void list() {
    final String id = query(_JOBS_EVAL.args(SLOW_QUERY));
    query(_JOBS_LIST.args() + " = '" + id + "'", "true");
  }

  /**
   * Test method.
   * @throws IOException I/O exception */
  @Test
  public void stop() throws IOException {
    final String id = query(_JOBS_EVAL.args(VERY_SLOW_QUERY));
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
    query("let $query := " + _JOBS_EVAL.args(SLOW_QUERY) + " return ("
        + _HOF_UNTIL.args("  function($result) { "
        + _JOBS_FINISHED.args("$query") + " }, function($curr) { prof:sleep(100) }, ()") + ","
        + _JOBS_RESULT.args("$query") + ')', "1");

    // receive asynchronous result
    String id = query(_JOBS_EVAL.args(SLOW_QUERY));
    while(true) {
      try {
        assertEquals("1", eval(_JOBS_RESULT.args(id)));
        break;
      } catch(final QueryException ex) {
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
    }

    // receive asynchronous result
    id = query(_JOBS_EVAL.args(SLOW_QUERY, " map {}", " map { 'cache': false() }"));
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
    }
  }
}
