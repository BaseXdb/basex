package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;

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
    query(_JOBS_LIST.args() + "[. != " + _JOBS_CURRENT.args() + "] ! " + _JOBS_WAIT.args(" ."));
    // consume cached results
    query("for $id in " + _JOBS_LIST_DETAILS.args() + "[@cached = 'true'] " +
        " return try { " + _JOBS_RESULT.args(" $id") + " } catch * { }");
  }

  /** Test method. */
  @Test
  public void eval() {
    query(_JOBS_EVAL.args("1"));
    query(_JOBS_EVAL.args(".", " map { '': '1' }"));
    query(_JOBS_EVAL.args(".", " map { '': <a/> }"));
    query(_JOBS_EVAL.args("declare variable $a external;$a", " map { 'a': <a/> }"));
    query(_JOBS_EVAL.args("\"static-base-uri()\"", " map { 'base-uri': 'abc.xq' }"));
    query(_JOBS_EVAL.args("1", "()", " map{ 'id':'123' }"));

    // database creation
    error(_DB_OPEN.args("db"), BXDB_OPEN_X);
    query(_PROF_VOID.args(_JOBS_EVAL.args("\"db:open('db')\"")) + ',' + _DB_CREATE.args("db"));
    query(_JOBS_EVAL.args("\"db:drop('db')\"") + ',' + _PROF_VOID.args(_DB_OPEN.args("db")));
    query(_JOBS_EVAL.args("delete node <a/>"));

    // errors (will not be raised before runtime)
    query(_JOBS_EVAL.args("\"db:open('db')\""));
    query(_JOBS_EVAL.args("1+"));
    query(_JOBS_EVAL.args("1, delete node <a/>"));

    // errors
    error(_JOBS_EVAL.args("1", "()", " map{ 'start':'12345' }"), DATEFORMAT_X_X_X);
    error(_JOBS_EVAL.args("1", "()", " map{ 'interval':'12345' }"), DATEFORMAT_X_X_X);
    error(_JOBS_EVAL.args("1", "()", " map{ 'interval':'-PT1S' }"), JOBS_RANGE_X);
    error(_JOBS_EVAL.args("1", "()", " map{ 'id':'job123' }"), JOBS_ID_INVALID_X);
    error(_JOBS_EVAL.args("1", "()", " map{ 'id':'job123' }"), JOBS_ID_INVALID_X);
    error("(1,2)!" + _JOBS_EVAL.args(SLOW_QUERY, "()", " map{ 'id':'abc','cache':true() }"),
        JOBS_ID_EXISTS_X);
  }

  /** Test method. */
  @Test
  public void evalStart() {
    // delayed execution
    final String id = query(_JOBS_EVAL.args(" 'prof:sleep(200)'", "()", " map{'start':'PT0.2S'}"));
    // ensure that query is not run again
    Performance.sleep(100);
    query(_JOBS_FINISHED.args(id), "true");
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), "false");
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), "true");
  }

  /** Test method. */
  @Test
  public void evalInterval() {
    // scheduled execution
    final String id = query(_JOBS_EVAL.args(" 'prof:sleep(400)'", "()", " map{'interval':'PT1S'}"));
    // ensure that query is running
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), "false");
    // ensure that query is scheduled
    Performance.sleep(700);
    query(_JOBS_FINISHED.args(id), "true");
    // ensure that next query is running
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), "false");
    // stop query, wait
    query(_JOBS_STOP.args(id));
    Performance.sleep(400);
    // ensure that query is not run again
    query(_JOBS_FINISHED.args(id), "true");
    Performance.sleep(400);
    query(_JOBS_FINISHED.args(id), "true");
  }

  /** Test method. */
  @Test
  public void evalEnd() {
    // scheduled execution
    final String id = query(_JOBS_EVAL.args("123", "()", " map{'interval':'PT1S','end':'PT1.5S'}"));
    // ensure that query is running
    Performance.sleep(500);
    query(_JOBS_LIST.args() + "='" + id + "'", "true");
    Performance.sleep(1200);
    query(_JOBS_LIST.args() + "='" + id + "'", "false");

    // error
    error(_JOBS_EVAL.args("1", "()", " map{'start':'PT2S','interval':'PT1S','end':'PT1S'}"),
        JOBS_RANGE_X);
  }

  /** Test method. */
  @Test
  public void finished() {
    final String id = verySlowQuery();
    try {
      query(_JOBS_FINISHED.args(id), "false");
    } finally {
      query(_JOBS_STOP.args(id));
    }
    query(_JOBS_WAIT.args(id));
    query(_JOBS_FINISHED.args("12345"), "true");
  }

  /** Test method. */
  @Test
  public void list() {
    final String id = verySlowQuery();
    try {
      query(_JOBS_LIST.args() + " = '" + id + "'", "true");
    } finally {
      query(_JOBS_STOP.args(id));
    }
  }

  /** Test method. */
  @Test
  public void listDetails() {
    final String id = verySlowQuery();
    try {
      final String list = query(_JOBS_LIST_DETAILS.args() + "[@id = '" + id + "']");
      query(list + "/@user/string()", UserText.ADMIN);
      query(list + "/@state/string() = ('running', 'queued')", true);
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
    final String id = verySlowQuery();
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
      Performance.sleep(1);
    }
  }

  /**
   * Test method.
   * @throws IOException I/O exception */
  @Test
  public void result() throws IOException {
    // receive result of asynchronous execution
    query("let $q := " + _JOBS_EVAL.args(SLOW_QUERY, "()", " map{'cache':true()}") +
        " return ("
        + _HOF_UNTIL.args(" function($r) { " + _JOBS_FINISHED.args("$q") + " },"
            + "function($c) { prof:sleep(1) }, ()") + ","
        + _JOBS_RESULT.args("$q") + ')', "1");

    // ensure that the result will not be cached
    String id = query(_JOBS_EVAL.args(SLOW_QUERY));
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
      Performance.sleep(1);
    }

    // receive cached result
    id = query(_JOBS_EVAL.args(SLOW_QUERY, "()", " map{'cache':true()}"));
    while(true) {
      try {
        assertEquals("1", eval(_JOBS_RESULT.args(id)));
        break;
      } catch(final QueryException ex) {
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
      Performance.sleep(1);
    }

    // receive cached error
    id = query(_JOBS_EVAL.args("\"db:open('db')\"", "()", " map{'cache':true()}"));
    query(_JOBS_WAIT.args(id));
    error(_JOBS_RESULT.args(id), BXDB_OPEN_X);
  }

  /** Test method. */
  @Test
  public void waitFor() {
    query(_JOBS_WAIT.args(_JOBS_EVAL.args("1",  "()", " map { 'start':'PT0.1S' }")));
    error(_JOBS_WAIT.args(_JOBS_CURRENT.args()), JOBS_SELF_X);
  }

  /**
   * Waits until a very slow query has been started.
   * @return query id
   */
  private String verySlowQuery() {
    final String id = query(_JOBS_EVAL.args(VERY_SLOW_QUERY));
    while(context.jobs.active.get(id) == null) Performance.sleep(1);
    return id;
  }
}
