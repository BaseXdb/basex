package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Async Module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class AsyncModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void forkJoin() {
    // pass on one or more functions
    query(_ASYNC_FORK_JOIN.args(" true#0"), "true");
    query(_ASYNC_FORK_JOIN.args("(false#0,true#0)"), "false\ntrue");
    query(_ASYNC_FORK_JOIN.args(" function() { 123 }"), "123");
    query("count(" + _ASYNC_FORK_JOIN.args(" (1 to 100) ! false#0") + ")", "100");

    // run slow and fast query and check that results are returned in the correct order
    query(_ASYNC_FORK_JOIN.args("(function() { (1 to 10000000)[.=1] }, true#0)"), "1\ntrue");
    query(_ASYNC_FORK_JOIN.args("(true#0, function() { (1 to 10000000)[.=1] })"), "true\n1");

    // try different options
    query(_ASYNC_FORK_JOIN.args(" (false#0,true#0)", " map {'threads':1 }"), "false\ntrue");
    query(_ASYNC_FORK_JOIN.args(" (false#0,true#0)", " map {'threads':64 }"), "false\ntrue");
    query(_ASYNC_FORK_JOIN.args(" (false#0,true#0)", " map {'thread-size':1 }"), "false\ntrue");
    query(_ASYNC_FORK_JOIN.args(" (false#0,true#0)", " map {'thread-size':1000 }"), "false\ntrue");

    // errors
    error(_ASYNC_FORK_JOIN.args(" count#1"), ZEROFUNCS_X_X);
    error(_ASYNC_FORK_JOIN.args(" 123"), ZEROFUNCS_X_X);
    error(_ASYNC_FORK_JOIN.args(" error#0"), FUNERR1);
    error(_ASYNC_FORK_JOIN.args(" ()", " map {'threads':-1 }"), ASYNC_ARG_X);
    error(_ASYNC_FORK_JOIN.args(" ()", " map {'threads':100000000 }"), ASYNC_ARG_X);
    error(_ASYNC_FORK_JOIN.args(" ()", " map {'thread-size':0 }"), ASYNC_ARG_X);
    error(_ASYNC_FORK_JOIN.args(" ()", " map {'thread-size':-1 }"), ASYNC_ARG_X);
  }

  /** Test method. */
  @Test
  public void eval() {
    query(_ASYNC_EVAL.args("1"));
    query(_ASYNC_EVAL.args(".", " map { '': '1' }"));
    query(_ASYNC_EVAL.args(".", " map { '': <a/> }"));
    query(_ASYNC_EVAL.args("declare variable $a external;$a", " map { 'a': <a/> }"));
    query(_ASYNC_EVAL.args("\"static-base-uri()\"", " map { 'base-uri': 'abc.xq' }"));
    // some errors will only be detected at runtime
    query(_ASYNC_EVAL.args("\"db:open('db')\""));
    query(_ASYNC_EVAL.args("1+<a/>"));

    // database creation
    error(_DB_OPEN.args("db"), BXDB_OPEN_X);
    query(_PROF_VOID.args(_ASYNC_EVAL.args("\"db:open('db')\"")) + ',' + _DB_CREATE.args("db"));
    query(_ASYNC_UPDATE.args("\"db:drop('db')\"") + ',' + _PROF_VOID.args(_DB_OPEN.args("db")));

    // errors
    error(_ASYNC_EVAL.args("delete node <a/>"), ASYNC_UPDATING);
    error(_ASYNC_EVAL.args("1+"), CALCEXPR);
  }

  /** Test method. */
  @Test
  public void update() {
    query(_ASYNC_UPDATE.args("delete node <a/>"));

    error(_ASYNC_UPDATE.args("1"), ASYNC_NOUPDATE);
    error(_ASYNC_UPDATE.args("1, delete node <a/>"), UPALL);
  }

  /** Test method. */
  @Test
  public void isRunning() {
    final String id = query(_ASYNC_EVAL.args("\"(1 to 1000000)[.=0]\""));
    query(_ASYNC_IS_RUNNING.args(id), "true");
  }

  /** Test method. */
  @Test
  public void ids() {
    final String id = query(_ASYNC_EVAL.args("\"(1 to 100000000000)[.=0]\""));
    query(_ASYNC_IDS.args() + " = '" + id + "'", "true");
    query(_ASYNC_IDS.args() + " ! " + _ASYNC_STOP.args(" ."));
  }

  /** Test method. */
  @Test
  public void stop() {
    final String id = query(_ASYNC_EVAL.args("\"(1 to 100000000000)[.=0]\""));
    query(_ASYNC_STOP.args(id));

    // check if query was interrupted
    while(true) {
      try {
        eval(_ASYNC_RESULT.args(id));
        fail("Query was not stopped.");
      } catch(final QueryException ex) {
        // query was successfully stopped
        if(ex.error() == ASYNC_WHICH_X) break;
        // query is still running: check error code
        assertSame(ASYNC_RUNNING_X, ex.error());
      } catch(final IOException ex) {
        fail(ex.toString());
      }
    }
  }

  /** Test method. */
  @Test
  public void result() {
    final String query = "\"(1 to 1000000)[.=1]\"";
    // receive result of asynchronous execution
    query("let $query := async:eval(" + query + ") "
        + "return (hof:until("
        + "  function($result) { not(async:is-running($query)) },"
        + "  function($curr) { prof:sleep(10) },"
        + "  ()"
        + "), async:result($query))", "1");

    // receive asynchronous result
    String id = query(_ASYNC_EVAL.args(query));
    while(true) {
      try {
        assertEquals("1", eval(_ASYNC_RESULT.args(id)));
        break;
      } catch(final QueryException ex) {
        // query is still running: check error code
        assertSame(ASYNC_RUNNING_X, ex.error());
      } catch(final IOException ex) {
        fail(ex.toString());
      }
    }

    // receive asynchronous result
    id = query(_ASYNC_EVAL.args(query, " map {}", " map { 'cache': false() }"));
    while(true) {
      try {
        eval(_ASYNC_RESULT.args(id));
        fail("Result was cached.");
      } catch(final QueryException ex) {
        // query was successfully stopped
        if(ex.error() == ASYNC_WHICH_X) break;
        // query is still running: check error code
        assertSame(ASYNC_RUNNING_X, ex.error());
      } catch(final IOException ex) {
        fail(ex.toString());
      }
    }
  }
}
