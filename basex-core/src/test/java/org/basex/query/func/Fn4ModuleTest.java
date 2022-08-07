package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.junit.jupiter.api.*;

/**
 * This class tests standard functions of XQuery 4.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class Fn4ModuleTest extends QueryPlanTest {
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
}
