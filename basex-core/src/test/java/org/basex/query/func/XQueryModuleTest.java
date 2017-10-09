package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the XQuery Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class XQueryModuleTest extends AdvancedQueryTest {
  /** Path to test file. */
  private static final String PATH = "src/test/resources/input.xml";

  /** Test method. */
  @Test
  public void eval() {
    query(_XQUERY_EVAL.args("1"), 1);
    query(_XQUERY_EVAL.args("1 + 2"), 3);
    query(_XQUERY_EVAL.args("\"declare variable $a external; $a\"", " map { '$a': 'b' }"), "b");
    query(_XQUERY_EVAL.args("\"declare variable $a external; $a\"", " map { 'a': 'b' }"), "b");
    query(_XQUERY_EVAL.args("\"declare variable $a external; $a\"", " map { 'a': (1,2) }"), "1\n2");
    query(_XQUERY_EVAL.args("\"declare variable $local:a external; $local:a\"",
        " map { xs:QName('local:a'): 1 }"), "1");
    query(_XQUERY_EVAL.args(".", " map { '': 1 }"), "1");
    error(_XQUERY_EVAL.args("1+"), CALCEXPR);
    error("declare variable $a:=1;" + _XQUERY_EVAL.args("\"$a\""), VARUNDEF_X);
    error("for $a in (1,2) return " + _XQUERY_EVAL.args("\"$a\""), VARUNDEF_X);
    // check updating expressions
    error(_XQUERY_EVAL.args("delete node ()"), BXXQ_UPDATING);
    error(_XQUERY_EVAL.args("declare %updating function local:x() {()}; local:x()"),
        BXXQ_UPDATING);
    query(_XQUERY_EVAL.args("declare %updating function local:x() {()}; 1"));
    query(_XQUERY_EVAL.args('"' + DOC.args(PATH).replace('"', '\'') + '"'));

    // check additional options
    query(_DB_CREATE.args('"' + NAME + '"'));
    query("try { " + _XQUERY_EVAL.args("\"(1 to 10000000000000)[. = 0]\"", " map { }",
        " map { 'timeout': 1 }") + " } catch * { () }", "");
    query(_XQUERY_EVAL.args(" 'static-base-uri()'", " map { }",
        " map { 'base-uri': 'http://x.x/' }"), "http://x.x/");

    error(_XQUERY_EVAL.args(" '" + DOC.args(NAME) + '\'', " map { }",
        " map { 'permission': 'none' }"), BXXQ_PERM_X);
    error(_XQUERY_EVAL.args(" '" + _DB_OPEN.args(NAME) + '\'', " map { }",
        " map { 'permission': 'none' }"), BXDB_OPEN_X);
    error(_XQUERY_EVAL.args(" '" + _FILE_EXISTS.args("x") + '\'', " map { }",
        " map { 'permission': 'none' }"), BXXQ_PERM_X);
    error(_XQUERY_EVAL.args("\"(1 to 10000000000000)[. = 0]\"", " map { }",
        " map { 'timeout': 1 }"), BXXQ_TIMEOUT);
    error(_XQUERY_EVAL.args("\"(1 to 10000000000000) ! <a/>\"", " map { }",
        " map { 'memory': 10 }"), BXXQ_MEMORY);
  }

  /** Test method. */
  @Test
  public void update() {
    query(_XQUERY_UPDATE.args("delete node <a/>"));
    query(_XQUERY_UPDATE.args(" '" + _DB_OUTPUT.args(1) + '\''), "1");
    query(_XQUERY_UPDATE.args(" '()'"));
    error(_XQUERY_UPDATE.args("1"), BXXQ_NOUPDATE);
  }

  /** Test method. */
  @Test
  public void invoke() {
    query(_XQUERY_INVOKE.args("src/test/resources/input.xq"), "XML");
    error(_XQUERY_INVOKE.args("src/test/resources/xxx.xq"), WHICHRES_X);
  }

  /** Test method. */
  @Test
  public void forkJoin() {
    // pass on one or more functions
    query(_XQUERY_FORK_JOIN.args(" true#0"), "true");
    query(_XQUERY_FORK_JOIN.args("(false#0,true#0)"), "false\ntrue");
    query(_XQUERY_FORK_JOIN.args(" function() { 123 }"), "123");
    query("count(" + _XQUERY_FORK_JOIN.args(" (1 to 100) ! false#0") + ')', "100");

    // run slow and fast query and check that results are returned in the correct order
    query(_XQUERY_FORK_JOIN.args("(function() { (1 to 10000000)[.=1] }, true#0)"), "1\ntrue");
    query(_XQUERY_FORK_JOIN.args("(true#0, function() { (1 to 10000000)[.=1] })"), "true\n1");
    query(_XQUERY_FORK_JOIN.args("()"), "");

    // errors
    error(_XQUERY_FORK_JOIN.args(" count#1"), ZEROFUNCS_X_X);
    error(_XQUERY_FORK_JOIN.args(" 123"), ZEROFUNCS_X_X);
    error(_XQUERY_FORK_JOIN.args(" error#0"), FUNERR1);
  }

  /** Test method. */
  @Test
  public void parse() {
    query(_XQUERY_PARSE.args("1") + "/name()", "MainModule");
    query(_XQUERY_PARSE.args("1") + "/@updating/string()", "false");
    query(_XQUERY_PARSE.args("1") + "/QueryPlan/@compiled/string()", "false");

    query(_XQUERY_PARSE.args("1", " map{'compile':true()}") + "/QueryPlan/@compiled/string()",
        "true");
    query(_XQUERY_PARSE.args("1", " map{'plan':false()}") + "/QueryPlan", "");

    query(_XQUERY_PARSE.args("module namespace x='x'; "
        + "declare function x:x() { 1 };") + "/name()", "LibraryModule");

    query(_XQUERY_PARSE.args("delete node <a/>") + "/name()", "MainModule");
    query(_XQUERY_PARSE.args("delete node <a/>") + "/@updating/string()", "true");

    error(_XQUERY_PARSE.args("1+"), CALCEXPR);
    query("\n\ntry {" + _XQUERY_PARSE.args("1+",
        " map{'pass':true()}") + "} catch * { $err:line-number }", "1");

    query("contains(try {" + _XQUERY_PARSE.args("1+",
        " map { 'base-uri': 'XXXX', 'pass': 'true' }") + "} catch * { $err:module }, 'XXXX')",
        "true");
  }

  /** Test method. */
  @Test
  public void parseUri() {
    query(_XQUERY_PARSE_URI.args("src/test/resources/input.xq") + "/name()", "MainModule");
    query(_XQUERY_PARSE_URI.args("src/test/resources/input.xq") + "/@updating/string()", "false");
    error(_XQUERY_PARSE_URI.args("src/test/resources/xxx.xq"), WHICHRES_X);
  }
}
