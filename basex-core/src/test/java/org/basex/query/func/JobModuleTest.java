package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.time.*;
import java.util.*;

import org.basex.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Job Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobModuleTest extends SandboxTest {
  /** Very slow query. */
  private static final String VERY_SLOW_QUERY = "(1 to 10000000000)[.=1]";
  /** Slow query. */
  private static final String SLOW_QUERY = "(1 to 10000000)[.=1]";
  /** Query that allocates a limited amount of memory and keeps it referenced. The variable is
   * accessed twice, so that the sequence is cached and stays reachable while the query runs. */
  private static final String BOUNDED_QUERY = "let $x := (1 to 3000000) ! string() return " +
      "(count($x[. = 'zzz']), (1 to 200000000)[. = -1], count($x[. = 'yyy']))";

  /** Wait until all queries have been processed. */
  @AfterEach public void clean() {
    // wait for running jobs
    query(_JOB_LIST.args() + "[. != " + _JOB_CURRENT.args() + "] ! " + _JOB_WAIT.args(" ."));
    // consume cached results
    query("for $id in " + _JOB_LIST_DETAILS.args() + "[@cached = 'true'] " +
        " return try { " + _JOB_RESULT.args(" $id") + " } catch * {}");
  }

  /** Test method. */
  @Test public void bindings() {
    final Function func = _JOB_BINDINGS;

    final int ms = 500;
    final String id = query(_JOB_EVAL.args("declare variable $ms external;"
        + "prof:sleep($ms)", " { 'ms': " + ms + " }"));
    Performance.sleep(ms / 2);
    query(func.args(id) + "?ms", ms);
  }

  /** Test method. */
  @Test public void eval1() {
    final Function func = _JOB_EVAL;
    query(func.args("1"));
    query(func.args(".", " { '': '1' }"));
    query(func.args(".", " { '': <a/> }"));
    query(func.args("declare variable $a external;$a", " { 'a': <a/> }"));
    query(func.args("static-base-uri()", " { 'base-uri': 'abc.xq' }"));
    query(func.args("1", " ()", " { 'id': '123' }"));
  }

  /** Test method. */
  @Test public void eval2() {
    // database creation
    final Function func = _JOB_EVAL;
    error(_DB_GET.args("db"), DB_GET2_X);
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
        func.args("prof:sleep(100)", " ()", " { 'id': 'eval4' }"))  + ", true())",
        "true");
  }

  /** Test method. */
  @Test public void evalError() {
    // errors
    final Function func = _JOB_EVAL;
    error(func.args("1", " ()", " { 'start': 'abc' }"), DATEFORMAT_X_X_X);
    error(func.args("1", " ()",
        " { 'start': '2030-01-01T01:01:01', 'end': '2029-01-01T01:01:01' }"), JOBS_RANGE_X);
    error(func.args("1", " ()", " { 'interval': '12345' }"), DATEFORMAT_X_X_X);
    error(func.args("1", " ()", " { 'interval': '-PT1S' }"), JOBS_RANGE_X);
    error(func.args("1", " ()", " { 'id': 'job123' }"), JOBS_ID_INVALID_X);
    error(func.args("1", " ()", " { 'id': 'job123' }"), JOBS_ID_INVALID_X);
    error("(1, 2) ! " + func.args(SLOW_QUERY, " ()", " { 'id': 'abc', 'cache': true() }"),
        JOBS_ID_EXISTS_X);
    error(func.args("1", " ()", " { 'cron': '* * *' }"), JOBS_CRON_X_X);
    error(func.args("1", " ()", " { 'cron': '0 0 30 2 *' }"), JOBS_CRON_X_X);
    error(func.args("1", " ()", " { 'cron': '* * * * *', 'interval': 'PT1S' }"), JOBS_OPTIONS_X_X);
    error(func.args("1", " ()", " { 'cron': '* * * * *', 'start': 'PT1S' }"), JOBS_OPTIONS_X_X);
    error(func.args("1", " ()", " { 'cron': '* * * * *', 'cache': true() }"), JOBS_OPTIONS_X_X);
  }

  /** Test method. */
  @Test public void evalMemory() {
    final Function func = _JOB_EVAL;
    final String id = query(func.args("(1 to 10000000000000) ! <a/>", " ()",
        " { 'cache': true(), 'memory': 10 }"));
    query(_JOB_WAIT.args(id));
    error(_JOB_RESULT.args(id), XQUERY_MEMORY);
  }

  /** Test method. */
  @Test public void evalTimeout() {
    final Function func = _JOB_EVAL;
    final String id = query(func.args(VERY_SLOW_QUERY, " ()",
        " { 'cache': true(), 'timeout': 0.1 }"));
    query(_JOB_WAIT.args(id));
    error(_JOB_RESULT.args(id), XQUERY_TIMEOUT);
  }

  /** Test method. */
  @Test public void evalPermission() {
    final Function func = _JOB_EVAL;
    query(_DB_CREATE.args(NAME));
    final String id = query(func.args(_DB_GET.args(NAME).trim(), " ()",
        " { 'cache': true(), 'permission': 'none' }"));
    query(_JOB_WAIT.args(id));
    error(_JOB_RESULT.args(id), BASEX_PERMISSION_X_X);
  }

  /** Test method: a job that allocates a limited amount of memory is stopped as well. */
  @Test public void evalMemoryBounded() {
    final Function func = _JOB_EVAL;
    final String id = query(func.args(BOUNDED_QUERY, " ()",
        " { 'cache': true(), 'memory': 1 }"));
    query(_JOB_WAIT.args(id));
    error(_JOB_RESULT.args(id), XQUERY_MEMORY);
  }

  /** Test method: of two jobs with a memory limit, only the greedy one is stopped. */
  @Test public void evalMemoryVictim() {
    final Function func = _JOB_EVAL;
    final String options = " { 'cache': true(), 'memory': 10 }";
    // job that allocates nothing, but whose limit is exceeded by the heap usage of the other job
    final String idle = query(func.args("prof:sleep(3000)", " ()", options));
    final String greedy = query(func.args("(1 to 10000000000000) ! <a/>", " ()", options));

    query(_JOB_WAIT.args(greedy));
    error(_JOB_RESULT.args(greedy), XQUERY_MEMORY);
    // the idle job was still running when the greedy one was stopped, and it survived
    query(_JOB_LIST.args() + " = '" + idle + '\'', true);
    query(_JOB_WAIT.args(idle));
    query(_JOB_RESULT.args(idle));
  }

  /** Test method. */
  @Test public void next() {
    final Function func = _JOB_NEXT;
    // a single date is returned by default
    query("count(" + func.args("* * * * *") + ')', 1);
    query("count(" + func.args("0 0 * * *", 5) + ')', 5);
    query("count(" + func.args("* * * * *", 0) + ')', 0);
    // all dates lie in the future and are returned in ascending order
    query("every $d in " + func.args("* * * * *", 3) + " satisfies $d > current-dateTime()", true);
    query("let $d := " + func.args("*/15 * * * *", 5) + " return deep-equal($d, sort($d))", true);
    // a daily job is triggered at midnight, on five different days
    query("every $d in " + func.args("0 0 * * *", 5) +
        " satisfies hours-from-dateTime($d) = 0 and minutes-from-dateTime($d) = 0", true);
    query("count(distinct-values(" + func.args("0 0 * * *", 5) + " ! xs:date(.)))", 5);
    // times are honored, weekends are skipped
    query("every $d in " + func.args("0 8 * * MON-FRI", 10) +
        " satisfies hours-from-dateTime($d) = 8", true);
    query("distinct-values(" + func.args("0 8 * * MON-FRI", 10) +
        " ! format-dateTime(., '[FNn]')) => sort()",
        "Friday\nMonday\nThursday\nTuesday\nWednesday");
    // expression that never matches
    query("count(" + func.args("0 0 30 2 *", 3) + ')', 0);
    // the query clock is used: repeated calls yield the same results
    query("let $a := " + func.args("* * * * * *") + " return (" +
        VOID.args(" prof:sleep(1100)") + ", $a eq " + func.args("* * * * * *") + ')', true);

    // errors
    error(func.args("* * *"), JOBS_CRON_X_X);
    error(func.args("* * * * 8"), JOBS_CRON_X_X);
    error(func.args("* * * * *", -1), JOBS_RANGE_X);
  }

  /** Test method. */
  @Test public void evalAnchor() {
    // a start time in the past anchors the phase of the interval: 30 seconds past the minute
    final Function func = _JOB_EVAL;
    final String id = query(func.args("1", " ()",
        " { 'start': '2020-01-01T00:00:30', 'interval': 'PT1M' }"));
    // the scheduled start may drift by some milliseconds
    final String seconds = query("floor(seconds-from-dateTime(xs:dateTime(" +
        _JOB_LIST_DETAILS.args(id) + "/@start)))");
    // remove before asserting: a repeating job that survives makes clean() wait forever
    query(_JOB_REMOVE.args(id));
    assertEquals("30", seconds);
  }

  /** Test method. */
  @Test public void evalCron() {
    // run every second, and check that the job is registered and repeated
    final Function func = _JOB_EVAL;
    final String id = query(func.args("prof:sleep(400)", " ()",
        " { 'cron': '* * * * * *', 'end': 'PT2.5S' }"));
    query(_JOB_LIST_DETAILS.args(id) + "/@cron/string()", "* * * * * *");
    Performance.sleep(1200);
    query(_JOB_LIST.args() + "='" + id + '\'', true);
    // job is removed after the end time has been exceeded
    Performance.sleep(2000);
    query(_JOB_LIST.args() + "='" + id + '\'', false);
  }

  /** Test method. */
  @Test public void evalStart() {
    // delayed execution
    final Function func = _JOB_EVAL;
    final String id = query(func.args(" 'prof:sleep(200)'", " ()", " { 'start': 'PT0.2S' }"));
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
    final String id = query(func.args("prof:sleep(400)", " ()", " { 'interval': 'PT1S' }"));
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
      " { 'interval': 'PT1S', 'end': 'PT1.5S' }"));
    // ensure that query is running
    Performance.sleep(500);
    query(_JOB_LIST.args() + "='" + id + '\'', true);
    Performance.sleep(1200);
    query(_JOB_LIST.args() + "='" + id + '\'', false);

    // error
    error(func.args("1", " ()",
      " { 'start': 'PT2S', 'interval': 'PT1S', 'end': 'PT1S' }"),
      JOBS_RANGE_X);
  }

  /** Test method. */
  @Test public void evalService() {
    final Function func = _JOB_EVAL;
    query(func.args("1", " ()", " { 'id': 'ID', 'service': true() }"));
    query(_FILE_EXISTS.args(_DB_OPTION.args("dbpath") + "|| '/jobs.xml'"), true);
    query("exists(" + _JOB_SERVICES.args() + "[@id = 'ID'])", true);
    query(_JOB_REMOVE.args("id"));
    query("exists(" + _JOB_SERVICES.args() + "[@id = 'ID'])", true);
    query(_JOB_REMOVE.args("ID", " { 'service': true() }"));
    query("exists(" + _JOB_SERVICES.args() + "[@id = 'ID'])", false);
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void evalServiceCron() throws IOException {
    // register a cron service: the expression is persisted
    final Function func = _JOB_EVAL;
    query(func.args("1", " ()",
        " { 'id': 'CRON', 'cron': '0 6 * * MON-FRI', 'service': true() }"));
    final String persisted = query(_JOB_SERVICES.args() + "[@id = 'CRON']/@cron/string()");

    // drop the scheduled job, keep the service, and re-register it as a restart would
    query(_JOB_REMOVE.args("CRON"));
    final String dropped = query("exists(" + _JOB_LIST_DETAILS.args("CRON") + ')');
    new Jobs(context).init();
    final String restored = query(_JOB_LIST_DETAILS.args("CRON") + "/@cron/string()");

    // remove job and service before asserting: a surviving cron job would stall clean()
    query(_JOB_REMOVE.args("CRON", " { 'service': true() }"));
    final String removed = query("exists(" + _JOB_SERVICES.args() + "[@id = 'CRON'])");

    assertEquals("0 6 * * MON-FRI", persisted);
    assertEquals("false", dropped);
    assertEquals("0 6 * * MON-FRI", restored);
    assertEquals("false", removed);
  }

  /** Test method. */
  @Test public void evalSleep() {
    final Function func = _JOB_EVAL;
    query(func.args("1", " ()", " { 'cache': true() }") + " ! (" +
        _PROF_SLEEP.args(500) + ", " + _JOB_LIST_DETAILS.args(" .") + "/@duration = 'PT0S'"
    + ")", true);
  }

  /** Test method. */
  @Test public void evalUri() {
    final Function func = _JOB_EVAL;
    final String uri = " xs:anyURI('src/test/resources/input.xq')";
    query("starts-with(" + func.args(uri) + ", 'job')", true);
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), RESWHICH_X);
  }

  /** Test method. */
  @Test public void evalLog() {
    final Function func = _JOB_EVAL;

    // write UUID into logs
    final String uuid = UUID.randomUUID().toString();
    final String id = query(func.args("1", " ()", " { 'log': '" + uuid + "' }"));
    query(_JOB_WAIT.args(id));
    // find log entry
    final String date = DateTime.DATE.format(LocalDate.now());
    query(_ADMIN_LOGS.args(date) + " = '" + uuid + "'", true);
  }

  /** Executes a job. */
  @Test public void execute() {
    final Function func = _JOB_EXECUTE;

    query(func.args("()"), "");
    query(func.args("1 + 2"), 3);
    query(func.args("(1 to 1000)[. = 0]"), "");
    query(func.args("(1 to 1000)[. = 1]"), 1);
    error(func.args("1 + ''"), CALCTYPE_X_X_X_X_X);
  }

  /** Invokes function items instead of query strings. */
  @Test public void executeFunction() {
    final Function func = _JOB_EXECUTE;

    query(func.args(" fn() { 1 + 1 }"), 2);
    query(func.args(" fn() { () }"), "");
    query(func.args(" fn($a) { $a * 2 }", " [ 21 ]"), 42);
    query(func.args(" fn($a, $b) { $a || $b }", " [ 'x', 'y' ]"), "xy");
    query(func.args(" fn($a) { $a }", " [ (1, 2, 3) ]"), "1\n2\n3");

    // partial application and function literals
    query(func.args(" concat('a', ?)", " [ 'b' ]"), "ab");
    query(func.args(" count#1", " [ (1, 2) ]"), 2);

    // closures over materialized values
    query("let $a := 40 return " + func.args(" fn() { $a + 2 }"), 42);
    query("let $n := <a>1</a> return " + func.args(" fn() { $n/text() }"), 1);

    // function items as arguments
    query(func.args(" fn($f) { $f() }", " [ fn() { 7 } ]"), 7);
    query(func.args(" fn($f) { $f(3) }", " [ fn($n) { $n * 3 } ]"), 9);

    // in-memory nodes are shared, so closure and argument stay the same node
    query("let $n := <a/> return " + func.args(" fn($x) { $x is $n }", " [ $n ]"), true);

    // calls of user-defined functions
    query("declare function local:f() { 5 }; " + func.args(" fn() { local:f() }"), 5);
    query("declare function local:g($n) { if($n) then local:g($n - 1) else 'done' }; " +
        func.args(" fn() { local:g(3) }"), "done");

    // the function keeps the static context of the query that created it
    query("declare default element namespace 'x'; " +
        func.args(" fn() { namespace-uri(<a/>) }"), "x");
  }

  /** Rejects function items that depend on the calling query. */
  @Test public void executeFunctionErrors() {
    final Function func = _JOB_EXECUTE;

    // arity mismatches
    error(func.args(" fn($a) { $a }"), APPLY_X_X);
    error(func.args(" fn($a) { $a }", " [ 1, 2 ]"), APPLY_X_X);
    error(func.args(" fn() { 1 }", " [ 1 ]"), APPLY_X_X);

    // arguments must be supplied as array
    error(func.args(" fn($a) { $a }", " { 'a': 1 }"), INVTYPE_X);
    // maps and arrays are neither queries nor invocable functions
    error(func.args(" { 'a': 1 }"), INVTYPE_X);
    error(func.args(" [ 1 ]"), INVTYPE_X);

    // dependencies on the calling query
    error(func.args(" fn() { . }"), BASEX_TRANSFER_X_X);
    error(func.args(" fn() { position() }"), BASEX_TRANSFER_X_X);
    error("declare variable $v := random:integer(); " + func.args(" fn() { $v }"),
        BASEX_TRANSFER_X_X);
    error(func.args(" fn() { Q{java:java.lang.Math}abs(-1) }"), BASEX_TRANSFER_X_X);
    // dependency in a function supplied as argument
    error(func.args(" fn($f) { $f() }", " [ fn() { . } ]"), BASEX_TRANSFER_X_X);

    // errors of the invoked function are passed on
    error(func.args(" fn() { 1 + '' }"), CALCTYPE_X_X_X_X_X);
  }

  /** Runs function items as asynchronous jobs. */
  @Test public void evalFunction() {
    final Function func = _JOB_EVAL;

    final String id1 = query(func.args(" fn() { 1 + 1 }", " ()", " { 'cache': true() }"));
    query(_JOB_WAIT.args(id1));
    query(_JOB_RESULT.args(id1), 2);

    final String id2 = query(func.args(" fn($a) { $a }", " [ 'x' ]", " { 'cache': true() }"));
    query(_JOB_WAIT.args(id2));
    query(_JOB_RESULT.args(id2), "x");
  }

  /** Rejects function items for services and scheduled jobs. */
  @Test public void evalFunctionScheduled() {
    final Function func = _JOB_EVAL;

    error(func.args(" fn() { 1 }", " ()", " { 'service': true() }"), JOBS_FUNCTION);
    error(func.args(" fn() { 1 }", " ()", " { 'start': 'PT1S' }"), JOBS_FUNCTION);
    error(func.args(" fn() { 1 }", " ()", " { 'interval': 'PT1S' }"), JOBS_FUNCTION);
    error(func.args(" fn() { 1 }", " ()", " { 'cron': '* * * * *' }"), JOBS_FUNCTION);
  }

  /** Computes database locks for jobs that invoke a function item. */
  @Test public void evalFunctionLocks() {
    final Function func = _JOB_EVAL;
    query(_DB_CREATE.args("db"));
    try {
      // a function job locks the same databases as the equivalent query
      final String body = "db:get('db'), prof:sleep(500)";
      final String fn = query(func.args(" fn() { " + body + " }", " ()",
          " { 'cache': true() }"));
      final String str = query(func.args(body, " ()", " { 'cache': true() }"));
      Performance.sleep(200);
      query(_JOB_LIST_DETAILS.args(fn) + "/@reads/string()", "db");
      query(_JOB_LIST_DETAILS.args(str) + "/@reads/string()", "db");
      query(_JOB_WAIT.args(fn));
      query(_JOB_WAIT.args(str));

      // the database is opened in the job's own context
      query(_JOB_EXECUTE.args(" fn() { count(db:get('db')) }"), 0);
    } finally {
      query(_DB_DROP.args("db"));
    }
  }

  /** Rejects jobs whose execution would deadlock the calling query. */
  @Test public void deadlock() {
    final Function func = _JOB_EXECUTE;
    query(_DB_CREATE.args("db"));
    query(_DB_CREATE.args("db2"));
    try {
      // caller read-locks a database, job would write-lock the same one: deadlock
      error(_DB_GET.args("db") + ", " + func.args("db:optimize('db')"), JOBS_DEADLOCK_X);
      // job resolves a constructed constant name to a specific lock: still conflicts, deadlock
      error(_DB_GET.args("db") + ", " + func.args("db:optimize(string(<a>db</a>))"),
          JOBS_DEADLOCK_X);
      // job acquires a global write lock (dynamic name), conflicting with the caller: deadlock
      error(_DB_GET.args("db") + ", " + func.args("db:optimize(db:list()[1])"),
          JOBS_DEADLOCK_X);
      // caller and job share a read lock: no conflict
      query(_DB_GET.args("db") + ", " + func.args("db:get('db')"));
      // caller and job touch different databases: no conflict
      query(_DB_GET.args("db") + ", " + func.args("db:optimize('db2')"));
      // caller holds no locks: job may lock freely
      query(func.args("db:optimize('db')"), "");
      // job:eval does not wait for the result and is therefore not guarded
      query(_DB_GET.args("db") + ", " + VOID.args(_JOB_EVAL.args("db:optimize('db')")));
      // a variable-bound database name resolves after compilation: different database, no deadlock
      query(_DB_GET.args("db") + ", " + func.args(
          "declare variable $d external; db:optimize($d)", " { 'd': 'db2' }"));
    } finally {
      query(_DB_DROP.args("db"));
      query(_DB_DROP.args("db2"));
    }
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
    assertEquals("", eval(_JOB_RESULT.args(id)));
  }

  /**
   * Test method.
   * @throws Exception exception
   */
  @Test public void result() throws Exception {
    // receive result of asynchronous execution
    final Function func = _JOB_RESULT;
    query("let $q :=" + _JOB_EVAL.args(SLOW_QUERY, " ()", " { 'cache': true() }") +
      " return (" + WHILE_DO.args(" ()",
        " function($_) { not(" + _JOB_FINISHED.args(" $q") + ") }",
        " function($_) { " + _PROF_SLEEP.args(1) + " }") + "," + func.args(" $q") + ")", 1);

    // ensure that the result will not be cached
    String id = query(_JOB_EVAL.args(SLOW_QUERY));
    assertEquals("", query(func.args(id)));

    // receive cached result
    id = query(_JOB_EVAL.args(SLOW_QUERY, " ()", " { 'cache': true() }"));
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
    final String id = query(_JOB_EVAL.args("()", " ()", " { 'cache': true() }"));
    query(_JOB_WAIT.args(id));
    query(_JOB_LIST_DETAILS.args(id), "");
    query(func.args(id), "");
  }

  /** Test method. */
  @Test public void resultRemove() {
    final Function func = _JOB_RESULT;
    final String id = query(_JOB_EVAL.args("1", " ()", " { 'cache': true() }"));
    query(_JOB_WAIT.args(id));
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', true);
    query(func.args(id, " { 'keep': true () }"), 1);
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', true);
    query(func.args(id, " { 'keep': true () }"), 1);
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', true);

    query(func.args(id, " { 'keep': false() }"), 1);
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', false);
    query(func.args(id, " { 'keep': false() }"), "");
    query("exists(" + _JOB_LIST_DETAILS.args(id) + ')', false);
  }

  /** Test method. */
  @Test public void resultError() {
    final Function func = _JOB_RESULT;
    final String id = query(_JOB_EVAL.args("db:get('db')", " ()", " { 'cache': true() }"));
    query(_JOB_WAIT.args(id));
    error(func.args(id), DB_GET2_X);
  }

  /** Test method. */
  @Test public void waitFor() {
    final Function func = _JOB_WAIT;
    query(func.args(_JOB_EVAL.args("1", " ()", " { 'start': 'PT0.1S' }")));
    error(func.args(_JOB_CURRENT.args()), JOBS_SELF_X);
  }

  /**
   * Waits until a very slow query has been started.
   * @return query ID
   */
  private static String verySlowQuery() {
    final String id = query(_JOB_EVAL.args(VERY_SLOW_QUERY));
    while(context.jobs.active.get(id) == null) Performance.sleep(1);
    return id;
  }
}
