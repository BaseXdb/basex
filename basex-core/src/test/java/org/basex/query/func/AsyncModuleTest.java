package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

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
}
