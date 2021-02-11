package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.QueryError.*;

import org.basex.query.ast.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Array Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArrayModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void append() {
    final Function func = _ARRAY_APPEND;
    query(func.args(" []", " ()"), "[()]");
    query(func.args(" [()]", " ()"), "[(), ()]");
    query(func.args(" []", 1), "[1]");
    query(func.args(" [1]", 2), "[1, 2]");
    query(func.args(" [1,2,3]", " (4,5)"), "[1, 2, 3, (4, 5)]");
  }

  /** Test method. */
  @Test public void filter() {
    final Function func = _ARRAY_FILTER;
    query("([1],1)[. instance of array(*)] ! " + func.args(" .",
        " function($a) { true() }"), "[1]");

    query(func.args(" [1]", " function($a) { true() }"), "[1]");
    query(func.args(" [1]", " function($a) { false() }"), "[]");
    query(func.args(" [1,-2]", " function($a) { $a>0 }"), "[1]");
    query(func.args(" [0,1]", " boolean#1"), "[1]");
  }

  /** Test method. */
  @Test public void flatten() {
    final Function func = _ARRAY_FLATTEN;
    query(func.args(" [1,2]"), "1\n2");
    query(func.args(" ([1],[2])"), "1\n2");
    query(func.args(" for $c in (1,2) return [$c]"), "1\n2");
    query(func.args(" [for $c in (1,2) return [$c]]"), "1\n2");
    query("head(" + func.args(" 1 to 1000000000000") + ')', "1");
    query(func.args(" array { 1 to 100000 }") + "[last()]", "100000");

    // check that the input sequence is evaluated lazily
    query(func.args(" ([1, [[2]]], [3], error())") + "[3]", "3");
    error("declare %basex:inline(0) function local:value($val) { $val };"
        + "local:value(array:flatten(([1, [[2]]], [3], error())))[3]", FUNERR1);

    // some more complex tests for the iterative variant using a complex, deeply nested array
    final String nested = "let $nested := "
        + "  fold-left(1 to $n, [0], function($seq, $i) { "
            + _ARRAY_APPEND.args(" $seq", " [$seq, $i]") + " }) ";
    // each distinct value `x` should occur `2^(10 - x)` times
    query("let $n := 10 "
        + nested
        + "for $v at $p in " + func.args(" $nested") + " "
        + "group by $v "
        + "order by $v descending "
        + "return count($p)",
        "1\n2\n4\n8\n16\n32\n64\n128\n256\n512\n1024");
    // the first occurrence of `x` should be at position `2^(x + 1) - 1`
    query("let $n := 10 "
        + nested
        + "return fold-left(" + func.args(" $nested") + ", [(), 0], function($acc, $v) {"
        + "  let $seq := $acc(1),"
        + "      $idx := $acc(2) + 1"
        + "  return ("
        + "    if(count($seq) gt $v) then [$seq, $idx]"
        + "    else [($seq, $idx), $idx]"
        + "  )"
        + "})(1)",
        "1\n3\n7\n15\n31\n63\n127\n255\n511\n1023\n2047");
    // the flattened result should be the same as that of a reference implementation
    query("declare function local:flatten($seq) {"
        + "  for $it in $seq"
        + "  return typeswitch($it)"
        + "    case array(*) return local:flatten($it?*)"
        + "    default return $it"
        + "};"
        + "let $n := 13 "
        + nested
        + "return deep-equal("
        + "  for $it in " + func.args(" $nested")
        + "  where random:double() le 1"
        + "  return $it,"
        + "  local:flatten($nested)"
        + ")",
        "true");
  }

  /** Test method. */
  @Test public void foldLeft() {
    final Function func = _ARRAY_FOLD_LEFT;
    query(func.args(" [1,2]", 0, " function($a,$b) { $a+$b }"), 3);
  }

  /** Test method. */
  @Test public void foldRight() {
    final Function func = _ARRAY_FOLD_RIGHT;
    query(func.args(" [1,2]", " ()", " function($a,$b) { $b,$a }"), "2\n1");
  }

  /** Test method. */
  @Test public void forEach() {
    final Function func = _ARRAY_FOR_EACH;
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" .", " function($a) { $a * $a }"), "[4]");
    query("(1, not#1)[. instance of function(*)] ! " +
        func.args(" [1]", " ."), "[false()]");

    query(func.args(" []", " function($a) { $a }"), "[]");
    query(func.args(" [1]", " function($a) { $a }"), "[1]");
    query(func.args(" [1,2]", " function($a) { $a+1 }"), "[2, 3]");
    query(func.args(" [1,2,3]", " function($a) { () }"), "[(), (), ()]");
  }

  /** Test method. */
  @Test public void forEachPair() {
    final Function func = _ARRAY_FOR_EACH_PAIR;
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" .", " .", " function($a, $b) { $a * $b }"), "[4]");
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" [1]", " .", " function($a, $b) { $a * $b }"), "[2]");
    query("([2], 2)[. instance of array(*)] ! " +
        func.args(" .", " [1]", " function($a, $b) { $a * $b }"), "[2]");
    query("(1, deep-equal#2)[. instance of function(*)] ! " +
        func.args(" [1]", " [2]", " ."), "[false()]");

    query(func.args(" []", " []", " function($a,$b) { $a+$b }"), "[]");
    query(func.args(" [1,2]", " []", " function($a,$b) { $a+$b }"), "[]");
    query(func.args(" [1]", " [2]", " function($a,$b) { $a+$b }"), "[3]");
    query(func.args(" [1,2,3]", " [2]", " function($a,$b) { $a+$b }"), "[3]");
  }

  /** Test method. */
  @Test public void head() {
    final Function func = _ARRAY_HEAD;
    query(func.args(" [1]"), 1);
    query(func.args(" array { 1 to 5 }"), 1);
    query(func.args(" [1 to 2, 3]"), "1\n2");

    error(func.args(" []"), ARRAYEMPTY);
  }

  /** Test method. */
  @Test public void insertBefore() {
    final Function func = _ARRAY_INSERT_BEFORE;
    query(func.args(" []", 1, 1), "[1]");
    query(func.args(" [1]", 1, 2), "[2, 1]");
    query(func.args(" [1]", 2, 2), "[1, 2]");

    error(func.args(" []", 0, 1), ARRAYBOUNDS_X_X);
    error(func.args(" []", 2, 1), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void join() {
    final Function func = _ARRAY_JOIN;
    query(func.args(" []"), "[]");
    query(func.args(" [1]"), "[1]");
    query(func.args(" ([1], [2])"), "[1, 2]");
    query(func.args(" ([1], [2], [3])"), "[1, 2, 3]");
    query(func.args(" ([1], [()], [2 to 3])"), "[1, (), (2, 3)]");

    check(func.args(" [ <a/> ]") + "?1", "<a/>", empty(func));
    check(func.args(" ([ <a/> ], [])") + "?1", "<a/>", empty(func));
    check(func.args(" ([ <a/> ], [])") + "?*", "<a/>", empty(func));

    // GH-1954
    query(func.args(" if (<a/>/text()) then array { } else ()") + " ! array:size(.)", 0);
  }

  /** Test method. */
  @Test public void put() {
    final Function func = _ARRAY_PUT;
    query(func.args(" [1]", 1, " ()"), "[()]");
    error(func.args(" []", 1, " ()"), ARRAYEMPTY);
    error(func.args(" [1]", 2, " ()"), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void remove() {
    final Function func = _ARRAY_REMOVE;
    query(func.args(" [1]", 1), "[]");
    query(func.args(" [1, 2]", 1), "[2]");
    query(func.args(" [1, 2]", 2), "[1]");
    query(func.args(" array { 1 to 5 }", 1), "[2, 3, 4, 5]");
    query(func.args(" array { 1 to 5 }", 3), "[1, 2, 4, 5]");
    query(func.args(" array { 1 to 5 }", 5), "[1, 2, 3, 4]");

    error(func.args(" []", 0), ARRAYEMPTY);
    error(func.args(" [1]", 0), ARRAYBOUNDS_X_X);
    error(func.args(" [1]", 2), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void reverse() {
    final Function func = _ARRAY_REVERSE;
    query(func.args(" []"), "[]");
    query(func.args(" [1]"), "[1]");
    query(func.args(" [1, 2]"), "[2, 1]");
    query(func.args(" [1 to 2]"), "[(1, 2)]");
    query(func.args(" array { 1 to 3 }"), "[3, 2, 1]");
  }

  /** Test method. */
  @Test public void size() {
    final Function func = _ARRAY_SIZE;
    query(func.args(" []"), 0);
    query(func.args(" [ 1 ]"), 1);
    query(func.args(" [ 1, 2 ]"), 2);
    query(func.args(" [ 1 to 3 ]"), 1);
    query(func.args(" array {}"), 0);
    query(func.args(" array { 1 }"), 1);
    query(func.args(" array { 1, 2 }"), 2);
    query(func.args(" array { 1 to 3 }"), 3);
  }

  /** Test method. */
  @Test public void sort() {
    final Function func = _ARRAY_SORT;
    query("([2,1], 1)[. instance of array(*)] ! " + func.args(" .", " ()", " hof:id#1"), "[1, 2]");

    query(func.args(" [1,4,6,5,3]"), "[1, 3, 4, 5, 6]");
    query(func.args(" [(1,0), (1,1), (0,1), (0,0)]"), "[(0, 0), (0, 1), (1, 0), (1, 1)]");
    query(func.args(" [3,2,1]", "http://www.w3.org/2005/xpath-functions/collation/codepoint"),
        "[1, 2, 3]");
    query(func.args(" [1,-2,5,10,-10,10,8]", " ()", " abs#1"), "[1, -2, 5, 8, 10, -10, 10]");

    check(func.args(" [1,2]", " ()", " function($a) { -$a }"), "[2, 1]",
        type(func, "array(xs:integer)"));
  }

  /** Test method. */
  @Test public void subquery() {
    final Function func = _ARRAY_SUBARRAY;
    query(func.args(" []", 1), "[]");
    query(func.args(" []", 1, 0), "[]");
    query(func.args(" [1]", 1), "[1]");
    query(func.args(" [1]", 1, 0), "[]");
    query(func.args(" [1]", 1, 1), "[1]");
    query(func.args(" [1]", 2, 0), "[]");
    query(func.args(" array { 1 to 5 }", 5), "[5]");
    query(func.args(" array { 1 to 5 }", 6), "[]");
    query(func.args(" array { 1 to 5 }", 1, 1), "[1]");
    query(func.args(" array { 1 to 5 }", 2, 3), "[2, 3, 4]");

    error(func.args(" [1]", 0, 0), ARRAYBOUNDS_X_X);
    error(func.args(" [1]", 1, " -1"), ARRAYNEG_X);
    error(func.args(" []", 1, 1), ARRAYBOUNDS_X_X);
    error(func.args(" [1]", 1, 2), ARRAYBOUNDS_X_X);
  }

  /** Test method. */
  @Test public void tail() {
    final Function func = _ARRAY_TAIL;
    query(func.args(" [1]"), "[]");
    query(func.args(" array { 1 to 5 }"), "[2, 3, 4, 5]");
    query(func.args(" [1 to 2, 3]"), "[3]");

    error(func.args(" []"), ARRAYEMPTY);
  }
}
