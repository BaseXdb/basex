package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Job Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JobsModuleTest extends SandboxTest {
  /** Very slow query. */
  private static final String VERY_SLOW_QUERY = "(1 to 10000000000)[.=1]";
  /** Slow query. */
  private static final String SLOW_QUERY = "(1 to 10000000)[.=1]";

  /** Wait until all queries have been processed. */
  @AfterEach public void clean() {
    // wait for running jobs
    query(_JOBS_LIST.args() + "[. != " + _JOBS_CURRENT.args() + "] ! " + _JOBS_WAIT.args(" ."));
    // consume cached results
    query("for $id in " + _JOBS_LIST_DETAILS.args() + "[@cached = 'true'] " +
        " return try { " + _JOBS_RESULT.args(" $id") + " } catch * { }");
  }

  /** Test method. */
  @Test public void eval1() {
    final Function func = _JOBS_EVAL;
    query(func.args("1"));
    query(func.args(".", " map { '': '1' }"));
    query(func.args(".", " map { '': <a/> }"));
    query(func.args("declare variable $a external;$a", " map { 'a': <a/> }"));
    query(func.args("static-base-uri()", " map { 'base-uri': 'abc.xq' }"));
    query(func.args("1", " ()", " map { 'id':'123' }"));
  }

  /** Test method. */
  @Test public void eval2() {
    // database creation
    final Function func = _JOBS_EVAL;
    error(_DB_OPEN.args("db"), DB_OPEN2_X);
    query(_PROF_VOID.args(func.args("db:open('db')")) + ',' + _DB_CREATE.args("db"));
    query(func.args("db:drop('db')") + ',' + _PROF_VOID.args(_DB_OPEN.args("db")));
    query(func.args("delete node <a/>"));
  }

  /** Test method. */
  @Test public void eval3() {
    // errors (will not be raised before runtime)
    final Function func = _JOBS_EVAL;
    query(func.args("db:open('db')"));
    query(func.args("1+"));
    query(func.args("1, delete node <a/>"));

    // error in List implementation
    query("trace(true()) and (prof:void(" +
      func.args("prof:sleep(100)", " ()", " map { 'id': 'eval4' }") +
    "), true())", "true");
  }

  /** Test method. */
  @Test public void evalError() {
    // errors
    final Function func = _JOBS_EVAL;
    error(func.args("1", " ()", " map { 'start':'abc' }"), DATEFORMAT_X_X_X);
    error(func.args("1", " ()",
        " map { 'start':'2030-01-01T01:01:01','end':'2029-01-01T01:01:01' }"), JOBS_RANGE_X);
    error(func.args("1", " ()", " map { 'interval':'12345' }"), DATEFORMAT_X_X_X);
    error(func.args("1", " ()", " map { 'interval':'-PT1S' }"), JOBS_RANGE_X);
    error(func.args("1", " ()", " map { 'id':'job123' }"), JOBS_ID_INVALID_X);
    error(func.args("1", " ()", " map { 'id':'job123' }"), JOBS_ID_INVALID_X);
    error("(1,2)!" + func.args(SLOW_QUERY, " ()", " map { 'id': 'abc', 'cache': true() }"),
        JOBS_ID_EXISTS_X);
  }

  /** Test method. */
  @Test public void evalStart() {
    // delayed execution
    final Function func = _JOBS_EVAL;
    final String id = query(func.args(" 'prof:sleep(200)'", " ()", " map { 'start': 'PT0.2S' }"));
    // ensure that query is not run again
    Performance.sleep(100);
    query(_JOBS_FINISHED.args(id), true);
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), false);
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), true);
  }

  /** Test method. */
  @Test public void evalInterval() {
    // scheduled execution
    final Function func = _JOBS_EVAL;
    final String id = query(func.args("prof:sleep(400)", " ()", " map { 'interval': 'PT1S' }"));
    // ensure that query is running
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), false);
    // ensure that query is scheduled
    Performance.sleep(700);
    query(_JOBS_FINISHED.args(id), true);
    // ensure that next query is running
    Performance.sleep(200);
    query(_JOBS_FINISHED.args(id), false);
    // stop query, wait
    query(_JOBS_STOP.args(id));
    Performance.sleep(400);
    // ensure that query is not run again
    query(_JOBS_FINISHED.args(id), true);
    Performance.sleep(400);
    query(_JOBS_FINISHED.args(id), true);
  }

  /** Test method. */
  @Test public void evalEnd() {
    // scheduled execution
    final Function func = _JOBS_EVAL;
    final String id = query(func.args("123", " ()",
      " map { 'interval': 'PT1S', 'end': 'PT1.5S' }"));
    // ensure that query is running
    Performance.sleep(500);
    query(_JOBS_LIST.args() + "='" + id + '\'', true);
    Performance.sleep(1200);
    query(_JOBS_LIST.args() + "='" + id + '\'', false);

    // error
    error(func.args("1", " ()",
      " map { 'start': 'PT2S', 'interval': 'PT1S', 'end': 'PT1S' }"),
      JOBS_RANGE_X);
  }

  /** Test method. */
  @Test public void evalService() {
    final Function func = _JOBS_EVAL;
    query(func.args("1", " ()", " map { 'id': 'ID','service': true() }"));
    query("file:exists(db:option('dbpath') || '/jobs.xml')", true);
    query("exists(" + _JOBS_SERVICES.args() + "[@id = 'ID'])", true);
    query(_JOBS_STOP.args("id"));
    query("exists(" + _JOBS_SERVICES.args() + "[@id = 'ID'])", true);
    query(_JOBS_STOP.args("ID", " map { 'service':true() }"));
    query("exists(" + _JOBS_SERVICES.args() + "[@id = 'ID'])", false);
  }

  /** Test method. */
  @Test public void evalURI() {
    final Function func = _JOBS_EVAL;
    final String uri = " xs:anyURI('src/test/resources/input.xq')";
    query("starts-with(" + func.args(uri) + ", 'job')", true);
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void evalLog() {
    final Function func = _JOBS_EVAL;

    // write UUID into logs
    final String uuid = UUID.randomUUID().toString();
    final String id = query(func.args("1", null, " map { 'log': '" + uuid + "' }"));
    query(_JOBS_WAIT.args(id));
    // find log entry
    final String date = DateTime.format(new Date(), DateTime.DATE);
    query(_ADMIN_LOGS.args(date) + " = '" + uuid + "'", true);
  }


  /** Test method. */
  @Test public void finished() {
    final String id = verySlowQuery();
    try {
      query(_JOBS_FINISHED.args(id), false);
    } finally {
      query(_JOBS_STOP.args(id));
    }
    query(_JOBS_WAIT.args(id));
    query(_JOBS_FINISHED.args("12345"), true);
  }

  /** Test method. */
  @Test public void list() {
    final String id = verySlowQuery();
    try {
      query(_JOBS_LIST.args() + " = '" + id + '\'', true);
    } finally {
      query(_JOBS_STOP.args(id));
    }
  }

  /** Test method. */
  @Test public void listDetails() {
    final String id = verySlowQuery();
    try {
      final String list = query(_JOBS_LIST_DETAILS.args() + "[@id = '" + id + "']");
      query(list + "/@user/string()", UserText.ADMIN);
      query(list + "/@state/string() = ('running', 'queued')", true);
      query(list + "/@duration/string() castable as xs:dayTimeDuration", true);
    } finally {
      query(_JOBS_STOP.args(id));
    }
  }

  /**
   * Test method.
   * @throws IOException I/O exception */
  @Test public void stop() throws IOException {
    final String id = verySlowQuery();
    final Function func = _JOBS_STOP;
    query(func.args(id));

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
  @Test public void result() throws IOException {
    // receive result of asynchronous execution
    final Function func = _JOBS_RESULT;
    query("let $q := " + _JOBS_EVAL.args(SLOW_QUERY, " ()", " map { 'cache': true() }") +
        " return ("
        + _HOF_UNTIL.args(" function($r) { " + _JOBS_FINISHED.args(" $q") + " },"
            + "function($c) { prof:sleep(1) }, ()") + ','
        + func.args(" $q") + ')', 1);

    // ensure that the result will not be cached
    String id = query(_JOBS_EVAL.args(SLOW_QUERY));
    while(true) {
      try {
        eval(func.args(id));
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
    id = query(_JOBS_EVAL.args(SLOW_QUERY, " ()", " map { 'cache': true() }"));
    while(true) {
      try {
        assertEquals("1", eval(func.args(id)));
        break;
      } catch(final QueryException ex) {
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
      Performance.sleep(1);
    }

    // receive cached error
    id = query(_JOBS_EVAL.args("db:open('db')", " ()", " map { 'cache': true() }"));
    query(_JOBS_WAIT.args(id));
    error(func.args(id), DB_OPEN2_X);
  }

  /** Test method. */
  @Test public void waitFor() {
    final Function func = _JOBS_WAIT;
    query(func.args(_JOBS_EVAL.args("1",  " ()", " map { 'start': 'PT0.1S' }")));
    error(func.args(_JOBS_CURRENT.args()), JOBS_SELF_X);
  }

  /**
   * Waits until a very slow query has been started.
   * @return query id
   */
  private static String verySlowQuery() {
    final String id = query(_JOBS_EVAL.args(VERY_SLOW_QUERY));
    while(context.jobs.active.get(id) == null) Performance.sleep(1);
    return id;
  }
}
