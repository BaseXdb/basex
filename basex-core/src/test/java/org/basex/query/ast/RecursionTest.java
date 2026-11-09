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
    check(f + "count(local:f(1))", 1000000, empty(RECURSIVE), exists(Unfold.class));
    check(f + "local:f(999999)", "1999998\n2000000", exists(Unfold.class));
    check(f + "local:f(1000001)", "", exists(Unfold.class));

    // braced if, negated condition
    check("declare function local:f($n) { if($n <= 3) { $n, local:f($n + 1) } }; local:f(1)",
        "1\n2\n3", exists(Unfold.class));
    // base expression depends on the final state, several parameters
    check("declare function local:f($n, $m) {"
        + " if($n = 0) then $m else ($n, local:f($n - 1, $m + 1)) }; local:f(3, 10)",
        "3\n2\n1\n13", exists(Unfold.class));
    // parameter coercion
    check("declare function local:f($n as xs:double) {"
        + " if($n > 2) then () else ($n, local:f($n + 1)) }; local:f(1) ! xs:string(.)",
        "1\n2", exists(Unfold.class));
    // return type with arbitrary occurrence
    check("declare function local:f($n) as xs:integer* {"
        + " if($n > 3) then () else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3", exists(Unfold.class));
  }

  /** Generator: lazy evaluation. */
  @Test public void lazy() {
    inline(true);
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 1000000000000) then () else ($n, local:f($n + 1)) }; head(local:f(1))",
        1, empty(StaticFuncCall.class), exists(Unfold.class));
  }

  /** Generator: items after the recursive call. */
  @Test public void reverse() {
    check("declare function local:f($n) {"
        + " if($n > 3) then 'base' else (local:f($n + 1), $n) }; local:f(1)",
        "base\n3\n2\n1", exists(Unfold.class));
    check("declare function local:f($n) {"
        + " if($n > 3) then ('b', 'c') else (local:f($n + 1), $n, $n * 10) }; local:f(1)",
        "b\nc\n3\n30\n2\n20\n1\n10", exists(Unfold.class));
    check("declare function local:f($n) {"
        + " if($n > 1000000) then () else (local:f($n + 1), $n) }; local:f(1)[1]",
        1000000, exists(Unfold.class));
    // items before and after the recursive call
    check("declare function local:f($n) {"
        + " if($n > 3) then 'b' else ($n, local:f($n + 1), -$n) }; local:f(1)",
        "1\n2\n3\nb\n-3\n-2\n-1", exists(Unfold.class));
  }

  /** Other control flow around the recursive call. */
  @Test public void control() {
    // let clause
    check("declare function local:f($n) {"
        + " let $m := $n * 2 return if($n > 3) then () else ($m, local:f($n + 1)) }; local:f(1)",
        "2\n4\n6", exists(Unfold.class));
    // else-if chain
    check("declare function local:f($n) {"
        + " if($n > 5) then () else if($n mod 2 = 0) then ('e', local:f($n + 1))"
        + " else ($n, local:f($n + 1)) }; local:f(1)",
        "1\ne\n3\ne\n5", exists(Unfold.class));
    // branches with and without results after the recursive call
    check("declare function local:f($n) {"
        + " if($n > 3) then () else if($n mod 2 = 0) then (local:f($n + 1), 'x')"
        + " else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n3\nx", exists(Unfold.class));
    // switch
    check("declare function local:f($n) {"
        + " switch($n) case 4 return 'end' default return ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3\nend", exists(Unfold.class));
    // typeswitch
    check("declare function local:f($n) {"
        + " typeswitch($n) case xs:string return $n"
        + " default return ($n, local:f(if($n = 3) then 'x' else $n + 1)) }; local:f(1)",
        "1\n2\n3\nx", exists(Unfold.class));
    // pipeline
    check("declare function local:f($n as xs:integer) {"
        + " ($n * 10) -> (if(. > 30) then () else (., local:f($n + 1))) }; local:f(1)",
        "10\n20\n30", exists(Unfold.class));
    // multiple tuples: no rewrite
    check("declare function local:f($n) {"
        + " for $i in 1 to 2 return if($n > 2) then () else ($i, local:f($n + 1)) };"
        + "count(local:f(1))", 6, empty(Unfold.class));
  }

  /** Reduction: addition. */
  @Test public void sum() {
    final String f = "declare function local:f($n as xs:integer) as xs:integer {"
        + " if($n > 1000000) then 0 else $n + local:f($n + 1) };";
    check(f + "local:f(1)", 500000500000L, empty(RECURSIVE), exists(Unfold.class),
        exists(SUM));
    check("declare function local:f($n as xs:integer) as xs:decimal {"
        + " if($n > 3) then 0.5 else local:f($n + 1) + $n }; local:f(1)",
        6.5, exists(Unfold.class));
  }

  /** Reduction: string concatenation. */
  @Test public void concat() {
    check("declare function local:f($n as xs:integer) as xs:string {"
        + " if($n > 3) then '!' else $n || '-' || local:f($n + 1) }; local:f(1)",
        "1-2-3-!", exists(Unfold.class), exists(STRING_JOIN));
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 1000000) then '' else 'x' || local:f($n + 1) };"
        + "string-length(local:f(1))",
        1000000, exists(Unfold.class));
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 2) then '' else ($n, $n) || local:f($n + 1) }; local:f(1)",
        "1122", exists(Unfold.class));
    check("declare function local:f($n as xs:integer) {"
        + " if($n > 3) then '!' else local:f($n + 1) || $n }; local:f(1)",
        "!321", exists(Unfold.class));
  }

  /** Reduction: multiplication. */
  @Test public void product() {
    check("declare function local:f($n as xs:integer) as xs:integer {"
        + " if($n = 0) then 1 else $n * local:f($n - 1) }; local:f(20)",
        2432902008176640000L, exists(Unfold.class), exists(FOLD_RIGHT));
    check("declare function local:f($n as xs:integer) as xs:decimal {"
        + " if($n = 0) then 0.5 else local:f($n - 1) * $n }; local:f(4)",
        12, exists(Unfold.class), empty(Util.className(Unfold.class) + "[@suffix]"));
    // order of multiplications is preserved: no intermediate overflow
    check("declare function local:f($n as xs:integer) as xs:integer {"
        + " if($n = 0) then 0 else $n * local:f($n - 1) }; local:f(30)",
        0, exists(Unfold.class));
  }

  /** Functions that must not be rewritten. */
  @Test public void negative() {
    // tree recursion
    check("declare function local:f($n) {"
        + " if($n = 0) then 1 else (local:f($n - 1), local:f($n - 1)) }; count(local:f(3))",
        8, empty(Unfold.class));
    // return type with restricted occurrence
    check("declare function local:f($n) as xs:integer+ {"
        + " if($n > 3) then 0 else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3\n0", empty(Unfold.class));
    // no base case
    check("declare function local:f($n) {"
        + " (if($n > 3) then error() else $n, local:f($n + 1)) };"
        + "try { local:f(1) } catch * { 'error' }",
        "error", empty(Unfold.class));
    // memoization
    check("declare %basex:memo function local:f($n) {"
        + " if($n > 3) then () else ($n, local:f($n + 1)) }; local:f(1)",
        "1\n2\n3", empty(Unfold.class));
    // nondeterministic body, reverse order
    check("declare function local:f($n) {"
        + " if($n > 3) then () else (local:f($n + 1), random:integer(1) + $n) }; local:f(1)",
        "3\n2\n1", empty(Unfold.class));
    // constructed nodes, reverse order
    check("declare function local:f($n) {"
        + " if($n = 0) then () else (local:f($n - 1), <a n='{ $n }'/>) };"
        + "(local:f(3) | ()) ! string(@n)",
        "1\n2\n3", empty(Unfold.class));
    // floating-point reduction
    check("declare function local:f($n as xs:double) as xs:double {"
        + " if($n > 3) then 0 else $n + local:f($n + 1) }; local:f(1)",
        6, empty(Unfold.class));
    // concatenation with a non-string base result
    check("declare function local:f($n) {"
        + " if($n = 0) then 1 else 'a' || local:f($n - 1) }; local:f(0) instance of xs:integer",
        true, empty(Unfold.class));
    check("declare function local:f($n) {"
        + " if($n = 0) then () else 'a' || local:f($n - 1) }; empty(local:f(0))",
        true, empty(Unfold.class));
  }

  /** Nondeterministic bodies: evaluation order is preserved. */
  @Test public void nondeterministic() {
    check("declare function local:f($n, $g) {"
        + " if($n = 0) then () else ($g?number, local:f($n - 1, $g?next())) };"
        + "count(distinct-values(local:f(100000, random-number-generator(1))))",
        100000, exists(Unfold.class));
    check("declare function local:f($n, $g) {"
        + " if($n = 0) then () else ($g?number, local:f($n - 1, $g?next())) };"
        + "declare function local:acc($n, $g, $r) {"
        + " if($n = 0) then $r else local:acc($n - 1, $g?next(), ($r, $g?number)) };"
        + "deep-equal(local:f(5, random-number-generator(1)),"
        + " local:acc(5, random-number-generator(1), ()))",
        true, exists(Unfold.class));
  }

  /** Errors. */
  @Test public void errors() {
    error("declare function local:f($n) {"
        + " if($n > 3) then () else (if($n = 3) then error() else $n, local:f($n + 1)) };"
        + "local:f(1)", FUNERR1);
    error("declare function local:f($n) as xs:integer* {"
        + " if($n > 3) then () else (if($n = 3) then 'x' else $n, local:f($n + 1)) };"
        + "local:f(1)", INVTYPE_X);
  }
}
