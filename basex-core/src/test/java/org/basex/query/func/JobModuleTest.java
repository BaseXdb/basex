package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Job Module.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JobModuleTest extends SandboxTest {
  /** Very slow query. */
  private static final String VERY_SLOW_QUERY = "(1 to 10000000000)[.=1]";
  /** Slow query. */
  private static final String SLOW_QUERY = "(1 to 10000000)[.=1]";

  /** Wait until all queries have been processed. */
  @AfterEach public void clean() {
    // wait for running jobs
    query(_JOB_LIST.args() + "[. != " + _JOB_CURRENT.args() + "] ! " + _JOB_WAIT.args(" ."));
    // consume cached results
    query("for $id in " + _JOB_LIST_DETAILS.args() + "[@cached = 'true'] " +
        " return try { " + _JOB_RESULT.args(" $id") + " } catch * { }");
  }

  /** Test method. */
  @Test public void bindings() {
    final Function func = _JOB_BINDINGS;

    final int ms = 500;
    final String id = query(_JOB_EVAL.args("declare variable $ms external;"
        + "prof:sleep($ms)", " map { 'ms': " + ms + " }"));
    Performance.sleep(ms / 2);
    query(func.args(id) + "?ms", ms);
  }

  /** Test method. */
  @Test public void eval1() {
    final Function func = _JOB_EVAL;
    query(func.args("1"));
    query(func.args(".", " map { '': '1' }"));
    query(func.args(".", " map { '': <a/> }"));
    query(func.args("declare variable $a external;$a", " map { 'a': <a/> }"));
    query(func.args("static-base-uri()", " map { 'base-uri': 'abc.xq' }"));
    query(func.args("1", " ()", " map { 'id': '123' }"));
  }

  /** Test method. */
  @Test public void eval2() {
    // database creation
    final Function func = _JOB_EVAL;
    error(_DB_GET.args("db"), DB_OPEN2_X);
    query(VOID.args(func.args("db:get('db')")) + ',' + _DB_CREATE.args("db"));
    query(func.args("db:drop('db')") + ',' + VOID.args(_DB_GET.args("db")));
    query(func.args("delete node <a/>"));
  }

  /** Test method. */
  @Test public void eval3() {
    // errors (will not be raised before runtime)
    final Function func = _JOB_EVAL;
    query(func.args("db:get('db')"));
    query(func.args("1+"));
    query(func.args("1, delete node <a/>"));

    // error in List implementation
    query("trace(true()) and (" + VOID.args(
        func.args("prof:sleep(100)", " ()", " map { 'id': 'eval4' }"))  + ", true())",
        "true");
  }

  /** Test method. */
  @Test public void evalError() {
    // errors
    final Function func = _JOB_EVAL;
    error(func.args("1", " ()", " map { 'start': 'abc' }"), DATEFORMAT_X_X_X);
    error(func.args("1", " ()",
        " map { 'start': '2030-01-01T01:01:01', 'end': '2029-01-01T01:01:01' }"), JOBS_RANGE_X);
    error(func.args("1", " ()", " map { 'interval': '12345' }"), DATEFORMAT_X_X_X);
    error(func.args("1", " ()", " map { 'interval': '-PT1S' }"), JOBS_RANGE_X);
    error(func.args("1", " ()", " map { 'id': 'job123' }"), JOBS_ID_INVALID_X);
    error(func.args("1", " ()", " map { 'id': 'job123' }"), JOBS_ID_INVALID_X);
    error("(1, 2) ! " + func.args(SLOW_QUERY, " ()", " map { 'id': 'abc', 'cache': true() }"),
        JOBS_ID_EXISTS_X);
  }

  /** Test method. */
  @Test public void evalStart() {
    // delayed execution
    final Function func = _JOB_EVAL;
    final String id = query(func.args(" 'prof:sleep(200)'", " ()", " map { 'start': 'PT0.2S' }"));
    // ensure that query is not run again
    Performance.sleep(100);
    query(_JOB_FINISHED.args(id), true);
    Performance.sleep(200);
    query(_JOB_FINISHED.args(id), false);
    Performance.sleep(200);
    query(_JOB_FINISHED.args(id), true);
  }

  /** Test method. */
  @Test public void evalInterval() {
    // scheduled execution
    final Function func = _JOB_EVAL;
    final String id = query(func.args("prof:sleep(400)", " ()", " map { 'interval': 'PT1S' }"));
    // ensure that query is running
    Performance.sleep(200);
    query(_JOB_FINISHED.args(id), false);
    // ensure that query is scheduled
    Performance.sleep(700);
    query(_JOB_FINISHED.args(id), true);
    // ensure that next query is running
    Performance.sleep(200);
    query(_JOB_FINISHED.args(id), false);
    // stop query, wait
    query(_JOB_REMOVE.args(id));
    Performance.sleep(400);
    // ensure that query is not run again
    query(_JOB_FINISHED.args(id), true);
    Performance.sleep(400);
    query(_JOB_FINISHED.args(id), true);
  }

  /** Test method. */
  @Test public void evalEnd() {
    // scheduled execution
    final Function func = _JOB_EVAL;
    final String id = query(func.args("123", " ()",
      " map { 'interval': 'PT1S', 'end': 'PT1.5S' }"));
    // ensure that query is running
    Performance.sleep(500);
    query(_JOB_LIST.args() + "='" + id + '\'', true);
    Performance.sleep(1200);
    query(_JOB_LIST.args() + "='" + id + '\'', false);

    // error
    error(func.args("1", " ()",
      " map { 'start': 'PT2S', 'interval': 'PT1S', 'end': 'PT1S' }"),
      JOBS_RANGE_X);
  }

  /** Test method. */
  @Test public void evalService() {
    final Function func = _JOB_EVAL;
    query(func.args("1", " ()", " map { 'id': 'ID', 'service': true() }"));
    query(_FILE_EXISTS.args(_DB_OPTION.args("dbpath") + "|| '/jobs.xml'"), true);
    query("exists(" + _JOB_SERVICES.args() + "[@id = 'ID'])", true);
    query(_JOB_REMOVE.args("id"));
    query("exists(" + _JOB_SERVICES.args() + "[@id = 'ID'])", true);
    query(_JOB_REMOVE.args("ID", " map { 'service': true() }"));
    query("exists(" + _JOB_SERVICES.args() + "[@id = 'ID'])", false);
  }

  /** Test method. */
  @Test public void evalSleep() {
    final Function func = _JOB_EVAL;
    query(func.args("1", " ()", " map { 'cache': true() }") + " ! (" +
        _PROF_SLEEP.args(500) + ", " + _JOB_LIST_DETAILS.args(" .") + "/@duration = 'PT0S'"
    + ")", true);
  }

  /** Test method. */
  @Test public void evalUri() {
    final Function func = _JOB_EVAL;
    final String uri = " xs:anyURI('src/test/resources/input.xq')";
    query("starts-with(" + func.args(uri) + ", 'job')", true);
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void evalLog() {
    final Function func = _JOB_EVAL;

    // write UUID into logs
    final String uuid = UUID.randomUUID().toString();
    final String id = query(func.args("1", " ()", " map { 'log': '" + uuid + "' }"));
    query(_JOB_WAIT.args(id));
    // find log entry
    final String date = DateTime.format(new Date(), DateTime.DATE);
    query(_ADMIN_LOGS.args(date) + " = '" + uuid + "'", true);
  }

  /** Test method. */
  @Test public void finished() {
    final String id = verySlowQuery();
    try {
      query(_JOB_FINISHED.args(id), false);
    } finally {
      query(_JOB_REMOVE.args(id));
    }
    query(_JOB_WAIT.args(id));
    query(_JOB_FINISHED.args("12345"), true);
  }

  /** Test method. */
  @Test public void list() {
    final String id = verySlowQuery();
    try {
      query(_JOB_LIST.args() + " = '" + id + '\'', true);
    } finally {
      query(_JOB_REMOVE.args(id));
    }
  }

  /** Test method. */
  @Test public void listDetails() {
    final String id = verySlowQuery();
    try {
      final String list = query(_JOB_LIST_DETAILS.args() + "[@id = '" + id + "']");
      query(list + "/@user/string()", UserText.ADMIN);
      query(list + "/@state/string() = ('running', 'queued')", true);
      query(list + "/@duration/string() castable as xs:dayTimeDuration", true);
    } finally {
      query(_JOB_REMOVE.args(id));
    }
  }

  /**
   * Test method.
   * @throws Exception exception */
  @Test public void remove() throws Exception {
    final Function func = _JOB_REMOVE;

    final String id = verySlowQuery();
    try {
      eval(_JOB_RESULT.args(id));
    } catch(final QueryException ex) {
      // query is still running: check error code
      assertSame(JOBS_RUNNING_X, ex.error());
    }

    query(func.args(id));
    Performance.sleep(100);

    // check if query was successfully stopped
    assertEquals(eval(_JOB_RESULT.args(id)), "");
  }

  /**
   * Test method.
   * @throws Exception exception
   */
  @Test public void result() throws Exception {
    // receive result of asynchronous execution
    final Function func = _JOB_RESULT;
    query("let $q :=" + _JOB_EVAL.args(SLOW_QUERY, " ()", " map { 'cache': true() }") +
      " return (" + ITERATE_WHILE.args(" ()",
        " function($_) { not(" + _JOB_FINISHED.args(" $q") + ") }",
        " function($_) { " + _PROF_SLEEP.args(1) + " }") + "," + func.args(" $q") + ")", 1);

    // ensure that the result will not be cached
    String id = query(_JOB_EVAL.args(SLOW_QUERY));
    assertEquals(query(func.args(id)), "");

    // receive cached result
    id = query(_JOB_EVAL.args(SLOW_QUERY, " ()", " map { 'cache': true() }"));
    while(true) {
      try {
        assertEquals("1", eval(func.args(id)));
        break;
      } catch(final QueryException ex) {
        // query is still running: check error code
        assertSame(JOBS_RUNNING_X, ex.error());
      }
      Performance.sleep(10);
    }
  }

  /** Test method. */
  @Test public void resultEmpty() {
    final Function func = _JOB_RESULT;
    final String id = query(_JOB_EVAL.args("()", " ()", " map { 'cache': true() }"));
    query(_JOB_WAIT.args(id));
    query(_JOB_LIST_DETAILS.args(id), "");
    query(func.args(id), "");
  }

  /** Test method. */
  @Test public void resultRemove() {
    final Function func = _JOB_RESULT;
    final String id = query(_JOB_EVAL.args("1", " ()", " map { 'cache': true() }"));
    query(_JOB_WAIT.args(id));
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', true);
    query(func.args(id, " map { 'keep': true () }"), 1);
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', true);
    query(func.args(id, " map { 'keep': true () }"), 1);
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', true);

    query(func.args(id, " map { 'keep': false() }"), 1);
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', false);
    query(func.args(id, " map { 'keep': false() }"), "");
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', false);
  }

  /** Test method. */
  @Test public void resultError() {
    final Function func = _JOB_RESULT;
    final String id = query(_JOB_EVAL.args("db:get('db')", " ()", " map { 'cache': true() }"));
    query(_JOB_WAIT.args(id));
    error(func.args(id), DB_OPEN2_X);
  }

  /** Test method. */
  @Test public void waitFor() {
    final Function func = _JOB_WAIT;
    query(func.args(_JOB_EVAL.args("1", " ()", " map { 'start': 'PT0.1S' }")));
    error(func.args(_JOB_CURRENT.args()), JOBS_SELF_X);
  }

  /**
   * Waits until a very slow query has been started.
   * @return query id
   */
  private static String verySlowQuery() {
    final String id = query(_JOB_EVAL.args(VERY_SLOW_QUERY));
    while(context.jobs.active.get(id) == null) Performance.sleep(1);
    return id;
  }
}
