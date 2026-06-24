package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Timeout.ThreadMode.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the XQuery Module.
 *
 * @author BaseX Team, BSD License
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
    error("declare variable $a := 1;" + func.args("$a"), VARUNDEF_X);
    error("for $a in (1, 2) return" + func.args("$a"), VARUNDEF_X);

    // check updating expressions
    error(func.args("delete node ()"), XQUERY_NOUPDATES);
    error(func.args("declare %updating function local:x() {()}; local:x()"), XQUERY_NOUPDATES);
    query(func.args("declare %updating function local:x() {()}; 1"));
    query(func.args(DOC.args(PATH).trim()));

    // GH-2332
    query("try {" + func.args("declare function local:f() { local:f() }; local:f()") +
        "} catch xquery:error { 'STOP' }", "STOP");

    // GH-2640
    query("element e { attribute { QName('', 'a') } {}, " + func.args("true()") + " }",
        "<e a=\"\">true</e>");
    query("""
<e xmlns:p='p'
   p:a='{
     map:keys(in-scope-namespaces(<x/>))
   }'
   p:b='{xquery:eval('
     map:keys(in-scope-namespaces(<x/>))
   ')}'/>""", "<e xmlns:p=\"p\" p:a=\"p xml\" p:b=\"xml\"/>");
  }

  /** Test method. */
  @Test public void evalBaseUri() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args("static-base-uri()", " {}", " { 'base-uri': 'http://x.x/' }"),
        "http://x.x/");
  }

  /** Test method. */
  @Test public void evalPermission() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(_DB_CREATE.args(NAME));
    error(func.args(DOC.args(NAME).trim(), " {}", " { 'permission': 'none' }"),
        XQUERY_PERM_X);
    error(func.args(_DB_GET.args(NAME).trim(), " {}", " { 'permission': 'none' }"),
        XQUERY_PERM_X);
    error(func.args(_FILE_EXISTS.args("x").trim(), " {}", " { 'permission': 'none' }"),
        XQUERY_PERM_X);
  }

  /** Test method. */
  @Test public void evalMemory() {
    final Function func = _XQUERY_EVAL;
    // queries
    error(func.args("(1 to 10000000000000) ! <a/>", " {}", " { 'memory': 10 }"),
        XQUERY_MEMORY);
  }

  /** Test method. */
  @Test public void evalTimeout() {
    final Function func = _XQUERY_EVAL;
    // queries
    query("try { " + func.args("(1 to 10000000000000)[. = 0]", " {}", " { 'timeout': 1 }") +
        " } catch * { () }", "");
    error(func.args("(1 to 10000000000000)[. = 0]", " {}", " { 'timeout': 1 }"),
        XQUERY_TIMEOUT);
    error(func.args("(1 to 10000000000000)[. = 0]", " {}", " { 'timeout': .1 }"),
        XQUERY_TIMEOUT);
  }

  /** Test method. */
  @Test public void evalBindings() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args("declare variable $a external; $a", " { '$a': 'b' }"), "b");
    query(func.args("declare variable $a external; $a", " { 'a': 'b' }"), "b");
    query(func.args("declare variable $a external; $a", " { 'a': (1, 2) }"), "1\n2");
    query(func.args("declare variable $local:a external; $local:a",
      " { xs:QName('local:a'): 1 }"), 1);
    query(func.args(".", " { '': 1 }"), 1);

    // ensure that global bindings will not overwrite local bindings
    set(MainOptions.BINDINGS, "a=X");
    try {
      error(func.args("declare variable $a external; $a", " ()"), VAREMPTY_X);
      query(func.args("declare variable $a external; $a", " { '$a': 'b' }"), "b");
    } finally {
      set(MainOptions.BINDINGS, "");
    }
  }

  /** Test method. */
  @Test public void evalUpdate() {
    final Function func = _XQUERY_EVAL_UPDATE;
    // queries
    query(func.args("delete node <a/>"));
    query(func.args("update:output(1)"), 1);
    query(func.args("()"));
    error(func.args("1"), XQUERY_UPDATEEXPECTED);
  }

  /** Test method. */
  @Test public void evalUpdateUri() {
    final Function func = _XQUERY_EVAL_UPDATE;
    // queries
    error(func.args(" xs:anyURI('src/test/resources/input.xq')"), XQUERY_UPDATEEXPECTED);
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), RESWHICH_X);
  }

  /** Test method. */
  @Test public void evalUri() {
    final Function func = _XQUERY_EVAL;
    // queries
    query(func.args(" xs:anyURI('src/test/resources/input.xq')"), "XML");
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), RESWHICH_X);
  }

  /** Test method. */
  @Test public void forkJoin() {
    final Function func = _XQUERY_FORK_JOIN;
    // pass on one or more functions
    query(func.args(" true#0"), true);
    query(func.args(" (false#0, true#0)"), "false\ntrue");
    query(func.args(" function() { 123 }"), 123);
    query("count(" + func.args(" (1 to 100) ! false#0") + ')', 100);
    query(func.args(VOID.args(1)), "");
    query(func.args(" (true#0," + VOID.args(1) + ')'), true);
    query(func.args(" true#0[" + wrap(1) + " = '1']"), true);
    query(func.args(" true#0[" + wrap(1) + " = '']"), "");

    // run slow and fast query and check that results are returned in the correct order
    query(func.args(" (function() { (1 to 10000000)[. = 1] }, true#0)"), "1\ntrue");
    query(func.args(" (true#0, function() { (1 to 10000000)[. = 1] })"), "true\n1");
    query(func.args(" ()"), "");

    // options
    query(func.args(" (1 to 2) ! function() { 1 }", " { 'results': false() }"), "");
    query(func.args(" (error#0, true#0)", " { 'errors': false() }"), true);
    query(func.args(" (true#0, false#0)", " { 'parallel': -1 }"), "true\nfalse");
    query(func.args(" (true#0, false#0)", " { 'parallel': 100 }"), "true\nfalse");
    query(func.args(" (true#0, false#0)", " { 'parallel': 1000000000 }"), "true\nfalse");
    query(func.args(" (true#0, false#0)", " { 'parallel': <_>1</_> }"), "true\nfalse");

    // MapMerge mutable instance variables
    query("""
xquery:fork-join(
  for $t in 1 to 100
  return fn() {
    for $i in 1 to 10
    return map:build(($i, $i), (), (), { 'duplicates': op('*') })($i)
  }
) => count()
        """, 1000);
    // dynamic namespace context
    query("""
(for $i in 1 to 100
 return fn() {
   (1 to $i)
   ! <e xmlns:a='a' xmlns:b='b' xmlns:c='c' xmlns:d='d' xmlns:e='e'>{
     ('a', 'b', 'c', 'd', 'e') ! fn:namespace-uri-for-prefix(., <e/>)
   }</e>
   => distinct-values()
 }()
) => distinct-values()
        """, "a b c d e");
    // GH-2640: fork-join inherits parent dynamic namespace context
    query("""
<e xmlns:p='p' p:a='{
  xquery:fork-join(
   (1 to 100) ! fn() { map:keys(in-scope-namespaces(<x/>)) }
 )[. = 'p'] => count()
}'/>
        """, "<e xmlns:p=\"p\" p:a=\"100\"/>");

    // optimizations
    check(func.args(" ()"), "", empty());
    check(func.args(" false#0"), false, root(DynFuncCall.class));

    // errors
    final String updating = " %updating function() { delete node <a/> }";
    error(func.args(updating), FUNCUP_X);
    error(func.args(" (" + updating + ", " + updating + ')'), FUNCUP_X);
    error(func.args(" (" + updating + ", " + updating + ")[number(<_>1</_>)]"), FUNCUP_X);
    error(func.args(" (" + updating + ", " + updating + ")[" + wrap(1) + ']'), FUNCUP_X);

    error(func.args(" count#1"), INVARITY_X_X);
    error(func.args(" 123"), INVTYPE_X);
    error(func.args(" (count#1, count#1)"), INVARITY_X_X);
    error(func.args(" (123, 123)"), INVTYPE_X);
    error(func.args(" error#0"), FUNERR1);
    error(func.args(" replicate(error#0, 100)"), FUNERR1);
  }

  /** Test method. */
  @Test public void forkJoinReport() {
    final Function func = _XQUERY_FORK_JOIN;
    final String report = " { 'report': true() }";
    // successful results are wrapped in records
    query(func.args(" (true#0, false#0)", report) + " ! ?value", "true\nfalse");
    // errors are captured, not raised (single function: option bypasses the direct-call rewrite)
    query("exists(" + func.args(" error#0", report) + "?error)", true);
    query(func.args(" error#0", report) + "?error?code => local-name-from-QName()", "FOER0000");
    // mixed outcomes: count successes and failures (key existence)
    query("let $r := " + func.args(" (true#0, error#0, false#0)", report) +
        " return (count($r[exists(?value)]), count($r[exists(?error)]))", "2\n1");
    // the reported error is the canonical catch map ($err:map), with no self-referential 'map' key
    query("let $a := " + func.args(" fn() { 1 div 0 }", report) + "?error" +
        " let $b := try { 1 div 0 } catch * { $err:map }" +
        " return (deep-equal(sort(map:keys($a)), sort(map:keys($b))), " +
        "not(map:contains($a, 'map')))",
        "true\ntrue");
    // report overrides the 'results' and 'errors' options
    query(func.args(" (true#0, error#0)", " { 'report': true(), 'results': false() }") +
        " => count()", 2);
  }

  /** Test method. */
  @Test @Timeout(60) public void forkJoinTimeout() {
    final Function func = _XQUERY_FORK_JOIN;
    // long-running branches are cancelled once the timeout is exceeded
    error(func.args(" (1 to 4) ! fn() { prof:sleep(30000) }", " { 'timeout': 0.1 }"),
        XQUERY_TIMEOUT);
    // a timeout that is not reached returns the results
    query(func.args(" (fn() { 1 }, fn() { 2 })", " { 'timeout': 60 }") + " => count()", 2);
    // a non-positive timeout is treated as "no timeout"
    query(func.args(" (fn() { 1 }, fn() { 2 })", " { 'timeout': -1 }") + " => count()", 2);
  }

  /** Test method. */
  @Test @Timeout(value = 60, threadMode = SEPARATE_THREAD) public void gh2678() {
    final Function func = _XQUERY_FORK_JOIN;

    // GH-2678: concurrent access to module-level static variable caused XQDY0054
    // ("static variable depends on itself")
    query("declare variable $V := (1 to 1000000) ! math:sqrt(.); "
        + func.args(" (1 to 16) ! fn() { sum($V) }"));

    final IOFile x = new IOFile(sandbox(), "x.xqm");
    write(x, "module namespace x = 'x'; declare variable $x:V := (1 to 1000000) ! math:sqrt(.);");
    query("import module namespace x = 'x' at \"" + x.path() + "\"; "
        + func.args(" (1 to 16) ! fn() { sum($x:V) }"));

    query("declare %basex:lazy variable $V := (prof:sleep(200), random:double());\n"
        + "count(distinct-values(xquery:fork-join((1 to 16) ! fn() { $V })))", 1);

    final IOFile f = new IOFile(sandbox(), "f.txt");
    query("declare %basex:lazy variable $V :=\n"
        + "  (prof:sleep(200), file:append-text('" + f.path() + "', 'x'), random:double());\n"
        + "count(distinct-values(xquery:fork-join((1 to 16) ! fn() { $V }))),\n"
        + "file:size('" + f.path() + "')", "1\n1");

    // all waiting threads must see the same error value and location as the evaluating thread
    query("declare %basex:lazy variable $v := (prof:sleep(200), error(xs:QName('err:FOER0000'), 'd'"
        + ", 7));\nxquery:fork-join((1 to 8) ! fn() { $v }, { 'report': true() })?error?value[. = 7"
        + "] => count()", 8);
    query("declare %basex:lazy variable $V := (prof:sleep(200), error());\n"
        + "let $errors := xquery:fork-join((1 to 4) ! fn() { $V }, { 'report': true() })?error\n"
        + "return (count(distinct-values($errors?line-number)) eq 1, "
        + "$errors[1]?line-number gt 0)",
        "true\ntrue");

    // circular detection works across threads without deadlocks
    error("declare %basex:lazy variable $a := (prof:sleep(200), $b);\n"
        + "declare %basex:lazy variable $b := (prof:sleep(200), $a);\n"
        + func.args(" (fn() { $a }, fn() { $b })"), CIRCVAR_X);
    query("""
declare %basex:lazy variable $A := (prof:sleep(200), $B);
declare %basex:lazy variable $B := (prof:sleep(200), $C);
declare %basex:lazy variable $C := (prof:sleep(200), $D);
declare %basex:lazy variable $D := (prof:sleep(200), $E);
declare %basex:lazy variable $E := (prof:sleep(200), $F);
declare %basex:lazy variable $F := (prof:sleep(200), $A);
let $errors := xquery:fork-join((
  fn() { $A },
  fn() { $B },
  fn() { $C },
  fn() { $D },
  fn() { $E },
  fn() { $F }
), { 'report': true() })?error?code
return count($errors[local-name-from-QName(.) = 'XQDY0054'])
        """, 6);
    error("declare %basex:lazy variable $V := xquery:fork-join((1 to 2) ! fn() { $V }, "
        + "{ 'parallel': 2 }); $V", CIRCVAR_X);
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = _XQUERY_FOR_EACH;
    // apply the action to each item in parallel, results in input order
    query(func.args(" 1 to 5", " fn($n) { $n * $n }"), "1\n4\n9\n16\n25");
    query(func.args(" ()", " fn($n) { $n }"), "");
    query(func.args(" 1 to 100", " data#1") + " => sum()", 5050);
    // positional parameter
    query(func.args(" ('a', 'b', 'c')", " fn($v, $p) { $p || $v }"), "1a\n2b\n3c");
    // an action without parameters is run once per item
    query(func.args(" (1, 2, 3)", " fn() { 'x' }"), "x\nx\nx");
    // results of varying size are concatenated in input order
    query(func.args(" (1, 2, 3)", " fn($n) { 1 to $n }"), "1\n1\n2\n1\n2\n3");
    // options
    query(func.args(" 1 to 3", " fn($n) { $n }", " { 'results': false() }"), "");
    query("count(" + func.args(" 1 to 100", " fn($n) { (1, 2) }", " { 'parallel': 4 }") + ')', 200);
    query(func.args(" (1, 2)", " fn($n) { error() }", " { 'errors': false() }"), "");
    // report mode
    query(func.args(" (1, 2)", " fn($n) { $n }", " { 'report': true() }") + " ! ?value", "1\n2");
    query("exists(" + func.args(" 1", " fn($n) { error() }", " { 'report': true() }") + "?error)",
        true);
    // optimizations
    check(func.args(" ()", " fn($n) { $n }"), "", empty());
    // errors
    error(func.args(" 1", " fn($a, $b, $c) { $a }"), INVARITY_X_X);
    error(func.args(" 1", " 123"), INVTYPE_X);
    error(func.args(" (1, 2)", " fn($n) { error() }"), FUNERR1);
    error(func.args(" 1", " %updating fn($n) { delete node <a/> }"), FUNCUP_X);
  }

  /** Test method. */
  @Test @Timeout(60) public void forkAny() {
    final Function func = _XQUERY_FORK_ANY;
    // single function and empty input
    query(func.args(" true#0"), true);
    query(func.args(" ()"), "");
    // the first function to finish successfully wins
    query(func.args(" (fn() { prof:sleep(30000), 1 }, fn() { 2 })"), 2);
    // a failing branch is ignored if another one succeeds
    query(func.args(" (error#0, fn() { 42 })"), 42);
    // a successful empty sequence is a valid winner
    query(func.args(" (error#0, fn() { () })"), "");
    // all branches fail: an error is raised
    error(func.args(" (error#0, error#0)"), FUNERR1);
    // long-running branches are cancelled once the timeout is exceeded
    error(func.args(" (1 to 4) ! fn() { prof:sleep(30000) }", " { 'timeout': 0.1 }"),
        XQUERY_TIMEOUT);
    // a single function with a timeout is cancelled, too (no direct-invoke shortcut)
    error(func.args(" fn() { prof:sleep(30000) }", " { 'timeout': 0.1 }"), XQUERY_TIMEOUT);
    // errors
    error(func.args(" error#0"), FUNERR1);
    error(func.args(" count#1"), INVARITY_X_X);
    error(func.args(" 123"), INVTYPE_X);
    error(func.args(" (%updating fn() { delete node <a/> }, true#0)"), FUNCUP_X);
  }

  /** Test method. */
  @Test @Timeout(60) public void reduce() {
    final Function func = _XQUERY_REDUCE;
    // parallel sum
    query(func.args(" 1 to 100", " 0", " op('+')", " op('+')"), 5050);
    query(func.args(" 1 to 1000", " 0", " op('+')", " op('+')", " { 'parallel': 8 }"), 500500);
    // empty input returns the seed
    query(func.args(" ()", " 42", " op('+')", " op('+')"), 42);
    // single item: the action is applied once to the seed
    query(func.args(" 5", " 0", " op('+')", " op('+')"), 5);
    // a dedicated single-threaded pool yields the same result
    query(func.args(" 1 to 100", " 0", " op('+')", " op('+')", " { 'parallel': 1 }"), 5050);
    // associative string concatenation
    query(func.args(" 1 to 5", " ''", " fn($a, $b) { $a || $b }", " fn($a, $b) { $a || $b }"),
        12345);
    // accumulating a sequence, with the empty sequence as identity
    query(func.args(" 1 to 5", " ()", " fn($a, $b) { ($a, $b) }", " fn($a, $b) { ($a, $b) }"),
        "1\n2\n3\n4\n5");
    // count (per-chunk count, combined by addition)
    query(func.args(" 1 to 1000", " 0", " fn($a, $b) { $a + 1 }", " op('+')"), 1000);
    // errors
    error(func.args(" 1 to 10", " 0", " fn($a, $b) { error() }", " op('+')"), FUNERR1);
    error(func.args(" 1 to 4", " 0", " op('+')", " fn($a, $b, $c) { $a }"), INVARITY_X_X);
    error(func.args(" 1 to 4", " 0", " %updating fn($a, $b) { delete node <a/> }", " op('+')"),
        FUNCUP_X);
    // long-running aggregation is cancelled once the timeout is exceeded
    error(func.args(" 1 to 4", " 0", " fn($a, $b) { prof:sleep(30000) }", " op('+')",
        " { 'timeout': 0.1 }"), XQUERY_TIMEOUT);
  }

  /** Test method. */
  @Test public void parse() {
    final Function func = _XQUERY_PARSE;
    // queries
    query(func.args("1") + "/name()", "MainModule");
    query(func.args("1") + "/@updating/string()", false);
    query(func.args("1") + "/QueryPlan/@compiled/string()", false);

    query(func.args("1", " { 'compile': true() }") + "/QueryPlan/@compiled/string()", true);
    query(func.args("1", " { 'plan': false() }") + "/QueryPlan", "");

    final String lib = "module namespace x='x'; declare function x:x() { 1 + 2 };";
    query(func.args(lib) + "/name()", "LibraryModule");
    query(func.args(lib, " { 'compile': true() }") + "/name()", "LibraryModule");

    query(func.args("delete node <a/>") + "/name()", "MainModule");
    query(func.args("delete node <a/>") + "/@updating/string()", true);

    error(func.args("1+"), CALCEXPR);
    query("\n\ntry {" + func.args("1 +",
        " { 'pass': true() }") + "} catch * { $err:line-number }", 1);

    query("contains(try {" + func.args("1 +",
        " { 'base-uri': 'XXXX', 'pass': 'true' }") + "} catch * { $err:module }, 'XXXX')",
        true);

    // queries
    query(func.args(" xs:anyURI('src/test/resources/input.xq')") + "/name()", "MainModule");
    query(func.args(" xs:anyURI('src/test/resources/input.xq')") + "/@updating/string()", false);
    error(func.args(" xs:anyURI('src/test/resources/xxx.xq')"), RESWHICH_X);
  }
}
