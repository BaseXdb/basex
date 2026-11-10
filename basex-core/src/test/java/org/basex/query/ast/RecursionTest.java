package org.basex.query.ast;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for rewriting non-tail-recursive functions to iterative expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RecursionTest extends SandboxTest {
  /** Recursive calls in function declarations. */
  private static final String RECURSIVE =
      Util.className(StaticFunc.class) + "//" + Util.className(StaticFuncCall.class);

  /** Generator: items before the recursive call. */
  @Test public void generator() {
    final String f = "declare function local:f($n) {"
        + " if($n > 1000000) then () else ($n * 2, local:f($n + 1)) };";
    check(f + "count(local:f(1))", 1000000, empty(RECURSIVE));
    check(f + "local:f(999999)", "1999998\n2000000", empty(RECURSIVE));
    check(f + "local:f(1000001)", "", empty(RECURSIVE));

    // braced if, negated condition
    check("declare function local:f($n) { if($n <= 3) { $n, local:f($n + 1) } }; local:f(1)",
        "1\n2\n3", empty(RECURSIVE));
    // base expression depends on the final state, several parameters
    check("declare function local:f($n, $m) {"
        + " if($n = 0) then $m else ($n, local:f($n - 1, $m + 1)) }; local:f(3, 10)",
        "3\n2\n1\n13", empty(RECURSIVE));
    // parameter coercion
    check("declare function local:f($n as xs:double) {"
        + " if($n > 2) then () else ($n, local:f($n + 1)) }; local:f(1) ! xs:string(.)",
        "1\n2", empty(RECURSIVE));
    // return type with arbitrary occurrence
    check("declare function local:f($n) as xs:integer* {"
        + " if($n > 3) then () else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3", empty(RECURSIVE));
  }

  /** Generator: lazy evaluation. */
  @Test public void lazy() {
    inline(true);
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 1000000000000) then () else ($n, local:f($n + 1)) }; head(local:f(1))",
        1, empty(StaticFuncCall.class), empty(RECURSIVE));
    check("declare function local:f($s as xs:string) {"
        + " if(string-length($s) > 1000000000) then () else ($s, local:f($s || 'x')) };"
        + "head(local:f('a'))", "a", empty(StaticFuncCall.class), exists(Unfold.class));
  }

  /** Loops without counter: iterative evaluation. */
  @Test public void unfold() {
    check("declare function local:f($n) {"
        + " if($n > 100) then () else ($n, local:f($n * 2)) }; local:f(1)",
        "1\n2\n4\n8\n16\n32\n64", exists(Unfold.class));
    check("declare function local:f($s) {"
        + " if(empty($s)) then 'base' else (local:f(tail(tail($s))), head($s)) }; local:f(1 to 5)",
        "base\n5\n3\n1", exists(Unfold.class));
    // tail calls only
    check("declare function local:f($n, $m) {"
        + " if($n > 100) then $m else local:f($n * 2, $m + 1) }; local:f(1, 0)",
        7, exists(Unfold.class));
  }

  /** Counter loops: rewritten to ranges. */
  @Test public void counter() {
    final String up = "declare function local:f($n) {"
        + " if($n <= 5) then ($n * 2, local:f($n + 1)) };";
    check(up + "local:f(1)", "2\n4\n6\n8\n10", empty(RECURSIVE), empty(Unfold.class));
    check(up + "local:f(6)", "", empty(Unfold.class));
    check(up + "local:f(" + wrap(1) + " => xs:integer())", "2\n4\n6\n8\n10",
        empty(Unfold.class), exists(Range.class));
    check(up + "local:f(1.5)", "3\n5\n7\n9", exists(Unfold.class));

    // recursive call nested in a list operand
    check("declare function local:f($n) { $n * 2, if($n < 5) { local:f($n + 1) } }; local:f(1)",
        "2\n4\n6\n8\n10", empty(RECURSIVE), empty(Unfold.class));
    // base expression equal to the step results
    check("declare function local:f($n) { if($n < 5) then ($n, local:f($n + 1)) else $n };"
        + "local:f(" + wrap(1) + " => xs:integer())", "1\n2\n3\n4\n5",
        exists(Range.class), empty(Unfold.class));
    check("declare function local:f($n) { if($n < 5) then ($n, local:f($n + 1)) else $n };"
        + "local:f(7)", 7, empty(Unfold.class));
    // counting down, base case depending on the counter
    check("declare function local:f($n) { if($n = 0) then 'x' else ($n, local:f($n - 1)) };"
        + "local:f(3)", "3\n2\n1\nx", empty(Unfold.class));
    check("declare function local:f($n) { if($n >= 1) then ($n, local:f($n - 1)) else -$n };"
        + "local:f(3), local:f(-2)", "3\n2\n1\n0\n2", empty(Unfold.class));
    // bound on the left, results after the recursive call
    check("declare function local:f($n) { if(3 < $n) then 'base' else (local:f($n + 1), $n) };"
        + "local:f(1)", "base\n3\n2\n1", empty(Unfold.class));
    // unchanged parameters
    check("declare function local:f($n, $max) {"
        + " if($n > $max) then () else ($n * 2, local:f($n + 1, $max)) }; local:f(1, 5)",
        "2\n4\n6\n8\n10", empty(Unfold.class));
    check("declare function local:f($n, $k) {"
        + " if($n > 3) then $k else ($n * $k, local:f($n + 1, $k)) }; local:f(1, 10)",
        "10\n20\n30\n10", empty(Unfold.class));
    // no loop
    check("declare function local:f($i) { if($i eq 12345) then $i else local:f($i + 1) };"
        + "local:f(0)", 12345, empty(Unfold.class));

    // not rewritten: nondeterministic or non-integer bound, other steps
    check("declare function local:f($n) { if($n > " + _RANDOM_INTEGER.args(1) + " + 3) then ()"
        + " else ($n, local:f($n + 1)) }; local:f(1)", "1\n2\n3", exists(Unfold.class));
    check("declare function local:f($n) { if($n > 3.5) then () else ($n, local:f($n + 1)) };"
        + "local:f(1)", "1\n2\n3", exists(Unfold.class));
    check("declare function local:f($n) { if($n > 5) then () else ($n, local:f($n + 2)) };"
        + "local:f(1)", "1\n3\n5", exists(Unfold.class));
  }

  /** Loops over the items of a sequence: rewritten to iterations. */
  @Test public void walk() {
    final String seq = " (1 to 6)[. > " + wrap(2) + "]";
    check("declare function local:f($s) {"
        + " if(empty($s)) then () else (head($s) * 2, local:f(tail($s))) }; local:f(" + seq + ")",
        "6\n8\n10\n12", empty(RECURSIVE), empty(Unfold.class), empty(TAIL));
    check("declare function local:f($s, $acc) { if(empty($s)) then $acc"
        + " else local:f(tail($s), ($acc, head($s) * 2)) }; local:f(" + seq + ", ())",
        "6\n8\n10\n12", empty(Unfold.class), empty(TAIL));
    // results after the recursive call
    check("declare function local:f($s) {"
        + " if(exists($s)) then (local:f(tail($s)), head($s)) else 'end' }; local:f(" + seq + ")",
        "end\n6\n5\n4\n3", empty(Unfold.class), empty(SUBSEQUENCE));
    // let clauses, unchanged parameters, other references to the sequence
    check("declare function local:f($s) { if(empty($s)) then ()"
        + " else let $h := head($s) return ($h, $h * 10, local:f(tail($s))) };"
        + " local:f(" + seq + ")",
        "3\n30\n4\n40\n5\n50\n6\n60", empty(Unfold.class));
    check("declare function local:f($s, $k) { if(empty($s)) then ()"
        + " else (head($s) * $k, count($s), local:f(tail($s), $k)) }; local:f(" + seq + ", 10)",
        "30\n4\n40\n3\n50\n2\n60\n1", empty(Unfold.class));
    check("declare function local:f($s) { if(empty($s)) then 0"
        + " else (sum($s), local:f(tail($s))) }; local:f(" + seq + ")",
        "18\n15\n11\n6\n0", empty(Unfold.class));
    check("declare function local:f($s) { if(empty($s)) then ()"
        + " else (head($s), local:f(tail($s))) }; count(local:f(1 to 1000000))", 1000000);
  }

  /** Let clauses and pipelines before the condition: moved into the branches. */
  @Test public void lets() {
    final String n = wrap(1) + " => xs:integer()";
    check("declare function local:f($n) { let $x := $n * 2"
        + " return if($n <= 5) then ($x, $x + 1, local:f($n + 1)) else () }; local:f(" + n + ")",
        "2\n3\n4\n5\n6\n7\n8\n9\n10\n11", empty(RECURSIVE), empty(Unfold.class));
    // base expression uses the let
    check("declare function local:f($n) { let $x := $n * 10"
        + " return if($n < 3) then ($x, $x + 1, local:f($n + 1)) else ($x, $x) };"
        + "local:f(" + n + ")", "10\n11\n20\n21\n30\n30", empty(RECURSIVE), empty(Unfold.class));
    // walk
    check("declare function local:f($s) { let $h := head($s)"
        + " return if(empty($s)) then () else ($h * 2, $h * 3, local:f(tail($s))) };"
        + "local:f((" + n + ", 2, 3))", "2\n3\n4\n6\n6\n9", empty(RECURSIVE),
        empty(Unfold.class), empty(TAIL));
    // pipeline, nested let and pipeline
    check("declare function local:f($n) {"
        + " ($n * 2) -> (if($n <= 3) then (., . + 1, local:f($n + 1)) else ()) };"
        + "local:f(" + n + ")", "2\n3\n4\n5\n6\n7", empty(RECURSIVE), empty(Unfold.class));
    check("declare function local:f($n) { let $x := $n * 2 return ($x + 1)"
        + " -> (if($n <= 3) then ($x, ., ., local:f($n + 1)) else $x) }; local:f(" + n + ")",
        "2\n3\n3\n4\n5\n5\n6\n7\n7\n8", empty(RECURSIVE), empty(Unfold.class));
    // results after the recursive call
    check("declare function local:f($n) { let $x := $n * 2"
        + " return if($n <= 3) then ($x, $x + 1, local:f($n + 1), $n) else () };"
        + "local:f(" + n + ")", "2\n3\n4\n5\n6\n7\n3\n2\n1", empty(RECURSIVE),
        empty(Unfold.class));

    // not rewritten: condition depends on the let
    check("declare function local:f($n) { let $x := $n * 2"
        + " return if($x <= 6) then ($x, $x + 1, local:f($n + 1)) else () }; local:f(" + n + ")",
        "2\n3\n4\n5\n6\n7", exists(RECURSIVE));
  }

  /** Tail recursion with accumulating parameters. */
  @Test public void accumulator() {
    check("declare function local:f($n, $acc) {"
        + " if($n > 5) then $acc else local:f($n + 1, ($acc, $n * 2)) }; local:f(1, ())",
        "2\n4\n6\n8\n10", empty(RECURSIVE), empty(Unfold.class));
    check("declare function local:f($n, $acc) {"
        + " if($n > 3) then ($acc, 'b') else local:f($n + 1, ($acc, $n)) }; local:f(1, 'a')",
        "a\n1\n2\n3\nb", empty(Unfold.class));
    // prepended values
    check("declare function local:f($n, $acc) {"
        + " if($n > 3) then $acc else local:f($n + 1, ($n, $acc)) }; local:f(1, 'a')",
        "3\n2\n1\na", empty(Unfold.class));
    // prepended nodes: constructed in the original order
    check("declare function local:f($n, $acc) {"
        + " if($n > 3) then $acc else local:f($n + 1, (<a>{ $n }</a>, $acc)) };"
        + "(local:f(1, ()) | ()) ! string()", "1\n2\n3", exists(Unfold.class));
    // sums, strings, maps
    check("declare function local:f($n as xs:integer, $s as xs:integer) {"
        + " if($n > 100) then $s else local:f($n + 1, $s + $n) }; local:f(1, 0)",
        5050, empty(RECURSIVE), empty(Unfold.class));
    check("declare function local:f($n as xs:integer, $s as xs:string) {"
        + " if($n > 5) then $s else local:f($n + 1, $s || $n) }; local:f(1, '-')",
        "-12345", empty(Unfold.class));
    check("declare function local:f($n as xs:integer, $m as map(*)) {"
        + " if($n > 3) then $m else local:f($n + 1, map:put($m, $n, $n * $n)) };"
        + "local:f(1, {})?*", "1\n4\n9", empty(Unfold.class));
    check("declare function local:f($s, $m as map(*)) { if(empty($s)) then $m"
        + " else local:f(tail($s), map:remove($m, head($s))) };"
        + "local:f((1 to 3)[. > " + wrap(1) + "], map:build(1 to 5)) => map:keys()",
        "1\n4\n5", empty(RECURSIVE), exists(_MAP_REMOVE));
    // conditional accumulation
    check("declare function local:f($n, $acc) { if($n > 10) then $acc"
        + " else local:f($n + 1, if($n mod 3 = 0) then ($acc, $n) else $acc) }; local:f(1, ())",
        "3\n6\n9", empty(RECURSIVE));
    // sibling walk
    check("declare function local:f($e, $acc) { if(empty($e)) then $acc"
        + " else local:f($e/following-sibling::*[1], ($acc, name($e))) };"
        + "local:f(<x><a/><b/><c/></x>/*[1], ())", "a\nb\nc", exists(Unfold.class));

    // accumulator is referenced elsewhere: plain iteration
    check("declare function local:f($n, $acc) {"
        + " if(count($acc) >= 3) then $acc else local:f($n + 1, ($acc, $n)) }; local:f(1, ())",
        "1\n2\n3", empty(RECURSIVE), exists(Unfold.class));
    // untyped numeric accumulator: plain iteration
    check("declare function local:f($n, $s) {"
        + " if($n > 100) then $s else local:f($n + 1, $s + $n) }; local:f(1, 0.5)",
        5050.5, empty(RECURSIVE), exists(Unfold.class));
  }

  /** Generator: items after the recursive call. */
  @Test public void reverse() {
    check("declare function local:f($n) {"
        + " if($n > 3) then 'base' else (local:f($n + 1), $n) }; local:f(1)",
        "base\n3\n2\n1", empty(RECURSIVE));
    check("declare function local:f($n) {"
        + " if($n > 3) then ('b', 'c') else (local:f($n + 1), $n, $n * 10) }; local:f(1)",
        "b\nc\n3\n30\n2\n20\n1\n10", empty(RECURSIVE));
    check("declare function local:f($n) {"
        + " if($n > 1000000) then () else (local:f($n + 1), $n) }; local:f(1)[1]",
        1000000, empty(RECURSIVE));
    // items before and after the recursive call
    check("declare function local:f($n) {"
        + " if($n > 3) then 'b' else ($n, local:f($n + 1), -$n) }; local:f(1)",
        "1\n2\n3\nb\n-3\n-2\n-1", empty(RECURSIVE));
  }

  /** Other control flow around the recursive call. */
  @Test public void control() {
    // let clause
    check("declare function local:f($n) {"
        + " let $m := $n * 2 return if($n > 3) then () else ($m, local:f($n + 1)) }; local:f(1)",
        "2\n4\n6", empty(RECURSIVE));
    // else-if chain
    check("declare function local:f($n) {"
        + " if($n > 5) then () else if($n mod 2 = 0) then ('e', local:f($n + 1))"
        + " else ($n, local:f($n + 1)) }; local:f(1)",
        "1\ne\n3\ne\n5", empty(RECURSIVE));
    // branches with and without results after the recursive call
    check("declare function local:f($n) {"
        + " if($n > 3) then () else if($n mod 2 = 0) then (local:f($n + 1), 'x')"
        + " else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n3\nx", empty(RECURSIVE));
    // switch
    check("declare function local:f($n) {"
        + " switch($n) case 4 return 'end' default return ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3\nend", empty(RECURSIVE));
    // typeswitch
    check("declare function local:f($n) {"
        + " typeswitch($n) case xs:string return $n"
        + " default return ($n, local:f(if($n = 3) then 'x' else $n + 1)) }; local:f(1)",
        "1\n2\n3\nx", empty(RECURSIVE));
    // switch in a list operand
    check("declare function local:f($n) {"
        + " $n, switch($n) case 4 return 'end' default return local:f($n + 1) }; local:f(1)",
        "1\n2\n3\n4\nend", empty(RECURSIVE));
    // switch: recursive call in a case expression
    check("declare function local:f($n) {"
        + " $n, switch(3) case (if($n > 2) then 3 else local:f($n + 1)[1]) return ()"
        + " default return 'end' };"
        + "local:f(1)", "1\nend", exists(RECURSIVE));
    // pipeline
    check("declare function local:f($n as xs:integer) {"
        + " ($n * 10) -> (if(. > 30) then () else (., local:f($n + 1))) }; local:f(1)",
        "10\n20\n30", empty(RECURSIVE));
    // multiple tuples: no rewrite
    check("declare function local:f($n) {"
        + " for $i in 1 to 2 return if($n > 2) then () else ($i, local:f($n + 1)) };"
        + "count(local:f(1))", 6, exists(RECURSIVE));
  }

  /** Reduction: addition. */
  @Test public void sum() {
    final String f = "declare function local:f($n as xs:integer) as xs:integer {"
        + " if($n > 1000000) then 0 else $n + local:f($n + 1) };";
    check(f + "local:f(1)", 500000500000L, empty(RECURSIVE),
        exists(SUM));
    check("declare function local:f($n as xs:integer) as xs:decimal {"
        + " if($n > 3) then 0.5 else local:f($n + 1) + $n }; local:f(1)",
        6.5, empty(RECURSIVE));
  }

  /** Reduction: string concatenation. */
  @Test public void concat() {
    check("declare function local:f($n as xs:integer) as xs:string {"
        + " if($n > 3) then '!' else $n || '-' || local:f($n + 1) }; local:f(1)",
        "1-2-3-!", empty(RECURSIVE), exists(STRING_JOIN));
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 1000000) then '' else 'x' || local:f($n + 1) };"
        + "string-length(local:f(1))",
        1000000, empty(RECURSIVE));
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 2) then '' else ($n, $n) || local:f($n + 1) }; local:f(1)",
        "1122", empty(RECURSIVE));
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 3) then '!' else local:f($n + 1) || $n }; local:f(1)",
        "!321", empty(RECURSIVE));
  }

  /** Reduction: multiplication. */
  @Test public void product() {
    check("declare function local:f($n as xs:integer) as xs:integer {"
        + " if($n = 0) then 1 else $n * local:f($n - 1) }; local:f(20)",
        2432902008176640000L, empty(RECURSIVE), exists(FOLD_RIGHT));
    check("declare function local:f($n as xs:integer) as xs:decimal {"
        + " if($n = 0) then 0.5 else local:f($n - 1) * $n }; local:f(4)",
        12, empty(RECURSIVE));
    // order of multiplications is preserved: no intermediate overflow
    check("declare function local:f($n as xs:integer) as xs:integer {"
        + " if($n = 0) then 0 else $n * local:f($n - 1) }; local:f(30)",
        0, empty(RECURSIVE));
  }

  /** Functions that must not be rewritten. */
  @Test public void negative() {
    // tree recursion
    check("declare function local:f($n) {"
        + " if($n = 0) then 1 else (local:f($n - 1), local:f($n - 1)) }; count(local:f(3))",
        8, exists(RECURSIVE));
    // no base case
    check("declare function local:f($n) {"
        + " (if($n > 3) then error() else $n, local:f($n + 1)) };"
        + "try { local:f(1) } catch * { 'error' }",
        "error", exists(RECURSIVE));
    // memoization
    check("declare %basex:memo function local:f($n) {"
        + " if($n > 3) then () else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3", exists(RECURSIVE));
    // nondeterministic body, reverse order
    check("declare function local:f($n) {"
        + " if($n > 3) then () else (local:f($n + 1), random:integer(1) + $n) }; local:f(1)",
        "3\n2\n1", exists(RECURSIVE));
    // constructed nodes, reverse order
    check("declare function local:f($n) {"
        + " if($n = 0) then () else (local:f($n - 1), <a n='{ $n }'/>) };"
        + "(local:f(3) | ()) ! string(@n)",
        "1\n2\n3", exists(RECURSIVE));
    // floating-point reduction
    check("declare function local:f($n as xs:double) as xs:double {"
        + " if($n > 3) then 0 else $n + local:f($n + 1) }; local:f(1)",
        6, exists(RECURSIVE));
    // concatenation with a non-string base result
    check("declare function local:f($n) {"
        + " if($n = 0) then 1 else 'a' || local:f($n - 1) }; local:f(0) instance of xs:integer",
        true, exists(RECURSIVE));
    check("declare function local:f($n) {"
        + " if($n = 0) then () else 'a' || local:f($n - 1) }; empty(local:f(0))",
        true, exists(RECURSIVE));
  }

  /** Nondeterministic bodies: evaluation order is preserved. */
  @Test public void nondeterministic() {
    check("declare function local:f($n, $g) {"
        + " if($n = 0) then () else ($g?number, local:f($n - 1, $g?next())) };"
        + "count(distinct-values(local:f(100000, random-number-generator(1))))",
        100000, empty(RECURSIVE));
    check("declare function local:f($n, $g) {"
        + " if($n = 0) then () else ($g?number, local:f($n - 1, $g?next())) };"
        + "declare function local:acc($n, $g, $r) {"
        + " if($n = 0) then $r else local:acc($n - 1, $g?next(), ($r, $g?number)) };"
        + "deep-equal(local:f(5, random-number-generator(1)),"
        + " local:acc(5, random-number-generator(1), ()))",
        true, empty(RECURSIVE));
  }

  /** Errors. */
  @Test public void errors() {
    error("declare function local:f($n) {"
        + " if($n > 3) then () else (if($n = 3) then error() else $n, local:f($n + 1)) };"
        + "local:f(1)", FUNERR1);
    error("declare function local:f($n) as xs:integer* {"
        + " if($n > 3) then () else (if($n = 3) then 'x' else $n, local:f($n + 1)) };"
        + "local:f(1)", INVTYPE_X);
    // return types with restricted occurrence: the innermost result is checked
    check("declare function local:f($n) as xs:integer+ {"
        + " if($n > 3) then 0 else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3\n0", empty(RECURSIVE));
    error("declare function local:f($n) as xs:integer+ {"
        + " if($n > 3) then 1[. = " + wrap(0) + "] else ($n, local:f($n + 1)) }; local:f(1)",
        INVTYPE_X);
    // base expression with an incompatible occurrence: rewritten, error is raised at runtime
    final String inc = "declare function local:f($n) as xs:integer+ {"
        + " if($n > 3) then () else ($n, local:f($n + 1)) };";
    error(inc + "local:f(1)", INVTYPE_X);
    check(inc + "try { local:f(1) } catch * { local-name-from-QName($err:code) }",
        "XPTY0004", empty(RECURSIVE));
    error("declare function local:f($n) as xs:integer? {"
        + " if($n > 3) then () else ($n, local:f($n + 1)) }; local:f(2)", INVTYPE_X);

    // errors in unevaluated branches
    check("declare function local:f($n) as xs:integer+ { if($n < 0) then 'x'"
        + " else if($n = 0) then 1 else ($n, local:f($n - 1)) }; local:f(3)",
        "3\n2\n1\n1", empty(RECURSIVE));
    error("declare function local:f($n) as xs:integer+ { if($n < 0) then 'x'"
        + " else if($n = 0) then 1 else ($n, local:f($n - 1)) }; local:f(-1)", INVTYPE_X);
    check("declare function local:f($n) {"
        + " if($n = 0) then 0 else 1 + (if($n < 0) then 'x' else local:f($n - 1)) }; local:f(3)",
        3, empty(RECURSIVE));
    error("declare function local:f($n) {"
        + " if($n = 0) then 0 else 1 + (if($n < 0) then 'x' else local:f($n - 1)) }; local:f(-1)",
        CALCTYPE_X_X_X_X_X);
  }
}
