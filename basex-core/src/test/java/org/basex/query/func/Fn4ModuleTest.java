package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;

/**
 * This class tests standard functions of XQuery 4.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class Fn4ModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void all() {
    final Function func = ALL;

    query(func.args(" ()", " boolean#1"), true);
    query(func.args(1, " boolean#1"), true);
    query(func.args(" 0 to 1", " boolean#1"), false);
    query(func.args(" (1, 3, 7)", " function($_) { $_ mod 2 = 1 }"), true);
    query(func.args(" -5 to 5", " function($_) { $_ ge 0 }"), false);
    query(func.args(" ('January', 'February', 'March', 'April', 'September', 'October',"
        + "'November', 'December')", " contains(?, 'r')"), true);
    check(func.args(" -3 to 3", " function($n) { abs($n) >= 0 }"), true,
        exists(CmpG.class), empty(ALL), exists(NOT));

    final String lookup = "function-lookup(xs:QName(<?_ fn:all?>), 2)";
    query(lookup + "(1 to 9, boolean#1)", true);
    query(lookup + "(1 to 9, not#1)", false);
    query(lookup + "(0 to 9, boolean#1)", false);
    query(lookup + "(0 to 9, not#1)", false);
  }

  /** Test method. */
  @Test public void characters() {
    final Function func = CHARACTERS;

    // test pre-evaluation
    check(func.args(" ()"), "", empty());
    query(func.args(""), "");
    query(func.args("abc"), "a\nb\nc");

    query("count(" + func.args(" string-join(" + REPLICATE.args("A", 100000) + ')') + ')',
        100000);
    check("count(" + func.args(" string-join(" + REPLICATE.args("A", 100000) + ')') + ')',
        100000, empty(func), empty(STRING_LENGTH));

    // test iterative evaluation
    query(func.args(wrap("")), "");
    query(func.args(wrap("abc")), "a\nb\nc");
    query(func.args(wrap("abc")) + "[2]", "b");
    query(func.args(wrap("abc")) + "[last()]", "c");

    query(func.args(wrap("äöü")), "ä\nö\nü");
    query("subsequence(" + func.args(wrap("")) + ", 3)", "");
    query("subsequence(" + func.args(wrap("aeiou")) + ", 3)", "i\no\nu");
    query("subsequence(" + func.args(wrap("äeiöü")) + ", 3)", "i\nö\nü");

    check("count(" + func.args(" string-join(" +
        REPLICATE.args(wrap("A"), 100000) + ')') + ')', 100000, exists(STRING_LENGTH));
    check("string-to-codepoints(" + wrap("AB") + ") ! codepoints-to-string(.)",
        "A\nB", root(func));
  }

  /** Test method. */
  @Test public void highest() {
    final Function func = HIGHEST;
    query(func.args(" ()"), "");
    check(func.args(" ('a', 'b', 'c', 'd', 'e', 'f')[. = 'f']"), "f");
    check(func.args(" ('a', 'b', 'c', 'd', 'e', 'f')[. = 'g']"), "");
    query(func.args(" 'x'"), "x");
    query(func.args(" (1e0, 2e0)"), 2);
    query(func.args(" (8 to 11)"), 11);
    query(func.args(" reverse(8 to 11)"), 11);
    query(func.args(" (8 to 11)", " ()", " string#1"), 9);
    query(func.args(" reverse(8 to 11)", " ()", " string#1"), 9);
    query(func.args(" (3, 2, 1)", " ()", " function($k) { true() }"), "3\n2\n1");
    query(func.args(" (8 to 11)", " ()",
        " function($k) { string-length(string($k)) }"), "10\n11");
    query(func.args(" reverse(8 to 11)", " ()",
        " function($k) { string-length(string($k)) }"), "11\n10");
    query(func.args(" (<a _='1'/>, <b _='2'/>)", " ()",
        " function($k) { $k/@* }") + " ! name()", "b");
    query(func.args(" <_ _='1'/>", " ()",
        " function($a) { $a/@* }"), "<_ _=\"1\"/>");
    query(func.args(" (<_ _='9'/>, <_ _='10'/>)", " ()",
        " function($a) { $a/@* }"), "<_ _=\"10\"/>");
    query(func.args(" (<_ _='9'/>, <_ _='10'/>)", " ()",
        " function($a) { string($a/@*) }"), "<_ _=\"9\"/>");
    check(func.args(" replicate('a', 2)"), "a\na", root(SingletonSeq.class));
    check(func.args(" replicate(<_/>, 2)"), "<_/>\n<_/>", root(REPLICATE));
    check(func.args(" reverse( (1 to 6)[. > 3] )"), 6, empty(REVERSE));

    error(func.args(" (1, 'x')"), CMPTYPES_X_X_X_X);
    error(func.args(" (xs:gYear('9998'), xs:gYear('9999'))"), CMPTYPE_X_X_X);
    error(func.args(" true#0"), FIATOM_X_X);
  }

  /** Test method. */
  @Test public void identity() {
    final Function func = IDENTITY;
    query(func.args(" ()"), "");
    query(func.args(" <x/>"), "<x/>");
    query(func.args(" 1 to 10"), "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
    query("reverse(9 to 10000) => sort((), identity#1) => head()", 9);
  }

  /** Test method. */
  @Test public void isNaN() {
    final Function func = IS_NAN;
    query(func.args(" xs:double('NaN')"), true);
    query(func.args(" xs:float('NaN')"), true);
    query(func.args(" number('twenty-three')"), true);
    query(func.args(" math:sqrt(-1)"), true);

    query(func.args(23), false);
    query(func.args("NaN"), false);
    query(func.args(" <_/>"), false);
    query(func.args(" <?_ ?>"), false);
    query(func.args(" number('1')"), false);
    query(func.args(" xs:double('INF')"), false);
    query(func.args(" xs:decimal(<?_ 1?>)"), false);
    query(func.args(" xs:integer(<?_ 1?>)"), false);
    query(func.args(" xs:byte(<?_ 1?>)"), false);
  }

  /** Test method. */
  @Test public void lowest() {
    final Function func = LOWEST;
    query(func.args(" ()"), "");
    check(func.args(" ('a', 'b', 'c', 'd', 'e', 'f')[. = 'f']"), "f");
    check(func.args(" ('a', 'b', 'c', 'd', 'e', 'f')[. = 'g']"), "");
    query(func.args(" 'x'"), "x");
    query(func.args(" (1e0, 2e0)"), 1);
    query(func.args(" (8 to 11)"), 8);
    query(func.args(" reverse(8 to 11)"), 8);
    query(func.args(" (8 to 11)", " ()", " string#1"), 10);
    query(func.args(" reverse(8 to 11)", " ()", " string#1"), 10);
    query(func.args(" (3, 2, 1)", " ()", " function($k) { true() }"), "3\n2\n1");
    query(func.args(" (8 to 11)", " ()",
        " function($k) { string-length(string($k)) }"), "8\n9");
    query(func.args(" reverse(8 to 11)", " ()",
        " function($k) { string-length(string($k)) }"), "9\n8");
    query(func.args(" (<a _='1'/>, <b _='2'/>)", " ()",
        " function($k) { $k/@* }") + " ! name()", "a");
    query(func.args(" <_ _='1'/>", " ()",
        " function($a) { $a/@* }"), "<_ _=\"1\"/>");
    query(func.args(" (<_ _='9'/>, <_ _='10'/>)", " ()",
        " function($a) { $a/@* }"), "<_ _=\"9\"/>");
    query(func.args(" (<_ _='9'/>, <_ _='10'/>)", " ()",
        " function($a) { string($a/@*) }"), "<_ _=\"10\"/>");
    check(func.args(" replicate('a', 2)"), "a\na", root(SingletonSeq.class));
    check(func.args(" replicate(<_/>, 2)"), "<_/>\n<_/>", root(REPLICATE));
    check(func.args(" reverse( (1 to 6)[. > 3] )"), 4, empty(REVERSE));

    error(func.args(" (1, 'x')"), CMPTYPES_X_X_X_X);
    error(func.args(" (xs:gYear('9998'), xs:gYear('9999'))"), CMPTYPE_X_X_X);
    error(func.args(" true#0"), FIATOM_X_X);
  }

  /** Test method. */
  @Test public void replicate() {
    final Function func = REPLICATE;

    query(func.args(" ()", 0), "");
    query(func.args(" ()", 1), "");
    query(func.args(1, 0), "");
    query(func.args("A", 1), "A");
    query(func.args("A", 2), "A\nA");
    query(func.args(" (0, 'A')", 1), "0\nA");
    query(func.args(" (0, 'A')", 2), "0\nA\n0\nA");
    query(func.args(" 1 to 10000", 10000) + "[last()]", "10000");
    query(func.args(" 1 to 10000", 10000) + "[1]", "1");
    query(func.args(" 1 to 10000", 10000) + "[10000]", "10000");
    query(func.args(" 1 to 10000", 10000) + "[10001]", "1");
    query("count(" + func.args(" 1 to 1000000", 1000000) + ")", 1000000000000L);
    query("count(" + func.args(func.args(" 1 to 3", 3), 3) + ")", 27);

    query("for $i in 1 to 2 return " + func.args(1, " $i"), "1\n1\n1");
    query(func.args(" <a/>", 2), "<a/>\n<a/>");
    query(func.args(" <a/>", wrap(2)), "<a/>\n<a/>");

    query(func.args(" 1[. = 1]", 2), "1\n1");

    check(func.args(" <a/>", -1), "", empty());
    check(func.args(" <a/>", 0), "", empty());
    check(func.args(" ()", wrap(2)), "", empty());
    check(func.args(" <a/>", 1), "<a/>", empty(func));
    check(func.args(" <a/>", 2), "<a/>\n<a/>", type(func, "element(a)+"));
    check(func.args(" <a/>", wrap(2)), "<a/>\n<a/>", type(func, "element(a)*"));

    check(func.args(func.args(" <a/>", 2), 2),
        "<a/>\n<a/>\n<a/>\n<a/>", count(REPLICATE, 1));
    check(func.args(" <_/>", 2) + " ! " + func.args(" .", 2),
        "<_/>\n<_/>\n<_/>\n<_/>", count(REPLICATE, 1));
    check("(1, 1) ! " + func.args(" .", 2),
        "1\n1\n1\n1", empty(REPLICATE));

    check("(1, 1) ! " + func.args(" .", 2),
        "1\n1\n1\n1", empty(REPLICATE));
  }

  /** Test method. */
  @Test public void some() {
    final Function func = SOME;

    query(func.args(" ()", " boolean#1"), true);
    query(func.args(1, " boolean#1"), true);
    query(func.args(" 0 to 1", " boolean#1"), true);
    query(func.args(" (1, 3, 7)", " function($_) { $_ mod 2 = 1 }"), true);
    query(func.args(" -5 to 5", " function($_) { $_ ge 0 }"), true);
    query(func.args(" ('January', 'February', 'March', 'April', 'September', 'October',"
        + "'November', 'December')", " contains(?, 'r')"), true);
    query(func.args(" ('January', 'February', 'March', 'April', 'September', 'October',"
        + "'November', 'December')", " contains(?, 'z')"), false);
    check(func.args(" -3 to 3", " function($n) { abs($n) >= 0 }"), true,
        exists(CmpG.class), empty(ALL));

    final String lookup = "function-lookup(xs:QName(<?_ fn:some?>), 2)";
    query(lookup + "(1 to 9, boolean#1)", true);
    query(lookup + "(1 to 9, not#1)", false);
    query(lookup + "(0 to 9, boolean#1)", true);
    query(lookup + "(0 to 9, not#1)", true);
  }

  /** Test method. */
  @Test public void uniform() {
    final Function func = UNIFORM;

    query(func.args(" ()"), true);
    query(func.args(1), true);
    query(func.args("x"), true);
    query(func.args(" (1, 1)"), true);
    query(func.args(" (1 to 1000) ! 1"), true);
    query(func.args(" (1 to 1000) ! 'x'"), true);
    query(func.args(" (1 to 2)"), false);
    query(func.args(" (1, 2, 3)"), false);
    query(func.args(" (1, 2, 3) ! string()"), false);
    query(func.args(" (1, '1')"), false);
    query(func.args(" (1, 1.0, 1e0)"), true);

    query(func.args(" <a/>[. = '']"), true);
    query(func.args(" (<a/>, <b/>)[. = '']"), true);
    query(func.args(" <a/>[. != '']"), true);
    query(func.args(" (<a/>, <b/>)[. != '']"), true);

    query(func.args(" []"), true);
    query(func.args(" [ 1 ]"), true);
    query(func.args(" [ 1, 1 ]"), true);
    query(func.args(" [ 1, 2 ]"), false);

    query(func.args(" (1 to <_>1</_>/text())"), true);
    query(func.args(" (1 to <_>2</_>/text())"), false);

    query(func.args(" replicate((1 to 100)[. < 1], 100)"), true);
    query(func.args(" replicate((1 to 100)[. < 2], 100)"), true);
    query(func.args(" replicate((1 to 100)[. < 3], 100)"), false);
    query(func.args(" reverse((1 to 10) ! string())"), false);
    query(func.args(" sort((1 to 10) ! string())"), false);

    final String c = "http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive";
    query(func.args(" ('A', 'a')", c), true);
    query(func.args(" ('A', 'b')", c), false);
  }

  /** Test method. */
  @Test public void unique() {
    final Function func = UNIQUE;

    query(func.args(" ()"), true);
    query(func.args(1), true);
    query(func.args("x"), true);
    query(func.args(" (1, 1)"), false);
    query(func.args(" (1 to 1000) ! 1"), false);
    query(func.args(" (1 to 1000) ! 'x'"), false);
    query(func.args(" (1 to 2)"), true);
    query(func.args(" (1, 2, 3)"), true);
    query(func.args(" (1, 2, 3) ! string()"), true);
    query(func.args(" (1, '1')"), true);
    query(func.args(" (1, 1.0, 1e0)"), false);

    query(func.args(" <a/>[. = '']"), true);
    query(func.args(" (<a/>, <b/>)[. = '']"), false);
    query(func.args(" <a/>[. != '']"), true);
    query(func.args(" (<a/>, <b/>)[. != '']"), true);

    query(func.args(" []"), true);
    query(func.args(" [ 1 ]"), true);
    query(func.args(" [ 1, 1 ]"), false);
    query(func.args(" [ 1, 2 ]"), true);

    query(func.args(" (1 to <_>1</_>/text())"), true);
    query(func.args(" (1 to <_>2</_>/text())"), true);

    query(func.args(" replicate((1 to 100)[. < 1], 100)"), true);
    query(func.args(" replicate((1 to 100)[. < 2], 100)"), false);
    query(func.args(" replicate((1 to 100)[. < 3], 100)"), false);
    query(func.args(" reverse((1 to 10) ! string())"), true);
    query(func.args(" sort((1 to 10) ! string())"), true);

    final String c = "http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive";
    query(func.args(" ('A', 'a')", c), false);
    query(func.args(" ('A', 'b')", c), true);
  }
}
