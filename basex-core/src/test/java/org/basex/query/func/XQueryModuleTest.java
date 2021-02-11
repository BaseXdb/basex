package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functions of the XQuery Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQueryModuleTest extends SandboxTest {
  /** Path to test file. */
  private static final String PATH = "src/test/resources/input.xml";

  /** Test method. */
  @Test public void eval() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args("1"), 1);
    query(func.args("1 + 2"), 3);
    error(func.args("1+"), CALCEXPR);
    error("declare variable $a:=1;" + func.args("$a"), VARUNDEF_X);
    error("for $a in (1,2) return " + func.args("$a"), VARUNDEF_X);
    // check updating expressions
    error(func.args("delete node ()"), XQUERY_UPDATE1);
    error(func.args("declare %updating function local:x() {()}; local:x()"), XQUERY_UPDATE1);
    query(func.args("declare %updating function local:x() {()}; 1"));
    query(func.args(DOC.args(PATH).trim()));
  }

  /** Test method. */
  @Test public void evalBaseUri() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args("static-base-uri()", " map { }",
      " map { 'base-uri': 'http://x.x/' }"), "http://x.x/");
  }

  /** Test method. */
  @Test public void evalPermission() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(_DB_CREATE.args(NAME));
    error(func.args(DOC.args(NAME).trim(), " map { }",
      " map { 'permission': 'none' }"), XQUERY_PERMISSION1_X);
    error(func.args(_DB_OPEN.args(NAME).trim(), " map { }",
      " map { 'permission': 'none' }"), XQUERY_PERMISSION1_X);
    error(func.args(_FILE_EXISTS.args("x").trim(), " map { }",
      " map { 'permission': 'none' }"), XQUERY_PERMISSION1_X);
  }

  /** Test method. */
  @Test public void evalMemory() {
    final Function func = _XQUERY_EVAL;
    // queries
    error(func.args("(1 to 10000000000000) ! <a/>", " map { }",
        " map { 'memory': 10 }"), XQUERY_MEMORY);
  }

  /** Test method. */
  @Test public void evalTimeout() {
    final Function func = _XQUERY_EVAL;
    // queries
    query("try { " + func.args("(1 to 10000000000000)[. = 0]", " map { }",
        " map { 'timeout': 1 }") + " } catch * { () }", "");
    error(func.args("(1 to 10000000000000)[. = 0]", " map { }",
        " map { 'timeout': 1 }"), XQUERY_TIMEOUT);
  }

  /** Test method. */
  @Test public void evalBindings() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args("declare variable $a external; $a", " map { '$a': 'b' }"), "b");
    query(func.args("declare variable $a external; $a", " map { 'a': 'b' }"), "b");
    query(func.args("declare variable $a external; $a", " map { 'a': (1,2) }"), "1\n2");
    query(func.args("declare variable $local:a external; $local:a",
      " map { xs:QName('local:a'): 1 }"), 1);
    query(func.args(".", " map { '': 1 }"), 1);

    // ensure that global bindings will not overwrite local bindings
    execute(new Set(MainOptions.BINDINGS, "a=X"));
    try {
      error(func.args("declare variable $a external; $a", " ()"), VAREMPTY_X);
      query(func.args("declare variable $a external; $a", " map { '$a': 'b' }"), "b");
    } finally {
      execute(new Set(MainOptions.BINDINGS, ""));
    }
  }

  /** Test method. */
  @Test public void evalUpdate() {
    final Function func = _XQUERY_EVAL_UPDATE;
    // queries
    query(func.args("delete node <a/>"));
    query(func.args("update:output(1)"), 1);
    query(func.args("()"));
    error(func.args("1"), XQUERY_UPDATE2);
  }

  /** Test method. */
  @Test public void evalUpdateUri() {
    final Function func = _XQUERY_EVAL_UPDATE;
    // queries
    error(func.args(" xs:anyURI('src/test/resources/input.xq')"), XQUERY_UPDATE2);
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void evalUri() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args(" xs:anyURI('src/test/resources/input.xq')"), "XML");
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void forkJoin() {
    final Function func = _XQUERY_FORK_JOIN;
    // pass on one or more functions
    query(func.args(" true#0"), true);
    query(func.args(" (false#0,true#0)"), "false\ntrue");
    query(func.args(" function() { 123 }"), 123);
    query("count(" + func.args(" (1 to 100) ! false#0") + ')', 100);

    // run slow and fast query and check that results are returned in the correct order
    query(func.args(" (function() { (1 to 10000000)[.=1] }, true#0)"), "1\ntrue");
    query(func.args(" (true#0, function() { (1 to 10000000)[.=1] })"), "true\n1");
    query(func.args(" ()"), "");

    // errors
    error(func.args(" count#1"), ZEROFUNCS_X_X);
    error(func.args(" 123"), ZEROFUNCS_X_X);
    error(func.args(" error#0"), FUNERR1);
  }

  /** Test method. */
  @Test public void parse() {
    final Function func = _XQUERY_PARSE;
    // queries
    query(func.args("1") + "/name()", "MainModule");
    query(func.args("1") + "/@updating/string()", false);
    query(func.args("1") + "/QueryPlan/@compiled/string()", false);

    query(func.args("1", " map { 'compile': true() }") + "/QueryPlan/@compiled/string()", true);
    query(func.args("1", " map { 'plan': false() }") + "/QueryPlan", "");

    query(func.args("module namespace x='x'; "
        + "declare function x:x() { 1 };") + "/name()", "LibraryModule");

    query(func.args("delete node <a/>") + "/name()", "MainModule");
    query(func.args("delete node <a/>") + "/@updating/string()", true);

    error(func.args("1+"), CALCEXPR);
    query("\n\ntry {" + func.args("1 +",
        " map { 'pass': true() }") + "} catch * { $err:line-number }", 1);

    query("contains(try {" + func.args("1 +",
        " map { 'base-uri': 'XXXX', 'pass': 'true' }") + "} catch * { $err:module }, 'XXXX')",
        true);
  }

  /** Test method. */
  @Test public void parseUri() {
    final Function func = _XQUERY_PARSE_URI;
    // queries
    query(func.args("src/test/resources/input.xq") + "/name()", "MainModule");
    query(func.args("src/test/resources/input.xq") + "/@updating/string()", false);
    error(func.args("src/test/resources/xxx.xq"), WHICHRES_X);
  }
}
