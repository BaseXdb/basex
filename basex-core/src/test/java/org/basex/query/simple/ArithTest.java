package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;

/**
 * Arithmetic tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArithTest extends SandboxTest {
  /** Test method. */
  @Test public void plus() {
    check("for $i in (1 to 2)[. != 0] return ($i * $i) + 1", "2\n5", exists(Arith.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i + 0",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0 + $i",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0e0 + $i",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return $i + 0e0",
        "1\n2", exists(Cast.class));

    // counts
    check("let $n := " + wrap(1) + "[. = 1] return count($n) + count($n)",
        2, count(Function.COUNT, 1), exists(Function.REPLICATE));
    check("let $n := " + wrap(1) + "[. = 1] return count($n) + count($n) > 0",
        true, root(CmpSimpleG.class));

    // negative zero: -0 + 0 → 0, -0 + -0 → -0
    final String z = "xs:double(" + wrap("-0") + ")";
    query(z + "+ 0", 0);
    check(z + "+ -0e0", "-0", empty(Arith.class));

    check(wrap(3) + "! (. + .)", 6, exists(Itr.class), count(Arith.class, 1));
    check(wrap(3) + "! (. + . + .)", 9, exists(Itr.class), count(Arith.class, 2));
    check("xs:decimal(" + wrap(3) + ") ! (. + . + .)", 9, root(Arith.class));
    check(wrap(3) + "! (. + -.)", 0, empty(Unary.class));
    check("xs:decimal(" + wrap(3) + ") ! (. + -.)", 0, empty(Unary.class), root(Dec.class));
  }

  /** Test method. */
  @Test public void minus() {
    check("for $i in (1 to 2)[. != 0] return ($i * $i) - 1", "0\n3", exists(Arith.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i - 0",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return $i - 0e0",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return 0 - $i",
        "-1\n-2", exists(Unary.class));

    // identical arguments
    check("for $i in (1, xs:double('NaN'))[. != 0] return $i - $i",
        "0\nNaN", exists(Arith.class));
    check("for $i in (1 to 2)[. != 0] return $i - $i",
        "0\n0", empty(Arith.class), empty(GFLWOR.class));

    query("string(xs:dateTime('2017-07-07T18:30:00.1') - xs:dayTimeDuration('PT1S'))",
        "2017-07-07T18:29:59.1");
    query("string(xs:dateTime('2017-07-07T18:00:59.1') - xs:dayTimeDuration('PT1M'))",
        "2017-07-07T17:59:59.1");

    check("xs:decimal(" + wrap(3) + ") ! (. - .)", 0, root(Dec.class));
    // negative zero: -0 - -0 → 0, -0 - 0 → -0, 0 - 0 → 0
    final String z = "xs:double(" + wrap("-0") + ")";
    query(z + "- -0e0", 0);
    check(z + "- 0", "-0", empty(Arith.class));
    query("0 - xs:double(" + wrap(0) + ")", 0);

    check(wrap(3) + "! (. + . - .)", 3, exists(Arith.class));
    check("xs:decimal(" + wrap(3) + ") ! (. + . - .)", 3, exists(Cast.class),
        empty(Arith.class));
    // floating-point numbers: rounding errors
    query("xs:double(" + wrap("0.42857142857142855") + ") ! (. * 3 - .)", "0.857142857142857");
    check(wrap(3) + "! (. - -.)", 6, exists(Itr.class), empty(Unary.class),
        count(Arith.class, 1));
  }

  /** Test method. */
  @Test public void mult() {
    check("for $i in (1 to 2)[. != 0] return $i * 2", "2\n4", exists(Arith.class));

    // neutral number
    check("for $i in (1 to 2)[. != 0] return $i * 1",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 1 * $i",
        "1\n2", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 1e0 * $i",
        "1\n2", exists(Cast.class));
    check("for $i in (1 to 2)[. != 0] return $i * 1e0",
        "1\n2", exists(Cast.class));

    // absorbing number
    check("for $i in (1 to 2)[. != 0] return $i * 0",
        "0\n0", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0 * $i",
        "0\n0", empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (1 to 2)[. != 0] return 0e0 * $i",
        "0\n0", exists(Arith.class));
    check("for $i in (1 to 2)[. != 0] return $i * 0e0",
        "0\n0", exists(Arith.class));

    check(wrap(3) + "! (. * .)", 9, exists(Dbl.class), exists(Function._MATH_POW));
    check(wrap(3) + "! (. * . * .)", 27, exists(Dbl.class), count(Function._MATH_POW, 1));
    // rounding errors (also for decimals)
    query(wrap(49) + "! (. * (1 div .))", "0.9999999999999999");
    query("xs:decimal(" + wrap(3) + ") ! (. * (1 div .))", "0.999999999999999999");
  }

  /** Test method. */
  @Test public void div() {
    check("for $i in (2, 4)[. != 0] return $i div 2", "1\n2", exists(Arith.class));
    check("for $i in (2, 4)[. != 0] return 1 div $i", "0.5\n0.25", exists(Arith.class));

    // neutral number
    check("for $i in (2.0, 4.0) return $i div 1", "2\n4",
        empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2e0, 4e0) return $i div 1", "2\n4",
        empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2, 4)[. != 0] return $i div 1e0", "2\n4",
        exists(Cast.class));

    // identical arguments
    check("for $i in (1, xs:double('NaN'))[. != 0] return $i div $i",
        "1\nNaN", exists(Arith.class));
    check("for $i in (2.0, 4.0)[. != 0] return $i div $i", "1\n1",
        empty(Arith.class), empty(GFLWOR.class));

    error("xs:dayTimeDuration('PT0S') div xs:dayTimeDuration('PT0S')", DIVZERO_X);
    error("xs:yearMonthDuration('P0M') div xs:yearMonthDuration('P0M')", DIVZERO_X);

    check("xs:decimal(" + wrap(3) + ") ! (. div .)", 1, root(Dec.class));
    check(wrap(3) + "! (. * . div .)", 3, exists(Arith.class));
    query("xs:double(" + wrap(0) + ") ! (math:pow(., 2) div .)", "NaN");

    // integer division yields decimals
    final String i = "xs:integer(" + wrap(6) + ")";
    query("(" + i + " div 1) instance of xs:decimal", true);
    query("(" + i + " div 1) instance of xs:integer", false);
    query("(" + i + " ! (. div .)) instance of xs:integer", false);
    query("(" + i + " * 3 div 3) instance of xs:integer", false);
    query("(" + i + " + 3 - 3) instance of xs:integer", true);

    // decimals: at least 18 fractional and 18 significant digits
    query("1 div 3.0", "0.333333333333333333");
    query("10 div 3.0", "3.333333333333333333");
    query("6.022 div 100000000000000000000000.0", "0.00000000000000000000006022");
    query("1 div 300000000000000000000.0", "0.00000000000000000000333333333333333333");
  }

  /** Test method. */
  @Test public void idiv() {
    check("for $i in (2, 4)[. != 0] return $i idiv 2", "1\n2", exists(Arith.class));
    check("for $i in (2, 4)[. != 0] return 1 idiv $i", "0\n0", exists(Arith.class));

    // neutral number
    check("for $i in (2, 4) return $i idiv 1e0", "2\n4",
        empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2, 4) return $i idiv 1", "2\n4",
        empty(Arith.class), empty(GFLWOR.class));
    check("for $i in (2, 4) return $i idiv 1", "2\n4",
        empty(Arith.class), empty(GFLWOR.class));

    // identical arguments
    error("for $i in (1, xs:double('NaN')) return $i idiv $i", INVIDIV_X);
    check("for $i in (2, 4) return $i idiv $i", "1\n1",
        empty(Arith.class), empty(GFLWOR.class));

    check("xs:decimal(" + wrap(3) + ") ! (. idiv .)", 1, root(Itr.class));

    // GH-2111
    check("xs:float  (1.13) idiv xs:float  (1.13)", 1, root(Itr.class));
    check("xs:double (1.13) idiv xs:double (1.13)", 1, root(Itr.class));
    check("xs:double (1.13) idiv xs:float  (1.13)", 1, root(Itr.class));
    check("xs:float  (1.13) idiv xs:double (1.13)", 0, root(Itr.class));
  }

  /** Test method. */
  @Test public void mod() {
    check("for $i in (-1, 0, 1) return $i mod 1", "0\n0\n0", empty(Arith.class));
  }

  /** Merge arithmetic expressions. */
  @Test public void gh1938() {
    final String i = "xs:integer(" + wrap(1) + ")";
    check("(" + i + "+ 1 - 1)[. instance of xs:integer]", 1, root(Cast.class));
    check("xs:integer(" + wrap(6) + ") * 3 * 2", 36, count(Arith.class, 1));
    check("(1 to 2) ! (" + i + "+ . - .)", "1\n1", empty(Arith.class));
    check("(1 to 2) ! (" + i + "* . div .)", "1\n1", empty(Arith.class));
    check("(1 to 6) ! (. + 1 - .)", "1\n1\n1\n1\n1\n1",
        empty(Arith.class), root(SingletonSeq.class));
    query(i + "* 7 idiv 3 idiv 2", 1);
    query(i + "* 7 mod 4 mod 2", 1);

    // floating-point and decimal division: no rewrite
    final String d = "xs:double(" + wrap("1e308") + ")";
    query(d + "+ 1e308 - 1e308", "INF");
    query(d + "* 10 div 10", "INF");
    query(wrap("1e-20") + "+ 1 - 1", 0);
    query("(1 to 2) ! (" + wrap("1e-20") + "+ . - .)", "0\n0");
    query("xs:decimal(" + wrap(1) + ") div 3 * 3", "0.999999999999999999");
    query("xs:decimal(" + wrap(2) + ") * 2 div 3", "1.333333333333333333");
  }

  /** Linear combinations of integers and decimals. */
  @Test public void linear() {
    final String xy = "let $x := xs:integer(" + wrap(1) + ") let $y := xs:integer(" + wrap(2) +
        ") return ";
    check(xy + "($x + 1) + ($y + 2)", 6, count(Itr.class, 1));
    check(xy + "$x * 2 + $y - $x * 3", 1, empty(Itr.class));
    check(xy + "2 * ($x + $y) - $y", 4, count(Itr.class, 1));
    check(xy + "($x + 1) * 2 + 3", 7, count(Itr.class, 2));
    check(xy + "$x + $y - $y", 1, empty(Arith.class));
    check(xy + "$x + 1 - $x + $x - 1", 1, empty(Arith.class));
    check(xy + "-$x + $y + $x", 2, empty(Arith.class));
    check(xy + "$x * 2 - $x - $x", 0, root(Itr.class));
    // preserve result type
    query(xy + "($x + 1.0 - 1.0) instance of xs:integer", false);
    query(xy + "($x * 0.5 + $x * 0.5) instance of xs:integer", false);
    query(xy + "(xs:byte($x) + 1 - 1) instance of xs:byte", false);
    // no rewrite: integer overflow
    error(xy + "$x * 9223372036854775807 + $x", RANGE_X);

    // comparisons
    check(xy + "$x + $y = $y + 1", true, empty(Arith.class), empty(Unary.class));
    check(xy + "2 * $x + 3 = 5", true, empty(Arith.class), empty(Unary.class));
    check(xy + "$x * 2 = 3", false, root(Bln.class));
    check(xy + "$x * 2 != 3", true, root(Bln.class));
    check(xy + "-$x < -3", false, empty(Arith.class), empty(Unary.class));
    check(xy + "3 - $x >= 1", true, empty(Arith.class), empty(Unary.class));
    check(xy + "$x + 1 = $x + 2", false, root(Bln.class));
    check(xy + "$x + 1 < $x + 2", true, root(Bln.class));
    // no rewrite: floating-point numbers
    query("let $d := xs:double(" + wrap("1e-20") + ") return $d + 1 = 1", true);
  }

  /** Identities that also apply to floating-point numbers. */
  @Test public void identities() {
    for(final String type : new String[] { "integer", "double" }) {
      final String xy = "let $x := xs:" + type + "(" + wrap(1) + ") let $y := xs:" + type +
          "(" + wrap(2) + ") return ";
      check(xy + "-$x * -$y", 2, empty(Unary.class));
      check(xy + "-$x div -$y", 0.5, empty(Unary.class));
      check(xy + "-$x idiv -$y", 0, empty(Unary.class));
      check(xy + "-$x + $y", 1, empty(Unary.class));
      check(xy + "-$x mod -$y", -1, exists(Unary.class));

      check(xy + "abs(abs(-$x))", 1, count(Function.ABS, 1), empty(Unary.class));
      check(xy + "floor(ceiling($x div $y))", 1, empty(Function.FLOOR));
      check(xy + "round(floor($x div $y))", 0, empty(Function.ROUND));
      check(xy + "round(floor($x div $y), -1)", 0, exists(Function.ROUND));
      check(xy + "round(floor($x div $y), 2)", 0, empty(Function.ROUND));
    }
    // unary plus
    query("1 + +" + wrap(1), 2);
    query("1 - +" + wrap(1), 0);
    // negative zero
    final String z = "xs:double(" + wrap("-0") + ")";
    query("-" + z + " * -" + z, 0);
    query("-" + z + " + " + z, 0);
    query("abs(-" + z + ")", 0);
    query("floor(ceiling(" + z + "))", "-0");
  }

  /** Aggregations of arithmetic expressions. */
  @Test public void aggregate() {
    final String e = "(1 to " + wrap(5) + ")";
    // monotonic functions: max(E ! (. + 1)) → max(E) + 1
    check("max(" + e + " ! (. + 1))", 6, exists(Function.MAX), empty(SimpleMap.class));
    check("min(" + e + " ! (. - 1))", 0, exists(Function.MIN), empty(SimpleMap.class));
    check("max(" + e + " ! (. * -2))", -2, exists(Function.MIN), empty(SimpleMap.class));
    check("max(" + e + " ! (10 - .))", 9, exists(Function.MIN), empty(SimpleMap.class));
    check("min(" + e + " ! (. div -2))", -2.5, exists(Function.MAX), empty(SimpleMap.class));
    check("max((" + wrap(1) + ", " + wrap(2) + ") ! (. + 0.5))", 2.5, empty(SimpleMap.class));
    check("max(" + e + " ! (. * 0))", 0, exists(SimpleMap.class));
    check("max(" + e + " ! (. * 2 + 1))", 11, empty(SimpleMap.class));
    check("max(" + e + " ! (abs(.) + 1))", 6, exists(Function.MAX), exists(SimpleMap.class),
        count(Arith.class, 1));
    check("max(" + e + " ! (-.))", -1, exists(Function.MIN), empty(SimpleMap.class));
    check("max((element b { " + wrap(1) + " }, element b { " + wrap(2) + " }) ! (. * 2))", 4,
        exists(Function.MAX), empty(SimpleMap.class));
    query("max((" + wrap("NaN") + ", 1) ! xs:double(.) ! (. + 1))", "NaN");
    query("max((1 to " + wrap(0) + ") ! (. + 1))", "");
    check("max((xs:float(" + wrap("1.1") + "), 1e0) ! (. + 1))", 2.1, exists(SimpleMap.class));

    // sum(E ! (. * 2)) → sum(E) * 2, sum(E ! (. + 1)) → sum(E) + count(E)
    check("sum(" + e + " ! (. * 2))", 30, empty(SimpleMap.class));
    final String n = "let $n := xs:integer(" + wrap(5) + ") return ";
    check(n + "sum((1 to $n) ! (. + 1))", 20, empty(SimpleMap.class));
    check(n + "sum((1 to $n) ! (10 - .))", 35, empty(SimpleMap.class));
    check(n + "sum((1 to $n) ! (. * 2 + 1))", 35, empty(SimpleMap.class));
    check("sum(" + e + " ! (. + 1))", 20, exists(SimpleMap.class));
    check("sum(" + e + " ! (-.))", -15, empty(SimpleMap.class));
    query("sum((1 to " + wrap(0) + ") ! (. * 2)) instance of xs:integer", true);
    check("sum((" + wrap(1) + ", " + wrap(2) + ") ! (. * 2))", 6, exists(SimpleMap.class));
    check("sum(" + e + " ! (. * 2), 0)", 30, exists(SimpleMap.class));

    // sum(E) div count(E) → avg(E)
    check("let $e := " + e + " return sum($e) div count($e)", 3, exists(Function.AVG),
        empty(Function.SUM));
  }

  /** Ranges. */
  @Test public void range() {
    final String n = "let $n := xs:integer(" + wrap(3) + ") return ";
    check(n + "(1 to $n) ! (. + 1)", "2\n3\n4", root(Range.class));
    check(n + "($n to 5) ! (. - 2)", "1\n2\n3", root(Range.class));
    check(n + "(5 to $n) ! (. + 1)", "", root(Range.class));
    // known result size
    check(n + "($n to $n + 10) ! (. + 1)", "4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14",
        "//Range/@size = 11");
    check(n + "$n - 1 to $n + 1", "2\n3\n4", "//Range/@size = 3");
    check(n + "$n + 1 to $n - 1", "", empty());
    check(n + "count($n to $n + random:integer(9) - random:integer(9)) >= 0", true,
        "empty(//Range/@size)");
    check(n + "$n to $n - 1 + random:integer(9) * 0", "", exists(Function._RANDOM_INTEGER));
    // untyped bounds: no rewrite (double arithmetic)
    check("(1 to " + wrap(3) + ") ! (. + 1)", "2\n3\n4", exists(SimpleMap.class));
  }

  /** Arithmetic positions and lengths. */
  @Test public void positions() {
    final String i = "let $i := xs:integer(" + wrap(2) + ") ";
    final String s = i + "let $s := string(" + wrap("abcde") + ") return ";
    check(s + "substring($s, $i, string-length($s) - $i + 1)", "bcde",
        empty(Function.STRING_LENGTH));
    query("substring(" + wrap("abc") + ", 5, 9223372036854775807)", "");
    // no rewrite: $i may be smaller than 1
    check(s + "substring($s, $i, string-length($s))", "bcde", exists(Function.STRING_LENGTH));
    check(s + "substring($s, $i, string-length($s) - $i + 3)", "bcde",
        empty(Function.STRING_LENGTH));
    check(s + "substring($s, $i, string-length($s) - $i)", "bcd",
        exists(Function.STRING_LENGTH));

    final String e = i + "let $e := tokenize(string(" + wrap("a b c d e") + ")) return ";
    check(e + "subsequence($e, $i, count($e) - $i + 1)", "b\nc\nd\ne", empty(Function.COUNT));
    check(e + "subsequence($e, $i, count($e) - $i)", "b\nc\nd", exists(Function.TRUNK),
        empty(Function.COUNT));
    check(e + "subsequence($e, $i, count($e) - $i - 1)", "b\nc", exists(Function.COUNT));
    check(e + "util:range($e, $i, $i - 1)", "", empty(Function._UTIL_RANGE));
    check(e + "util:range($e, $i, $i)", "b", empty(Function._UTIL_RANGE));
    check(e + "remove($e, count($e))", "a\nb\nc\nd", exists(Function.TRUNK));
    check(e + "remove($e, count($e) + 1)", "a\nb\nc\nd\ne", empty(Function.REMOVE));
    check(e + "insert-before($e, count($e) + 1, 'x')", "a\nb\nc\nd\ne\nx",
        empty(Function.INSERT_BEFORE));
  }

  /** Integer division and modulo. */
  @Test public void idivMod() {
    final String n = "let $n := xs:integer(" + wrap(9) + ") return ";
    final String d = "let $d := xs:double(" + wrap("9.5") + ") return ";
    final String mods = "count(//Arith[@op = 'mod'])";
    check(n + "$n * 6 idiv 3", 18, count(Arith.class, 1));
    check(n + "$n * 2 idiv 6", 3, count(Arith.class, 1));
    check(n + "$n * -2 idiv 6", -3, count(Arith.class, 1));
    check(n + "$n * 4 idiv 6", 6, count(Arith.class, 2));
    check(n + "$n * 6 mod 3", 0, root(Itr.class));
    check(n + "$n * 6 mod 4", 2, count(Arith.class, 2));
    check(n + "$n mod 4 mod 4", 1, mods + " = 1");
    check(n + "$n mod $n", 0, root(Itr.class));
    check("xs:decimal(" + wrap("2.5") + ") ! (. mod .)", 0, root(Dec.class));
    check(d + "$d mod $d", 0, mods + " = 1");
    query("xs:double(" + wrap("INF") + ") ! (. mod .)", "NaN");
    check(d + "$d mod 4 mod 4", 1.5, mods + " = 1");
    check(d + "$d * 6 idiv 3", 19, count(Arith.class, 2));
  }

  /** Comparisons. */
  @Test public void compare() {
    final String xy = "let $x := xs:integer(" + wrap(1) + ") let $y := xs:integer(" + wrap(2) +
        ") return ";
    // two terms
    check(xy + "$x + 1 = $y + 1", false, empty(Arith.class));
    check(xy + "2 * $x < 2 * $y", true, empty(Arith.class));
    check(xy + "-$x < -$y", false, empty(Arith.class), empty(Unary.class));
    check(xy + "$x + $y = $y + $x", true, root(Bln.class));

    // integer bounds: $x * 2 < 3 → $x <= 1
    check(xy + "$x * 2 < 3", true, exists(CmpIR.class));
    check(xy + "$x * 2 <= 3", true, exists(CmpIR.class));
    check(xy + "$x * 2 > 3", false, exists(CmpIR.class));
    check(xy + "$x * -2 >= -3", true, exists(CmpIR.class));
    check(xy + "$y * 2 > 3", true, exists(CmpIR.class));

    // zero-or-one operands
    final String o = "let $o := xs:integer(" + wrap(3) + ")[. > 0] return ";
    final String oe = "let $o := xs:integer(" + wrap(3) + ")[. > 5] return ";
    check(o + "$o * 2 = 6", true, empty(Arith.class));
    check(oe + "$o * 2 = 6", false, empty(Arith.class));
    check(o + "$o + $o = 6", true, empty(Arith.class));
    query(oe + "$o * 2 != 3", false);
    query(oe + "$o - $o + 1 = 1", false);

    // minimum and maximum
    final String e = "(1 to " + wrap(5) + ")";
    check("max(" + e + ") > 3", true, empty(Function.MAX));
    check("max(" + e + ") >= 5", true, empty(Function.MAX));
    check("min(" + e + ") < 2", true, empty(Function.MIN));
    check("max(" + e + ") < 3", false, exists(Function.MAX));
    check("3 < max(" + e + ")", true, empty(Function.MAX));
    check("max(" + e + ") > max((1 to " + wrap(3) + "))", true, count(Function.MAX, 1));
    check("max(" + e + ") > (6, 4)", true, exists(Function.MAX));
    check("max(" + e + " ! (. + 1)) > 3", true, empty(Function.MAX));
    query("max((1 to " + wrap(0) + ")) > 3", false);
    check("max((" + wrap("NaN") + ", 5) ! xs:double(.)) > 3", false, exists(Function.MAX));

    // non-negative operands
    final String u = wrap(-1);
    check("abs(" + u + ") < 0", false, root(Bln.class));
    check("abs(" + u + ") = -1", false, root(Bln.class));
    check(xy + "abs($x) >= 0", true, root(Bln.class));
    check(xy + "$x * $x < 0", false, root(Bln.class));
    check(xy + "$x * $x != -1", true, root(Bln.class));
    check("let $d := xs:double(" + u + ") return $d * $d < 0", false, root(Bln.class));
    // no rewrite: NaN, empty sequence
    check("abs(xs:double(" + wrap("NaN") + ")) >= 0", false, exists(Function.ABS));
    check("abs(" + u + "[. = 0]) != -1", false, exists(Function.ABS));
  }

  /** Simplify arithmetic expressions. */
  @Test public void simplify() {
    final String i = "xs:integer(" + wrap(1) + ")";
    check(i + "- 1 = 0", true, empty(Arith.class), count(Itr.class, 1));
    check(i + "- 1 = " + i + " - 1", true, empty(Arith.class), empty(Itr.class));
    check(i + "- 1 != " + i + " - 2", true, root(Bln.class));

    // untyped values: no rewrite (double arithmetic, decimal comparison)
    check(wrap(1) + "- 1 = 0", true, exists(Arith.class));
    query(wrap("1.00000000000000001") + "- 1 = 0", true);
    query(wrap("1.00000000000000001") + "- 1 = " + wrap(1) + " - 1", true);

    // mixed types: no rewrite
    query("some $v in (1 to 20) ! (. div 10) satisfies $v = 0.1e0", false);
    query("(1 to 20)[. div 10 = 0.1e0]", "");
    query("(1 to 20)[. div 10 = 0.1]", 1);
    // no rewrite if the new operand exceeds the integer range
    query("(1 to 3)[. + 1 = -9223372036854775808]", "");
    query("(1 to 3)[. div 2 = 9223372036854775807]", "");
  }

  /** Error in arithmetic calculation result comparison. */
  @Test public void gh2188() {
    query("<x _='1'/>/@* *  2 >= -2", true);
    query("<x _='1'/>/@* *  2 >  -2", true);
    query("<x _='1'/>/@* *  2 <= -2", false);
    query("<x _='1'/>/@* *  2 <  -2", false);
    query("<x _='1'/>/@* *  2  = -2", false);
    query("<x _='1'/>/@* *  2 != -2", true);

    check("<x _='1'/>/@* * -2 >= 2", false, exists(Arith.class));
    check("<x _='1'/>/@* * -2 >  2", false, exists(Arith.class));
    check("<x _='1'/>/@* * -2 <= 2", true, exists(Arith.class));
    check("<x _='1'/>/@* * -2 <  2", true, exists(Arith.class));
    check("<x _='1'/>/@* * -2  = 2", false, exists(Arith.class));
    check("<x _='1'/>/@* * -2 != 2", true, exists(Arith.class));
    check("<x _='1'/>/@* ! xs:integer(.) * -2  = 2", false, empty(Arith.class));
    check("<x _='1'/>/@* ! xs:integer(.) * -2 != 2", true, empty(Arith.class));
    query("<x _='-1.00000000000000001'/>/@* * -2 = 2", true);
  }

  /** Unexpected exception, division by zero. */
  @Test public void gh2189() {
    check("<x _='1'/>/@* * 0  = 0", true, exists(Arith.class));
    check("<x _='1'/>/@* * 0  > 0", false, exists(Arith.class));
    check("<x _='1'/>/@* * 0  = 0", true, exists(Arith.class));
    check("<x _='1'/>/@* * 0 != 0", false, exists(Arith.class));
  }

  /** Number literals. */
  @Test public void literals() {
    query("1.+1.", 2);
  }

  /** Numeric limits. */
  @Test public void limits() {
    query("2 * 4611686018427387903", 9223372036854775806L);
    error("2 * 4611686018427387904", RANGE_X);
    query("-2 * 4611686018427387904", -9223372036854775808L);
    error("4611686018427387905 * -2", RANGE_X);
    query("xs:decimal('18446744073709551616') idiv -2", -9223372036854775808L);
    error("xs:decimal('18446744073709551616') idiv 2", RANGE_X);
    query("xs:decimal('18446744073709551612') idiv 2", 9223372036854775806L);
    error("-9223372036854775808 idiv -1", RANGE_X);
    error("-9223372036854775807 - 1024", RANGE_X);
    error("-9223372036854775808 - 1", RANGE_X);

    // unsigned long values beyond the integer range
    error("xs:unsignedLong('18446744073709551615')", INTRANGE_X);
    error("xs:unsignedLong('9223372036854775808')", INTRANGE_X);
    final String u = "xs:unsignedLong('9223372036854775807')";
    error(u + " + 1", RANGE_X);
    query("+" + u, Long.MAX_VALUE);
    query("-" + u, -Long.MAX_VALUE);
    error("round(" + u + ", -1)", RANGE_X);

    // integer range comparisons
    final String e = "(9223372036854775806, 9223372036854775807, -9223372036854775808)[. != 0]";
    query("count(" + e + "[. > 9223372036854775807])", 0);
    query("count(" + e + "[. = 9223372036854775806])", 1);
    query("count(" + e + "[. < -9223372036854775808])", 0);
    query("count(" + e + "[. <= 9223372036854775807])", 3);
  }

  /** Arithmetics with durations. */
  @Test public void durations() {
    query("string(.5 * xs:yearMonthDuration('P1Y'))", "P6M");
    query("string(<_>.5</_> * xs:yearMonthDuration('P1Y'))", "P6M");
    query("string(xs:yearMonthDuration('P1Y') * .5)", "P6M");
    query("string(xs:yearMonthDuration('P1Y') * <_>.5</_>)", "P6M");
    query("string(xs:yearMonthDuration('P1Y') div .5)", "P2Y");
    query("string(xs:yearMonthDuration('P1Y') div <_>.5</_>)", "P2Y");

    // overflow
    query("string(xs:dayTimeDuration('PT1S') * 1e18)", "P11574074074074DT1H46M40S");
    error("xs:dayTimeDuration('PT1S') * 1e300", SECDURRANGE_X);
    error("xs:dayTimeDuration('PT1S') div 1e-300", SECDURRANGE_X);

    // exact limits
    final String ym = "xs:yearMonthDuration('P9223372036854775807M')";
    query("string(" + ym + ")", "P768614336404564650Y7M");
    query("string(xs:yearMonthDuration('P768614336404564649Y'))", "P768614336404564649Y");
    error("xs:yearMonthDuration('P768614336404564650Y8M')", DURRANGE_X_X);
    query("string(" + ym + " + xs:yearMonthDuration('P0M'))", "P768614336404564650Y7M");
    error(ym + " + xs:yearMonthDuration('P1M')", MONTHRANGE_X);
    error("xs:yearMonthDuration('-P9223372036854775807M') - xs:yearMonthDuration('P1M')",
        MONTHRANGE_X);
    final String dt = "xs:dayTimeDuration('PT9223372036854775807S')";
    query("string(" + dt + ")", "P106751991167300DT15H30M7S");
    error("xs:dayTimeDuration('PT9223372036854775807.5S')", DURRANGE_X_X);
    query("string(xs:dayTimeDuration('PT9223372036854775806S') + xs:dayTimeDuration('PT1S'))",
        "P106751991167300DT15H30M7S");
    error(dt + " + xs:dayTimeDuration('PT1S')", SECDURRANGE_X);
  }
}
