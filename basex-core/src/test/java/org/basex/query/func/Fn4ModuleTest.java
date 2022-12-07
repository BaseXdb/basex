package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;

/**
 * This class tests standard functions of XQuery 4.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class Fn4ModuleTest extends QueryPlanTest {
  /** Document. */
  private static final String DOC = "src/test/resources/input.xml";
  /** Months. */
  private static final String MONTHS = " ('January', 'February', 'March', 'April', 'May', "
      + "'June', 'July', 'August', 'September', 'October', 'November', 'December')";

  /** Test method. */
  @Test public void all() {
    final Function func = ALL;

    query(func.args(" (1 to 10) ! boolean(.)"), true);
    query(func.args(" reverse(1 to 10) ! boolean(.)"), true);
    query(func.args(" reverse(0 to 9) ! boolean(.)"), false);

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
  @Test public void allDifferent() {
    final Function func = ALL_DIFFERENT;

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

  /** Test method. */
  @Test public void allEqual() {
    final Function func = ALL_EQUAL;

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
  @Test public void foot() {
    final Function func = FOOT;

    query(func.args(" ()"), "");
    query(func.args(1), 1);
    query(func.args(" 1 to 2"), 2);

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>, <b/>)"), "<b/>");
    query(func.args(" (<a/>, <b/>)[position() > 2]"), "");

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>, <b/>)"), "<b/>");

    check(func.args(_PROF_VOID.args(" ()")), "", empty(func));
    check(func.args(" <a/>"), "<a/>", empty(func));
    check(func.args(" (<a/>, <b/>)[name()]"), "<b/>", type(HEAD, "element(b)|element(a)?"));
    check(func.args(" reverse((1, 2, 3)[. > 1])"), 2, exists(HEAD));

    check(func.args(" tokenize(<_/>)"), "", exists(FOOT));
    check(func.args(" tokenize(" + wrap(1) + ")"), 1, exists(FOOT));
    check(func.args(" tokenize(" + wrap("1 2") + ")"), 2, exists(FOOT));

    check(func.args(" tail(tokenize(<a/>))"), "", exists(TAIL));
    check(func.args(" tail(1 ! <_>{.}</_>)"), "", empty());
    check(func.args(" tail((1 to 2) ! <_>{.}</_>)"), "<_>2</_>", empty(TAIL));
    check(func.args(" tail((1 to 3) ! <_>{.}</_>)"), "<_>3</_>", empty(TAIL));

    check(func.args(TRUNK.args(" (1 to 3) ! <_>{.}</_>")), "<_>2</_>", empty(TRUNK));
    check(func.args(TRUNK.args(" tokenize(<a/>)")), "", exists(TRUNK));

    check(func.args(REPLICATE.args(" <a/>", 2)), "<a/>", root(CElem.class));
    check(func.args(REPLICATE.args(" <a/>[. = '']", 2)), "<a/>",
        root(IterFilter.class), empty(REPLICATE));
    check(func.args(REPLICATE.args(" (<a/>, <b/>)[. = '']", 2)), "<b/>",
        root(HEAD), empty(REPLICATE));
    check(func.args(REPLICATE.args(" <a/>", " <_>2</_>")), "<a/>", exists(REPLICATE));

    check(func.args(" (<a/>, <b/>)"), "<b/>", root(CElem.class));
    check(func.args(" (<a/>, 1 to 2)"), 2, root(Int.class));
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
    check(func.args(" reverse( (1 to 6)[. > 3] )"), 6, empty(REVERSE));

    error(func.args(" replicate(<_/>, 2)"), FUNCCAST_X_X);
    error(func.args(" xs:QName('x')"), CMPTYPE_X_X_X);
    error(func.args(" (1, 'x')"), CMPTYPES_X_X_X_X);
    error(func.args(" (xs:gYear('9998'), xs:gYear('9999'))"), CMPTYPE_X_X_X);
    error(func.args(" true#0"), FIATOMIZE_X);
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
  @Test public void indexWhere() {
    final Function func = INDEX_WHERE;
    query(func.args(" ()", " boolean#1"), "");
    query(func.args(0, " boolean#1"), "");
    query(func.args(1, " boolean#1"), 1);
    query(func.args(" (0, 4, 9)", " boolean#1"), "2\n3");
    query(func.args(" 1 to 9", " function($n) { $n mod 5 = 0 }"), 5);
    query(func.args(MONTHS, " contains(?, 'z')"), "");
    query(func.args(MONTHS, " contains(?, 'v')"), 11);
    query(func.args(MONTHS, " starts-with(?, 'J')"), "1\n6\n7");

    check(func.args(" (0 to 5)[. = 0]", " not#1"), 1, root(GFLWOR.class));
    check(func.args(" (0 to 5)[. = 6]", " not#1"), "", root(GFLWOR.class));

    query("function-lookup(xs:QName('fn:index-where'), <_>2</_>/text())(0, not#1)", 1);
    query("function-lookup(xs:QName('fn:index-where'), <_>2</_>/text())(1, not#1)", "");
  }

  /** Test method. */
  @Test public void inScopeNamespaces() {
    final Function func = IN_SCOPE_NAMESPACES;
    query(func.args(" <a/>") + " => map:keys()", "xml");
    query(func.args(" <a xmlns='x'/>") + " => map:keys() => sort()", "\nxml");
    query(func.args(" <a xmlns:p='x'/>") + " => map:keys() => sort()", "p\nxml");
  }

  /** Test method. */
  @Test public void intersperse() {
    final Function func = INTERSPERSE;

    query(func.args(" ()", " ()"), "");
    query(func.args(" ()", 1), "");
    query(func.args(1, " ()"), 1);
    query(func.args(" (1, 2)", " ()"), "1\n2");

    query(func.args(1, "a"), 1);
    query(func.args(1, " ('a', 'b')"), 1);
    query(func.args(" (1, 2)", "a"), "1\na\n2");
    query(func.args(" (1, 2)", " ('a', 'b')"), "1\na\nb\n2");

    check(func.args(1, "a") + " => count()", 1, root(Int.class));
    check(func.args(" 1[. = <_>1</_>]", "a"), 1, root(If.class));
    check(func.args(" (1, 2)[. = <_>3</_>]", " 'a'"), "", root(func));
    check(func.args(" (1, 2)", " 'a'[. = <_/>]"), "1\n2", root(func));
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
  @Test public void itemsAfter() {
    final Function func = ITEMS_AFTER;

    query(func.args(" ()", " boolean#1"), "");
    query(func.args(0, " boolean#1"), "");
    query(func.args(1, " boolean#1"), "");
    query(func.args(" (0, 1, 2, 3, 0)", " boolean#1"), "2\n3\n0");
    query(func.args(" 0 to 2", " not#1"), "1\n2");
    query(func.args(" 1 to 3", " function($n) { $n mod 2 = 0 }"), 3);
    query(func.args(MONTHS, " contains(?, 'Nov')"), "December");
    query(func.args(MONTHS, " starts-with(?, 'Dec')"), "");
  }

  /** Test method. */
  @Test public void itemsAt() {
    final Function func = ITEMS_AT;

    query(func.args(" ()", " ()"), "");
    query(func.args(" ()", 1), "");
    query(func.args(1, " ()"), "");
    query(func.args(1, 1), 1);
    query(func.args(1, -1), "");
    query(func.args(1, 0), "");
    query(func.args(1, 2), "");
    query(func.args(" 1 to 2", 2), 2);
    query(func.args(" 1 to 2", 3), "");
    query(func.args(" 1 to 2", 0), "");
    query(func.args(" 1 to 2", -1), "");

    query("for $i in 1 to 2 return " + func.args(" $i", 1), "1\n2");
    query(func.args(" (<a/>, <b/>)", 1), "<a/>");
    query(func.args(" (<a/>, <b/>)", 3), "");

    query(func.args(" (<a/>, <b/>)[name()]", 1), "<a/>");
    query(func.args(" (<a/>, <b/>)[name()]", 2), "<b/>");
    query(func.args(" (<a/>, <b/>)[name()]", 3), "");

    query(func.args(" <a/>", 2), "");
    query(func.args(" (<a/>, <b/>)", wrap(0)), "");
    query(func.args(" (<a/>, <b/>)", wrap(1)), "<a/>");
    query(func.args(" (<a/>, <b/>)", wrap(3)), "");
    query(func.args(" tokenize(" + wrap(1) + ")", 2), "");

    query(func.args(1, wrap(0)), "");
    query(func.args(" 1[. = 1]", wrap(1)), 1);
    query(func.args(" 1[. = 1]", wrap(2)), "");

    query("count(" + func.args(" 1 to 10", " 1 to 10") + ")", 10);
    query("count(" + func.args(" reverse(1 to 10)", " 1 to 10") + ")", 10);
    query("count(" + func.args(" 1 to 10", " reverse(1 to 10)") + ")", 10);
    query("count(" + func.args(" reverse(1 to 10)", " reverse(1 to 10)") + ")", 10);
    query("count(" + func.args(" 1 to 10", " xs:integer(<?_ 1?>) to 10") + ")", 10);

    query("count(" + func.args(" (1 to 10)[. > 0]", " 1 to 10") + ")", 10);
    query("count(" + func.args(" (1 to 10)", " (1 to 10)[. > 0]") + ")", 10);
    query("count(" + func.args(" (1 to 10)[. > 0]", " (1 to 10)[. > 0]") + ")", 10);

    query("count(" + func.args(" (1 to 10)[. > 0]", " (-100 to 100)[. < 1]") + ")", 0);
    query("count(" + func.args(" (1 to 10)[. > 0]", " (-100 to 100)[. > 10]") + ")", 0);

    query("count(" + func.args(" xs:string(<?_ x?>)", " xs:integer(<?_ 1?>)") + ")", 1);
    query(func.args(" (<a/>, <b/>)[. = '']", 2), "<b/>");
    query(func.args(" reverse((<a/>, <b/>)[. = ''])", 2), "<a/>");

    check(func.args(" (7 to 9)[. = 8]", -1), "", empty());
    check(func.args(" (7 to 9)[. = 8]", 0), "", empty());
    check(func.args(" 1[. = 1]", 1), 1, empty(func));
    check(func.args(" 1[. = 1]", 2), "", empty());

    check(func.args(" (1, 2, <_/>)", 3), "<_/>", root(CElem.class));
    check(func.args(" reverse((1, 2, <_/>))", 2), 2, empty(REVERSE));

    check(func.args(" tail((1, 2, 3, <_/>))", 2), 3, empty(TAIL));
    check(func.args(" tail((<_/>[data()], <_/>, <_/>))", 2), "", empty(TAIL), root(ITEMS_AT));

    check(func.args(" (7 to 9)[. = 8]", 1), 8, root(HEAD), type(HEAD, "xs:integer?"));

    check(func.args(" (<a/>, <b/>, <c/>)", 2), "<b/>", root(CElem.class));
    check(func.args(" (<a/>, <b/>, <c/>, <d/>)", 2), "<b/>", root(CElem.class));
    check(func.args(" (<a/>, <b/>[data()], <c/>)", 2), "<c/>", root(Otherwise.class));
    check(func.args(" (<a/>, <b/>[data()], <c/>, <d/>)", 2), "<c/>", root(Otherwise.class));
    check(func.args(" (<a/>[data()], <b/>, <c/>)", 2), "<c/>", root(ITEMS_AT));

    check(func.args(" replicate(<a/>, 5)", 0), "", empty());
    check(func.args(" replicate(<a/>, 5)", 3), "<a/>", root(CElem.class));
    check(func.args(" replicate(<a/>, 5)", 6), "", empty());

    check(func.args(" ('x', (2 to 10)[. = 5])", 2), 5, root(HEAD), empty(Str.class));
    check(func.args(" ('x', (2 to 10)[. = 5])", 3), "", empty(List.class), empty(Str.class));
    check(func.args(" ('x', (2 to 10)[. = 5], 3)", 3), 3, exists(List.class), empty(Str.class));

    check("let $seq := (1 to 10)[. > 0] return " + func.args(" $seq", " count($seq)"),
        10, root(HEAD));
    check("let $seq := (1 to 10)[. > 0] return " + func.args(" $seq", " count($seq) + 1"),
        "", empty());
    check("let $seq := (1 to 10)[. > 0] return " + func.args(" $seq", " count($seq) - 1"),
        9, root(GFLWOR.class));

    check(func.args(_PROF_VOID.args(" ()"), 0), "", empty(func));
    check(func.args(TRUNK.args(" (1, 2, 3, <_/>)"), 2), 2, empty(TRUNK));
  }

  /** Test method. */
  @Test public void itemsBefore() {
    final Function func = ITEMS_BEFORE;

    query(func.args(" ()", " boolean#1"), "");
    query(func.args(0, " boolean#1"), 0);
    query(func.args(1, " boolean#1"), "");
    query(func.args(" (0, 1, 2, 3, 0)", " boolean#1"), 0);
    query(func.args(" 1 to 3", " not#1"), "1\n2\n3");
    query(func.args(" 1 to 3", " function($n) { $n mod 2 = 0 }"), 1);
    query(func.args(MONTHS, " contains(?, 'Feb')"), "January");
    query(func.args(MONTHS, " starts-with(?, 'Jan')"), "");
  }

  /** Test method. */
  @Test public void itemsEndingWhere() {
    final Function func = ITEMS_ENDING_WHERE;

    query(func.args(" ()", " boolean#1"), "");
    query(func.args(0, " boolean#1"), 0);
    query(func.args(1, " boolean#1"), 1);
    query(func.args(" (0, 1, 2, 3, 0)", " boolean#1"), "0\n1");
    query(func.args(" 1 to 3", " not#1"), "1\n2\n3");
    query(func.args(" 1 to 3", " function($n) { $n mod 2 = 0 }"), "1\n2");
    query(func.args(MONTHS, " contains(?, '')"), "January");
    query(func.args(MONTHS, " starts-with(?, 'Feb')"), "January\nFebruary");
  }

  /** Test method. */
  @Test public void itemsStartingWhere() {
    final Function func = ITEMS_STARTING_WHERE;

    query(func.args(" ()", " boolean#1"), "");
    query(func.args(0, " boolean#1"), "");
    query(func.args(1, " boolean#1"), 1);
    query(func.args(" (0, 1, 2, 3, 0)", " boolean#1"), "1\n2\n3\n0");
    query(func.args(" 1 to 3", " not#1"), "");
    query(func.args(" 1 to 3", " function($n) { $n mod 2 = 0 }"), "2\n3");
    query(func.args(MONTHS, " contains(?, 'z')"), "");
    query(func.args(MONTHS, " starts-with(?, 'Nov')"), "November\nDecember");
  }

  /** Test method. */
  @Test public void iterateWhile() {
    final Function func = ITERATE_WHILE;
    query(func.args(1, " not#1", " ->($_) { error() }"), 1);
    error(func.args(1, " boolean#1", " ->($_) { error() }"), FUNERR1);
    query(func.args(1, " empty#1", " identity#1"), 1);
    query(func.args(" ()", " empty#1", " string#1"), "");
    query(func.args(" (21 to 24)", " function($s) { head($s) < 23 }", " tail#1"), "23\n24");
    query(func.args(" (6 to 8)", " function($s) { sum($s) > 10 }",
        " function($s) { $s ! (. - 1) }"), "2\n3\n4");
    query(func.args(" reverse(1 to 100)", " function($s) { sum($s) > 20 or head($s) > 4 }",
        " function($s) { tail($s) }"), "4\n3\n2\n1");

    query(func.args(1, " function($x) { $x < 10000 }", " function($x) { $x + 1 }"), 10000);
    query(func.args(2, " function($x) { $x < 1000 }", " function($x) { $x * $x }"), 65536);
    query(func.args(1, " function($x) { count($x) < 3 }", " function($x) { $x, $x }"),
        "1\n1\n1\n1");
    query(func.args(" (1 to 100)", " function($s) { $s[last()] - $s[1] > 1 }",
        " function($s) { subsequence($s, 2, count($s) - 2) }"),
        "50\n51");

    query(func.args(" 1e0", " -> { . instance of xs:float }",
        " -> { if(. instance of xs:double) then xs:float(.) else xs:double(.) }"),
        1);
    query(func.args(1, " -> { not(. instance of xs:byte) }",
        " -> { if(. instance of xs:short) then xs:byte(.) else xs:short(.) }"),
        1);

    query(func.args(" map { 'string': 'muckanaghederdauhaulia', 'remove': 'a' }",
        " -> { characters(?string) = ?remove }",
        " -> { map { 'string': replace(?string, ?remove, ''),"
        + "'remove': ?remove -> string-to-codepoints() -> { . + 2 } -> codepoints-to-string() } }")
        + "?string", "unhdrduhul");

    query("let $s := (1 to 1000) return " + func.args(1, " -> { . = $s }", " -> { . + 1 }"), 1001);
    query("let $i := 3936256 return " + func.args(" $i", " -> { abs(. * . - $i) >= 0.0000000001 }",
        " -> { (. + $i div .) div 2 }"), 1984);
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
    check(func.args(" reverse( (1 to 6)[. > 3] )"), 4, empty(REVERSE));

    error(func.args(" replicate(<_/>, 2)"), FUNCCAST_X_X);
    error(func.args(" xs:QName('x')"), CMPTYPE_X_X_X);
    error(func.args(" (1, 'x')"), CMPTYPES_X_X_X_X);
    error(func.args(" (xs:gYear('9998'), xs:gYear('9999'))"), CMPTYPE_X_X_X);
    error(func.args(" true#0"), FIATOMIZE_X);
  }

  /** Test method. */
  @Test public void op() {
    final Function func = OP;

    query(func.args("+") + " => count()", 1);
    query(func.args("+") + " instance of function(item()*, item()*) as xs:anyAtomicType?", true);
    query(func.args("+") + "(1, 2)", 3);
    query(func.args(wrap("+")) + "(1, 2)", 3);

    query(func.args(",") + "(<a>1</a>, <b>1</b>)", "<a>1</a>\n<b>1</b>");
    query(func.args("and") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("or") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("+") + "(<a>1</a>, <b>1</b>)", 2);
    query(func.args("-") + "(<a>1</a>, <b>1</b>)", 0);
    query(func.args("*") + "(<a>1</a>, <b>1</b>)", 1);
    query(func.args("div") + "(<a>1</a>, <b>1</b>)", 1);
    query(func.args("idiv") + "(<a>1</a>, <b>1</b>)", 1);
    query(func.args("mod") + "(<a>1</a>, <b>1</b>)", 0);
    query(func.args("=") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("<") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("<=") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args(">") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args(">=") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("!=") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("eq") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("lt") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("le") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("gt") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("ge") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args("ne") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("<<") + "(<a>1</a>, <b>1</b>)", true);
    query(func.args(">>") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("is") + "(<a>1</a>, <b>1</b>)", false);
    query(func.args("||") + "(<a>1</a>, <b>1</b>)", "11");
    query(func.args("|") + "(<a>1</a>, <b>1</b>)", "<a>1</a>\n<b>1</b>");
    query(func.args("union") + "(<a>1</a>, <b>1</b>)", "<a>1</a>\n<b>1</b>");
    query(func.args("except") + "(<a>1</a>, <b>1</b>)", "<a>1</a>");
    query(func.args("intersect") + "(<a>1</a>, <b>1</b>)", "");
    query(func.args("to") + "(<a>1</a>, <b>1</b>)", 1);
    query(func.args("otherwise") + "(<a>1</a>, <b>1</b>)", "<a>1</a>");

    error(func.args("xyz"), UNKNOWNOP_X);
  }

  /** Test method. */
  @Test public void replace() {
    final Function func = REPLACE;

    query(func.args("a", "a", " ()"), "");

    query(func.args("b", "b", " ()", " ()", " function($k, $g) { }"), "");
    query(func.args("c", "c", " ()", " ()", " function($k, $g) { upper-case($k) }"), "C");
    query(func.args("de", ".", " ()", " ()", " function($k, $g) { $k || $k }"), "ddee");

    query(func.args("Chapter 9", "[0-9]+", " ()", " ()",
        " function($k, $g) { string(number($k) + 1) }"), "Chapter 10");
    query("let $map := map { 'LAX': 'Los Angeles', 'LHR': 'London' } return"
        + func.args("LHR to LAX", "[A-Z]{3}", " ()", " ()",
        " function($s, $g) { $map($s) }"), "London to Los Angeles");
    query(func.args("57°43′30″", "([0-9]+)°([0-9]+)′([0-9]+)″", " ()", " ()", " function($s, $g) "
        + "{ string(number($g[1]) + number($g[2]) div 60 + number($g[3]) div 3600) || '°' }"),
        "57.725°");
    query(func.args("A1 B234", "([A-Z]+)([0-9]+)", " ()", " ()",
        " function($s, $g) { string-join(characters($g[2]) ! ($g[1] || .)) }"),
        "A1 B2B3B4");
    query(func.args("A(0)B(1)C(0)D(9)", "(.)\\((\\d)\\)", " ()", " ()",
        " function($s, $g) { $g[1][$g[2] != '0'] }"),
        "BD");
    query(func.args("chop first character ", "(.).*? ", " ()", " ()", " substring-after#2"),
        "hop irst haracter ");
    query(func.args("12345678", ".(.)", " ()", " ()", " replace(?, ?, '')"),
        1357);
    query("for $function in (head#1, tail#1) return" +
        func.args("1234", "(.)(.)", " ()", " ()", " function($s, $g) { $function($g) }"),
        "13\n24");
    query("for $function in (substring-before#2, substring-after#2) return" +
        func.args("1234", ".(..)", " ()", " ()", " $function"),
        "14\n4");
    query("for $before in (true(), false()) "
        + "let $name := 'fn:substring-' || (if($before) then 'before' else 'after') "
        + "let $function := function-lookup(xs:QName($name), 2) "
        + "return" + func.args("1234", ".(..)", " ()", " ()", " $function"),
        "14\n4");

    error(func.args("W", ".*", " ()", " ()", " function($k, $g) { }"), REGEMPTY_X);
    error(func.args("X", ".", " ()", " ()", " contains#2"), INVCONVERT_X_X_X);
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
  @Test public void slice() {
    final Function func = SLICE;

    String in = "() =>";
    check(in + func.args(+0), "", empty());
    check(in + func.args(+1, +2, +3), "", empty());
    check(in + func.args(-1, -2, -3), "", empty());
    check(in + func.args(+0, +0, +0), "", empty());

    in = "'a' =>";
    check(in + func.args(0), "a", root(Str.class));
    check(in + func.args(0, 0), "a", root(Str.class));
    check(in + func.args(0, 0, 0), "a", root(Str.class));

    in = "('a', 'b', 'c', 'd', 'e', 'f', 'g') =>";
    query(in + func.args(+2, +4), "b\nc\nd");
    query(in + func.args(+2, +4), "b\nc\nd");
    query(in + func.args(+2), "b\nc\nd\ne\nf\ng");
    query(in + func.args(" ()", +2), "a\nb");
    query(in + func.args(+3, +3), "c");
    query(in + func.args(+4, +3), "d\nc");
    query(in + func.args(+2, +5, +2), "b\nd");
    query(in + func.args(+5, +2, -2), "e\nc");
    check(in + func.args(+2, +5, -2), "", empty());
    check(in + func.args(+5, +2, +2), "", empty());
    query(in + func.args(), "a\nb\nc\nd\ne\nf\ng");
    query(in + func.args(-1), "g");
    query(in + func.args(-3), "e\nf\ng");
    query(in + func.args(" ()", -2), "a\nb\nc\nd\ne\nf");
    query(in + func.args(+2, -2), "b\nc\nd\ne\nf");
    query(in + func.args(-2, +2), "f\ne\nd\nc\nb");
    query(in + func.args(-4, -2), "d\ne\nf");
    query(in + func.args(-2, -4), "f\ne\nd");
    query(in + func.args(-4, -2, +2), "d\nf");
    query(in + func.args(-2, -4, -2), "f\nd");

    in = "('a', 'b', 'c', 'd', 'e', 'f', 'g')[. < 'c'] =>";
    query(in + func.args(+0), "a\nb");
    query(in + func.args(+1), "a\nb");
    query(in + func.args(-1), "b");
    query(in + func.args(+1, +2), "a\nb");
    query(in + func.args(-1, +2), "b");
    query(in + func.args(+1, -2), "a");
    query(in + func.args(-1, -2), "b\na");
    query(in + func.args(+1, +2, +3), "a");
    query(in + func.args(+1, +2, -3), "");
    query(in + func.args(+1, -2, +3), "a");
    query(in + func.args(+1, -2, -3), "a");
    query(in + func.args(-1, +2, +3), "b");
    query(in + func.args(-1, +2, -3), "b");
    query(in + func.args(-1, -2, +3), "");
    query(in + func.args(-1, -2, -3), "b");

    in = "('a', 'b', 'c', 'd', 'e', 'f', 'g')[. < 'b'] =>";
    query(in + func.args(+0), "a");
    query(in + func.args(+1), "a");
    query(in + func.args(-1), "a");
    query(in + func.args(+1, +2), "a");
    query(in + func.args(-1, +2), "a");
    query(in + func.args(+1, -2), "a");
    query(in + func.args(-1, -2), "a");
    query(in + func.args(+1, +2, +3), "a");
    query(in + func.args(+1, +2, -3), "");
    query(in + func.args(+1, -2, +3), "");
    query(in + func.args(+1, -2, -3), "a");
    query(in + func.args(-1, +2, +3), "a");
    query(in + func.args(-1, +2, -3), "");
    query(in + func.args(-1, -2, +3), "");
    query(in + func.args(-1, -2, -3), "a");

    in = "(1 to 1000) =>";
    query(in + func.args(-1001) + " => count()", 1000);
    query(in + func.args(-1000) + " => count()", 1000);
    query(in + func.args(-999) + " => count()", 999);
    query(in + func.args(-2) + " => count()", 2);
    query(in + func.args(-1) + " => count()", 1);
    query(in + func.args(0) + " => count()", 1000);
    query(in + func.args(1) + " => count()", 1000);
    query(in + func.args(2) + " => count()", 999);
    query(in + func.args(999) + " => count()", 2);
    query(in + func.args(1000) + " => count()", 1);
    query(in + func.args(1001) + " => count()", 1);

    query(in + func.args(wrap(1000)), 1000);
    query(in + func.args(1000, wrap(1000)), 1000);
    query(in + func.args(1000, 1000, wrap(1)), 1000);

    check(func.args(" (" + wrap("1") + " + 1, 3)", 1, 2), "2\n3", root(List.class));
    check(func.args(" (" + wrap("1") + " + 1, 3)", 1, 2), "2\n3", root(List.class));
    check(func.args(" replicate(" + wrap("1") + " + 1, 3)", 2), "2\n2", root(REPLICATE));

    check(func.args(" doc('" + DOC + "')//*", 1) + " => " + _PROF_VOID.args(),
        "", empty(func));
    check(func.args(" doc('" + DOC + "')//*", 2) + " => " + _PROF_VOID.args(),
        "", empty(func), exists(TAIL));
    check(func.args(" doc('" + DOC + "')//*", 9) + " => " + _PROF_VOID.args(),
        "", empty(func), exists(_UTIL_RANGE));
    check(func.args(" doc('" + DOC + "')//*", 10) + " => " + _PROF_VOID.args(),
        "", empty(func), exists(FOOT));
    check(func.args(" doc('" + DOC + "')//*", 11) + " => " + _PROF_VOID.args(),
        "", empty(func), exists(FOOT));
    check(func.args(" doc('" + DOC + "')//*", 10, 9) + " => " + _PROF_VOID.args(),
        "", empty(func), exists(_UTIL_RANGE));
  }

  /** Test method. */
  @Test public void some() {
    final Function func = SOME;

    query(func.args(" (1 to 10) ! boolean(.)"), true);
    query(func.args(" reverse(1 to 10) ! boolean(.)"), true);
    query(func.args(" reverse(0 to 9) ! boolean(.)"), true);

    query(func.args(" ()", " boolean#1"), false);
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
  @Test public void trunk() {
    final Function func = TRUNK;

    // static rewrites
    query(func.args(" ()"), "");
    query(func.args("A"), "");
    query(func.args(" (1, 2)"), 1);
    query(func.args(" (1 to 3)"), "1\n2");

    // known result size
    query(func.args(wrap(1) + "+ 1"), "");
    query(func.args(" (" + wrap(1) + "+ 1, 3)"), 2);
    query(func.args(_PROF_VOID.args(" ()")), "");

    // unknown result size
    query(func.args(" 1[. = 0]"), "");
    query(func.args(" 1[. = 1]"), "");
    query(func.args(" (1 to 2)[. = 0]"), "");
    query(func.args(" (1 to 4)[. < 3]"), 1);

    // value-based iterator
    query(func.args(" tokenize(<_></_>)"), "");
    query(func.args(" tokenize(<_>X</_>)"), "");
    query(func.args(" tokenize(<_>X Y</_>)"), "X");
    query(func.args(" tokenize(<_>X Y Z</_>)"), "X\nY");

    // iterator with known result size
    check(func.args(" (<a/>, <b/>)"), "<a/>", root(CElem.class));
    check(func.args(" sort((1 to 3) ! <_>{ . }</_>)"), "<_>1</_>\n<_>2</_>", exists(func));
    check("reverse(" + func.args(" (<a/>, <b/>, <c/>))"), "<b/>\n<a/>", root(List.class));

    // nested function calls
    check(func.args(func.args(" ()")), "", empty());
    check(func.args(func.args(" (<a/>)")), "", empty());
    check(func.args(func.args(" (<a/>, <b/>)")), "", empty());
    check(func.args(func.args(" (<a/>, <b/>, <c/>)")), "<a/>", root(CElem.class));
    check(func.args(func.args(" (<a/>, <b/>, <c/>, <d/>)")), "<a/>\n<b/>", root(List.class));
    check(func.args(func.args(" (1 to 10) ! <a>{. }</a>")),
        "<a>1</a>\n<a>2</a>\n<a>3</a>\n<a>4</a>\n<a>5</a>\n<a>6</a>\n<a>7</a>\n<a>8</a>",
        root(DualMap.class));

    check(func.args(REPLICATE.args(" <a/>", 2)), "<a/>", root(CElem.class));
    check(func.args(REPLICATE.args(" <a/>", 3)), "<a/>\n<a/>", root(REPLICATE));
    check(func.args(REPLICATE.args(" <a/>[. = '']", 2)), "<a/>", root(IterFilter.class));

    check(func.args(" (<a/>, <b/>)"), "<a/>", root(CElem.class), empty(TRUNK));
    check(func.args(" (<a/>, <b/>, <c/>)"), "<a/>\n<b/>", root(List.class), empty(TRUNK));
    check(func.args(" (<a/>, 1 to 2)"), "<a/>\n1", root(List.class), empty(TRUNK));

    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 1, 1)"),
        "", empty());
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 1, 2)"),
        "<_>1</_>", root(CElem.class));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 1, 3)"),
        "<_>1</_>\n<_>2</_>", root(DualMap.class));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 2, 3)"),
        "<_>2</_>\n<_>3</_>", root(DualMap.class));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 4, 2)"),
        "<_>4</_>", root(CElem.class));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 5, 1)"),
        "", empty());
  }

  /** Test method. */
  @Test public void arrayFoot() {
    final Function func = _ARRAY_FOOT;
    query(func.args(" [ 1 ]"), 1);
    query(func.args(" array { 1 to 5 }"), 5);
    query(func.args(" [ 1, 2 to 3 ]"), "2\n3");

    error(func.args(" [ ]"), ARRAYEMPTY);
  }

  /** Test method. */
  @Test public void arrayPartition() {
    final Function func = _ARRAY_PARITION;

    String fn = " function($seq, $curr) { true() }";
    query(func.args(" ()", fn), "");
    query(func.args(1, fn), "[1]");
    query(func.args(" (1 to 1000)", fn) + " => count()", 1000);
    query(func.args(" (1 to 1000)[. < 3]", fn) + " => count()", 2);
    query(func.args(" (1 to 1000)[. < 2]", fn) + " => count()", 1);
    query(func.args(" (1 to 1000)[. < 1]", fn) + " => count()", 0);

    fn = " function($seq, $curr) { false() }";
    query(func.args(" ()", fn), "");
    query(func.args(1, fn), "[1]");
    query(func.args(" (1 to 1000)", fn) + " => count()", 1);
    query(func.args(" (1 to 1000)[. < 3]", fn) + " => count()", 1);
    query(func.args(" (1 to 1000)[. < 2]", fn) + " => count()", 1);
    query(func.args(" (1 to 1000)[. < 1]", fn) + " => count()", 0);

    fn = " function($seq, $curr) { not($seq = $curr) }";
    query(func.args(" (1, 1)", fn), "[(1,1)]");
    query(func.args(" (1, 1, 2, 1)", fn), "[(1,1)]\n[2]\n[1]");

    fn = " function($seq, $curr) { $curr > $seq }";
    query(func.args(" (846, 23, 5, 8, 6, 1000)", fn), "[(846,23,5)]\n[(8,6)]\n[1000]");

    query(func.args(" ('Anita', 'Anne', 'Barbara', 'Catherine', 'Christine')",
        " function($x, $y) { substring($x[last()], 1, 1) ne substring($y, 1, 1) }"),
        "[(\"Anita\",\"Anne\")]\n[\"Barbara\"]\n[(\"Catherine\",\"Christine\")]");
    query(func.args(" (1, 2, 3, 4, 5, 6)", " function($a, $b){ count($a) eq 2 }"),
        "[(1,2)]\n[(3,4)]\n[(5,6)]");
    query(func.args(" (1, 4, 6, 3, 1, 1)", " function($a, $b) { sum($a) ge 5 }"),
        "[(1,4)]\n[6]\n[(3,1,1)]");
    query(func.args(" tokenize('In the beginning was the word')",
        " function($a, $b) { sum(($a, $b) ! string-length()) gt 10 }"),
        "[(\"In\",\"the\")]\n[\"beginning\"]\n[(\"was\",\"the\",\"word\")]");
    query(func.args(" (1, 2, 3, 6, 7, 9, 10)",
        " function($seq, $new) { not($new = $seq[last()] + 1) }"),
        "[(1,2,3)]\n[(6,7)]\n[(9,10)]");
  }

  /** Test method. */
  @Test public void arraySlice() {
    final Function func = _ARRAY_SLICE;

    String in = "array { } =>";
    query(in + func.args(+0), "[]");
    query(in + func.args(+1, +2, +3), "[]");
    query(in + func.args(-1, -2, -3), "[]");
    query(in + func.args(+0, +0, +0), "[]");

    in = "array { 'a' } =>";
    query(in + func.args(0), "[\"a\"]");
    query(in + func.args(0, 0), "[\"a\"]");
    query(in + func.args(0, 0, 0), "[\"a\"]");

    in = "array { 'a', 'b', 'c', 'd', 'e', 'f', 'g' } =>";
    query(in + func.args(+2, +4), "[\"b\",\"c\",\"d\"]");
    query(in + func.args(+2, +4), "[\"b\",\"c\",\"d\"]");
    query(in + func.args(+2), "[\"b\",\"c\",\"d\",\"e\",\"f\",\"g\"]");
    query(in + func.args(" ()", +2), "[\"a\",\"b\"]");
    query(in + func.args(+3, +3), "[\"c\"]");
    query(in + func.args(+4, +3), "[\"d\",\"c\"]");
    query(in + func.args(+2, +5, +2), "[\"b\",\"d\"]");
    query(in + func.args(+5, +2, -2), "[\"e\",\"c\"]");
    query(in + func.args(+2, +5, -2), "[]");
    query(in + func.args(+5, +2, +2), "[]");
    query(in + func.args(), "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\"]");
    query(in + func.args(-1), "[\"g\"]");
    query(in + func.args(-3), "[\"e\",\"f\",\"g\"]");
    query(in + func.args(" ()", -2), "[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\"]");
    query(in + func.args(+2, -2), "[\"b\",\"c\",\"d\",\"e\",\"f\"]");
    query(in + func.args(-2, +2), "[\"f\",\"e\",\"d\",\"c\",\"b\"]");
    query(in + func.args(-4, -2), "[\"d\",\"e\",\"f\"]");
    query(in + func.args(-2, -4), "[\"f\",\"e\",\"d\"]");
    query(in + func.args(-4, -2, +2), "[\"d\",\"f\"]");
    query(in + func.args(-2, -4, -2), "[\"f\",\"d\"]");

    in = "array { 'a', 'b' } =>";
    query(in + func.args(+0), "[\"a\",\"b\"]");
    query(in + func.args(+1), "[\"a\",\"b\"]");
    query(in + func.args(-1), "[\"b\"]");
    query(in + func.args(+1, +2), "[\"a\",\"b\"]");
    query(in + func.args(-1, +2), "[\"b\"]");
    query(in + func.args(+1, -2), "[\"a\"]");
    query(in + func.args(-1, -2), "[\"b\",\"a\"]");
    query(in + func.args(+1, +2, +3), "[\"a\"]");
    query(in + func.args(+1, +2, -3), "[]");
    query(in + func.args(+1, -2, +3), "[\"a\"]");
    query(in + func.args(+1, -2, -3), "[\"a\"]");
    query(in + func.args(-1, +2, +3), "[\"b\"]");
    query(in + func.args(-1, +2, -3), "[\"b\"]");
    query(in + func.args(-1, -2, +3), "[]");
    query(in + func.args(-1, -2, -3), "[\"b\"]");

    in = "array { 'a' } =>";
    query(in + func.args(+0), "[\"a\"]");
    query(in + func.args(+1), "[\"a\"]");
    query(in + func.args(-1), "[\"a\"]");
    query(in + func.args(+1, +2), "[\"a\"]");
    query(in + func.args(-1, +2), "[\"a\"]");
    query(in + func.args(+1, -2), "[\"a\"]");
    query(in + func.args(-1, -2), "[\"a\"]");
    query(in + func.args(+1, +2, +3), "[\"a\"]");
    query(in + func.args(+1, +2, -3), "[]");
    query(in + func.args(+1, -2, +3), "[]");
    query(in + func.args(+1, -2, -3), "[\"a\"]");
    query(in + func.args(-1, +2, +3), "[\"a\"]");
    query(in + func.args(-1, +2, -3), "[]");
    query(in + func.args(-1, -2, +3), "[]");
    query(in + func.args(-1, -2, -3), "[\"a\"]");

    in = "array { 1 to 1000 } =>";
    query(in + func.args(-1001) + " => array:size()", 1000);
    query(in + func.args(-1000) + " => array:size()", 1000);
    query(in + func.args(-999) + " => array:size()", 999);
    query(in + func.args(-2) + " => array:size()", 2);
    query(in + func.args(-1) + " => array:size()", 1);
    query(in + func.args(0) + " => array:size()", 1000);
    query(in + func.args(1) + " => array:size()", 1000);
    query(in + func.args(2) + " => array:size()", 999);
    query(in + func.args(999) + " => array:size()", 2);
    query(in + func.args(1000) + " => array:size()", 1);
    query(in + func.args(1001) + " => array:size()", 1);

    query(in + func.args(" array { " + wrap(1000) + " }"), "[1000]");
    query(in + func.args(" array { " + 1000 + " }", wrap(1000)), "[1000]");
    query(in + func.args(" array { " + 1000 + " }", 1000, wrap(1)), "[1000]");
  }

  /** Test method. */
  @Test public void arrayTrunk() {
    final Function func = _ARRAY_TRUNK;
    query(func.args(" [ 1 ]"), "[]");
    query(func.args(" array { 1 to 5 }"), "[1,2,3,4]");
    query(func.args(" [ 1, 2 to 3 ]"), "[1]");

    error(func.args(" [ ]"), ARRAYEMPTY);
  }

  /** Test method. */
  @Test public void mapBuild() {
    final Function func = _MAP_BUILD;

    query(func.args(" ()", " boolean#1"), "map{}");
    query(func.args(" 0", " boolean#1"), "map{false():0}");
    query(func.args(" 1", " boolean#1"), "map{true():1}");
    query(func.args(" (0, 1)", " boolean#1") + " => map:size()", 2);
    query(func.args(" (0, 1)", " function($i) { boolean($i)[.] }"), "map{true():1}");

    query(func.args(" (1 to 100)", " function($i) { }"), "map{}");
    query(func.args(" (1 to 100)", " boolean#1") + " => map:size()", 1);
    query(func.args(" (1 to 100)", " string#1") + " => map:size()", 100);
    query(func.args(" (1 to 100)", " function($i) { $i mod 10 }") + " => map:size()", 10);

    query(func.args(MONTHS, " string-length#1") + " => map:size()", 7);
    query(func.args(" (1 to 100)", " function($i) { $i mod 10 }") + " => map:size()", 10);
    query(func.args(" <xml>{ (1 to 9) ! <sub>{ . }</sub> }</xml>/*", " string-length#1")
        + " => map:keys()", 1);
    query("for $f in (true#0, false#0, concat#2, substring#2, contains#2, identity#1)"
        + "[function-arity(.) = 1] return " + func.args(5, " $f"), "map{5:5}");
    query("for $f in (1, 2, 3, 4, string#1, 6)"
        + "[. instance of function(*)] return " + func.args(8, " $f"), "map{\"8\":8}");
  }
}
