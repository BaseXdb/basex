package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests standard functions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnModuleTest extends QueryPlanTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";
  /** Document. */
  private static final String DOC = "src/test/resources/input.xml";
  /** Months. */
  private static final String MONTHS = " ('January', 'February', 'March', 'April', 'May', "
      + "'June', 'July', 'August', 'September', 'October', 'November', 'December')";

  /** Resets optimizations. */
  @AfterEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Test method. */
  @Test public void abs() {
    final Function func = ABS;

    check(func.args(" ()"), "", empty());
    check("for $i in (1 to 2)[. != 0] return " + func.args(" $i"),
        "1\n2", type(func, "xs:integer"));
    check("for $i in (1, 2.0)[. != 0] return " + func.args(" $i"),
        "1\n2", type(func, "xs:decimal"));
    check(func.args(wrap(1)), 1, type(func, "xs:double"));

    check("for $i in ([], [1])[. != 0] return " + func.args(" $i"),
        1, type(func, "xs:numeric?"));
    check("for $i in (1, <a>2</a>) return " + func.args(" $i"), "1\n2", type(func, "xs:numeric?"));

    // pre-evaluate empty sequence
    check(func.args(" ()"), "", empty(func));
    // pre-evaluate argument
    check(func.args(1), 1, empty(func));

    // function is replaced by its argument (argument yields no result)
    check(func.args(_PROF_VOID.args(" ()")), "", empty(func));
    // check adjusted type
    check(func.args(wrap(1)), 1, type(func, "xs:double"));
    check(func.args(wrap(1) + "! array { . }"), 1, type(func, "xs:double"));
  }

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
  @Test public void analyzeString() {
    final Function func = ANALYZE_STRING;
    query(func.args("a", "", "j") + "//fn:non-match/text()", "a");
    error(func.args("a", ""), REGEMPTY_X);
  }

  /** Test method. */
  @Test public void apply() {
    final Function func = APPLY;

    query(func.args(" true#0", " []"), true);
    query(func.args(" count#1", " [(1, 2, 3)]"));
    query(func.args(" string-join#1", " [ reverse(1 to 5) ! string() ]"), 54321);
    query("let $func := function($a, $b, $c) { $a + $b + $c } "
        + "let $args := [ 1, 2, 3 ] "
        + "return " + func.args(" $func", " $args"), 6);
    query("for $a in 2 to 3 "
        + "let $f := function-lookup(xs:QName('fn:concat'), $a) "
        + "return " + func.args(" $f", " array { 1 to $a }"), "12\n123");
    error(func.args(" false#0", " ['x']"), APPLY_X_X);
    error(func.args(" string-length#1", " [ ('a', 'b') ]"), INVPROMOTE_X_X_X);

    // no pre-evaluation (higher-order arguments), but type adjustment
    inline(true);
    check(func.args(" true#0", " []"), true, type(func, "xs:boolean"));
    check(func.args(" count#1", " [1]"), 1, type(func, "xs:integer"));
    check(func.args(" abs#1", " [1]"), 1, type(func, "xs:integer"));
    check(func.args(" reverse#1", " [()]"), "", type(func, "empty-sequence()"));
    check("(true#0, 1)[. instance of function(*)] ! " + func.args(" .", " []"), true,
        type(func, "item()*"));

    // code coverage tests
    query("string-length(" + func.args(" reverse#1", " ['a']") + ")", 1);
    error(func.args(" true#0", " [1]"), APPLY_X_X);
    error(func.args(" put#2", " [<_/>, '']"), FUNCUP_X);
  }

  /** Test method. */
  @Test public void avg() {
    final Function func = AVG;

    check(func.args(" ()"), "", empty());
    check(func.args(_PROF_VOID.args("X")), "", empty(func));

    check(func.args(" 1"), 1, empty(func));
    check(func.args(" 1.0"), 1, empty(func));
    check(func.args(" 1e0"), 1, empty(func));
    check(func.args(" xs:float('1')"), 1, empty(func));
    check(func.args(" (<a>1</a>, <a>3</a>)"), 2, empty(func));

    check(func.args(" (1, 2)[. = 1]"), 1, type(func, "xs:decimal?"));
    check(func.args(" (1.0, 2.0)[. = 1]"), 1, type(func, "xs:decimal?"));
    check(func.args(" (1e0, 2e0)[. = 1]"), 1, type(func, "xs:double?"));
    check(func.args(" (xs:float('1'), xs:float('2'))[. = 1]"), 1, type(func, "xs:float?"));

    check(func.args(" (1, (3, 4)[. = 5])"), 1, type(func, "xs:decimal"));
    check(func.args(" (1, (3.0, 4.0)[. = 5])"), 1, type(func, "xs:decimal"));

    check(func.args(" (1 to 3)"), 2, empty(func));
    check(func.args(" reverse(1 to 3)"), 2, empty(func));
    check(func.args(" (1 to " + wrap(3) + ")"), 2, type(func, "xs:decimal?"));
    check(func.args(" (1 to " + wrap(0) + ")"), "", type(func, "xs:decimal?"));
    check(func.args(" (1 to 999999)"), 500000, empty(func));
    check(func.args(" (1 to 999999) ! 1"), 1, empty(func));

    check(func.args(" (1 to 3) ! 1"), 1, empty(func));
    check(func.args(" (1 to 3) ! xs:untypedAtomic(1)"), 1, empty(func));

    check(func.args(REPLICATE.args(1.0, 3)), 1, empty(func));
    check(func.args(REPLICATE.args(wrap(1), 3)), 1, type(func, "xs:double"));

    error(func.args(" true#0"), FIATOMIZE_X);
    error(func.args(REPLICATE.args(" true#0", 2)), FIATOMIZE_X);
    error(func.args(" (1 to 999999) ! true#0"), FIATOMIZE_X);
  }

  /** Test method. */
  @Test public void bool() {
    final Function func = BOOLEAN;

    // pre-evaluated expressions
    check(func.args(1), true, empty(func));
    check(func.args(" ()"), false, empty(func));

    // function is replaced with fn:exists
    check(func.args(" <a>A</a>/text()"), true, exists(EXISTS));
    // function is replaced by its argument (argument yields no result)
    check("(false(), true())[" + func.args(" .") + "]", true, empty(func));
    // no replacement
    check("(false(), 1)[" + func.args(" .") + "]", 1, exists(func));

    // optimize ebv
    check("([], [])[. instance of xs:int][" + func.args(" .") + "]", "", empty(EXISTS));
  }

  /** Test method. */
  @Test public void charr() {
    final Function func = CHAR;

    // test pre-evaluation
    query(func.args("\\t") + " => string-to-codepoints()", 9);
    query(func.args("\\r") + " => string-to-codepoints()", 13);
    query(func.args("\\n") + " => string-to-codepoints()", 10);
    query(func.args("\\n"), "\n");
    query(func.args("#10"), "\n");
    query(func.args("#xa"), "\n");
    query(func.args("#xA"), "\n");
    query(func.args("#x0A"), "\n");
    query(func.args("#00000000000000000000010"), "\n");
    query(func.args("#x0000000000000000000000A"), "\n");
    query(func.args("#32"), " ");
    query(func.args("#x20"), " ");

    query(func.args("ring"), "\u02DA");
    query(func.args("AMP"), "&");
    query(func.args("amp"), "&");
    query(func.args("Tab"), "\t");
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
  @Test public void codepointsToString() {
    final Function func = CODEPOINTS_TO_STRING;
    check(func.args(" string-to-codepoints(" + wrap("ABC") + ')'), "ABC", root(STRING));
  }

  /** Test method. */
  @Test public void contains() {
    final Function func = CONTAINS;

    // pre-evaluate equal arguments and empty substring
    check(func.args(wrap("abc"), wrap("abc")), true, root(Bln.class));
    check(func.args(wrap("abc"), ""), true, root(Bln.class));
    check(func.args(wrap("abc"), " ()"), true, root(Bln.class));

    // do not optimize if argument may be of wrong type
    check(func.args(" (1, 'a')[. instance of xs:string]", "a"), true, root(CONTAINS));
  }

  /** Test method. */
  @Test public void count() {
    final Function func = COUNT;

    query(func.args(" (1 to 100000000) ! string()"), 100000000);
    query(func.args(" for $i in 1 to 100000000 return string('x')"), 100000000);

    query(func.args(" count(array { <a/>, <b/> }) "), 1);
    query(func.args(" count([ <a/>, <b/> ]) "), 1);
  }

  /** Test method. */
  @Test public void deepEqual() {
    final Function func = DEEP_EQUAL;

    query("let $a := reverse((<a/>, <b/>)) return " + func.args(" $a/.", " $a/."), true);
    query("deep-equal(1 to 1000000000, 1 to 1000000000)", true);
    query("deep-equal(1 to 1000000000, 1 to 1000000001)", false);
  }

  /** Test method. */
  @Test public void distinctValues() {
    final Function func = DISTINCT_VALUES;

    query(func.args(" (1 to 100000000) ! 'a'"), "a");
    query("count(" + func.args(" 1 to 100000000") + ')', 100000000);
    check(func.args(_PROF_VOID.args(1)), "", root(_PROF_VOID));
    check("(1, 3) ! " + func.args(" ."), "1\n3", root(IntSeq.class));

    // remove duplicate expressions
    check(func.args(" (1, <_/>, 1)"), "1\n", count(Int.class, 1));
    check(func.args(" (1, 1)[. = 1]"), 1, root(Int.class));
    check(func.args(" (" + wrap(1) + "," + wrap(1) + ")[. = 1]"), 1,
        empty(List.class), empty(REPLICATE));
    // remove reverse function call
    check(func.args(" reverse((<a>A</a>, <b>A</b>))"), "A", empty(REVERSE));
    check(func.args(" reverse((<a>A</a>, <b>A</b>)[data()])"), "A", empty(REVERSE));
    check(func.args(" reverse((<a>A</a>, <b>A</b>))[data()]"), "A", empty(REVERSE));
    // remove sort function call
    check(func.args(" sort((<a>A</a>, <b>A</b>))"), "A", empty(SORT));
    // swap distinct-values and sort
    check(func.args(" sort((<a>A</a>, <b>A</b>)[data()])"),
        "A", exists(SORT.className() + "/" + DISTINCT_VALUES.className()));
    check(func.args(" sort((string(<_>X</_>), 1), (), string#1)"),
        "1\nX", empty(DISTINCT_VALUES));

    // single value: replace with data
    check(func.args(wrap("A")), "A", root(DATA));
    check("(<a/>, <b/>) ! " + func.args(" ."), "\n", root(DATA));
    check("(1 to 2) ! " + func.args(" ."), "1\n2", root(RangeSeq.class));
  }

  /** Test method. */
  @Test public void docAvailable() {
    final Function func = DOC_AVAILABLE;

    query(func.args(TEXT), true);
    query(func.args("/"), false);
    query(func.args("/a/b/c/d/e"), false);
  }

  /** Test method. */
  @Test public void error() {
    final Function func = ERROR;

    // pre-evaluate empty sequence
    error(func.args(), FUNERR1);
    error(func.args(" ()"), FUNERR1);
    query("(1, " + func.args() + ")[1]", 1);

    // errors: defer error if not requested; adjust declared sequence type of {@link TypeCheck}
    query("head((1, " + func.args() + "))", 1);
    query("head((1, function() { error() }()))", 1);

    inline(true);
    query("declare function local:e() { error() }; head((1, local:e()))", 1);
    query("declare function local:e() as empty-sequence() { error() }; head((1, local:e()))", 1);
    query("declare %basex:inline(0) function local:f() { error() }; head((1, local:f()))", 1);
  }

  /** Test method. */
  @Test public void filter() {
    final Function func = FILTER;
    query(func.args(" (0, 1)", " boolean#1"), 1);

    check(func.args(" ()", " boolean#1"), "", empty());
    check(func.args(" 1 to 9", " function($_) { $_ = 0 }"), "", exists(IterFilter.class));
    check(func.args(" ('a', <a/>)", " function($_ as xs:string) { $_ = 'a' }"), "a",
        exists(IterFilter.class));
    check(func.args(" ('a', <a/>)", " function($_ as xs:string) as xs:boolean? { $_ = 'a' }"), "a",
        exists(IterFilter.class));

    inline(true);
    check(func.args(" (<a/>, <b/>)", " boolean#1"), "<a/>\n<b/>", root(List.class));
    check(func.args(" <a/>", " boolean#1"), "<a/>", root(CElem.class));
  }

  /** Test method. */
  @Test public void foldLeft() {
    final Function func = FOLD_LEFT;

    query("(1, 'a')[. instance of xs:integer] ! " +
        func.args(" .", 0, " function($a, $b) { $b }"), 1);
    query("(1, function($a, $b) { $b })[. instance of function(*)] ! " +
        func.args("A", 1, " ."), "A");

    query(func.args(" ()", 1, " function($a, $b) { $b }"), 1);
    query(func.args(_PROF_VOID.args(1), 1, " function($a, $b) { $b }"), 1);
    query(func.args(2, 1, " function($a, $b) { $b }"), 2);
    query("sort(" + func.args(" <a/>", "a", " compare#2") + ")", 1);

    check(func.args(" ()", " ()", " function($a, $b) { $b }"), "", empty());

    check(func.args(" <a>1</a>", " xs:byte(1)", " function($n, $_) {" +
        " if($n instance of xs:byte ) then xs:short  (1) else" +
        " if($n instance of xs:short) then xs:int    (1) else" +
        " if($n instance of xs:int  ) then xs:long   (1) else" +
        " if($n instance of xs:long ) then xs:integer(1) else" +
        " xs:decimal(1)" +
        "}"), 1,
        type(func, "xs:decimal"));

    // type inference
    inline(true);
    check(func.args(" (1, 2)[. = 1]", " ()", " function($r, $a) { $r, $a }"), 1,
        type(func, "xs:integer*"));
    check(func.args(" (1, 2)[. = 0]", 1, " function($r, $a) { $r, $a }"), 1,
        type(func, "xs:integer+"));
    check(func.args(" (1, 2)[. = 1]", "a", " function($r, $a) { $r, $a }"), "a\n1",
        type(func, "xs:anyAtomicType+"));

    check(func.args(" (1, 2)[. = 0]", 1, " function($r as xs:integer, $a) { $r + $r }"), 1,
        type(func, "xs:integer"));

    // should not be unrolled
    check(func.args(" 1 to 6", 0, " function($a, $b) { $a + $b }"), 21,
        exists(func));

    // should be unrolled and evaluated at compile time
    unroll(true);
    check(func.args(" 2 to 5", 1, " function($a, $b) { $a + $b }"), 15,
        empty(func),
        exists(Int.class));
    // should be unrolled but not evaluated at compile time
    check(func.args(" 2 to 5", 1, " function($a, $b) { $b[" + _RANDOM_DOUBLE.args() + "] }"), "",
        empty(func),
        exists(_RANDOM_DOUBLE));
  }

  /** Test method. */
  @Test public void foldRight() {
    final Function func = FOLD_RIGHT;

    check(func.args(" ()", " ()", " function($a, $b) { $a }"), "", empty());

    // should not be unrolled
    check(func.args(" 0 to 5", 10, " function($a, $b) { $a + $b }"), 25,
        exists(func));

    // should be unrolled and evaluated at compile time
    unroll(true);
    check(func.args(" 1 to 4", 10, " function($a, $b) { $a + $b }"), 20,
        empty(func),
        exists(Int.class));
    // should be unrolled but not evaluated at compile time
    check(func.args(" 1 to 4", 10, " function($a, $b) { $b[" + _RANDOM_DOUBLE.args() + "] }"), "",
        empty(func),
        exists(_RANDOM_DOUBLE));
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
  @Test public void forEach() {
    final Function func = FOR_EACH;

    query("(1, not#1)[. instance of function(*)] ! " + func.args(" 1", " ."), false);
    query("sort(" + func.args(" (1 to 2)[. > 0]", " string#1") + ')', "1\n2");
    check(func.args(" ()", " boolean#1"), "", empty());

    inline(true);
    // pre-compute result size
    query("count(" + func.args(" 1 to 10000000000", " string#1") + ')', 10000000000L);
    check("count(" + func.args(" 1 to 20", " function($a) { $a, $a }") + ')',
        40, root(Int.class));

    // rewritten to FLWOR expression
    check(func.args(" 0 to 8", " function($x) { $x + 1 }"),
        "1\n2\n3\n4\n5\n6\n7\n8\n9",
        empty(func),
        root(RangeSeq.class), exists(RangeSeq.class));
    check(func.args(" 1 to 9", " function($x) { $x[" + _RANDOM_DOUBLE.args() + "] }"), "",
        empty(func),
        exists(DualMap.class), exists(_RANDOM_DOUBLE));
    check(func.args(" 0 to 10", " function($x) { $x idiv 2 }"),
        "0\n0\n1\n1\n2\n2\n3\n3\n4\n4\n5",
        root(DualMap.class));
    check(func.args(" (1 to 2)[. = 2]", " function($a) { $a * $a }"), 4,
        type(DualMap.class, "xs:integer*"));
  }

  /** Test method. */
  @Test public void forEachPair() {
    final Function func = FOR_EACH_PAIR;

    query("(0, concat#2)[. instance of function(*)] ! " +
        func.args("A", "B", " ."), "AB");

    query("sort(" + func.args(" ('aa', 'bb')", " (2, 2)", " substring#2") + ')', "a\nb");

    // pre-compute result size
    check("count(" + func.args(" 1 to 10000000000", " 1 to 10000000000",
        " function($a, $b) { 'a' }") + ')', 10000000000L, empty(func));
    check("count(" + func.args(" 1 to 20000000000", " 1 to 10000000000",
        " function($a, $b) { 'a' }") + ')', 10000000000L, empty(func));
    check("count(" + func.args(" 1 to 10000000000", " 1 to 20000000000",
        " function($a, $b) { 'a' }") + ')', 10000000000L, empty(func));
    check("count(" + func.args(" 1 to 20", " 1 to 20", " function($a, $b) { $a, $b }") + ')', 40,
        exists(func));

    check(func.args(" ()", "a", " matches#2"), "", empty());
    check(func.args("aa", " ()", " matches#2"), "", empty());

    query(func.args("aa", "a", " matches#2"), true);
    query(func.args(" ('aa', 'bb')", "a", " matches#2"), true);
    query(func.args("aa", " ('a', 'b')", " matches#2"), true);
  }

  /** Test method. */
  @Test public void formatInteger() {
    final Function func = FORMAT_INTEGER;

    query(func.args(11, "1"), "11");
    query(func.args(11, "001"), "011");

    query(func.args(1234, "16^ffff"), "04d2");
    query(func.args(1234, "16^F"), "4D2");
    query(func.args(12345678, "16^ffff_ffff"), "00bc_614e");
    query(func.args(12345678, "16^#_ffff"), "bc_614e");
    query(func.args(255, "2^1111 1111"), "1111 1111");
    query(func.args(1023, "32^AAAA"), "00VV");
  }

  /** Test method. */
  @Test public void functionLookup() {
    final Function func = FUNCTION_LOOKUP;

    check("for $f in ('fn:true', 'fn:position') !" + func.args(" xs:QName(.)", 0) +
        " return (8, 9)[$f()]",
        "8\n9\n9", exists(func));
    check("for $f in xs:QName('fn:position') return (8, 9)[" + func.args(" $f", 0) + "()]",
        "8\n9", empty(func));

    inline(true);
    check(func.args(" xs:QName('fn:count')", 1) + "((1, 2))", 2, root(Int.class));
    check(func.args(" xs:QName('hof:id')", 1) + "(1)", 1, root(Int.class));
    check(func.args(" xs:QName('hof:id')", 1) + "(<a/>)", "<a/>", root(CElem.class));
  }

  /** Test method. */
  @Test public void generateId() {
    final Function func = GENERATE_ID;

    // GH-1633: ensure that database nodes return identical id
    query("count(distinct-values((document { <x/> } update {}) ! (*, *) ! " +
        func.args(" .") + "))", 1);
  }

  /** Test method. */
  @Test public void head() {
    final Function func = HEAD;

    // pre-evaluate empty sequence
    check(func.args(" ()"), "", empty(func));
    check(func.args(1), 1, empty(func));
    check(func.args(" (1, 2)"), 1, empty(func));
    check(func.args(" <a/>"), "<a/>", empty(func));
    check(func.args(" <a/>[name()]"), "<a/>", empty(func));
    check(func.args(" (<a/>, <b/>)[name()]"), "<a/>", exists(func));
    check(func.args(" (1, error())"), 1, exists(Int.class));
    check(func.args(" reverse((1 to " + wrap(3) + ")[. > 1])"), 3,
        "exists(//IterFilter/FnReverse)");

    check(func.args(TRUNK.args(" (<a/>, <b/>, <c/>)")), "<a/>", empty(TRUNK));
    check(func.args(TRUNK.args(" (<a/>, <b/>) ")), "<a/>", empty(TRUNK));
    check(func.args(TRUNK.args(" <a/>")), "", empty());
    check(func.args(TRUNK.args(" (1, 2)[. = 0]")), "", exists(TRUNK));

    check(func.args(" tail((<a/>, <b/>, <c/>[. = '']))"), "<b/>", root(CElem.class));
    check(func.args(" tail((<a/>, <b/>, <c/>))"), "<b/>", root(CElem.class));

    check(func.args(" subsequence((<a/>, <b/>)," + wrap(1) + ")"), "<a/>",
        exists(SUBSEQUENCE));
    check(func.args(" subsequence((<a/>, <b/>, <c/>, <d/>), 2, 2)"), "<b/>",
        root(CElem.class));
    check(func.args(_UTIL_RANGE.args(" (<a/>, <b/>, <c/>, <d/>)", 2, 3)), "<b/>",
        root(CElem.class));

    check(func.args(REPLICATE.args(" <a/>", 2)), "<a/>", root(CElem.class));
    check(func.args(REPLICATE.args(" <a/>[. = '']", 2)), "<a/>",
        root(IterFilter.class), empty(REPLICATE));

    check(func.args(REPLICATE.args(" (<a/>, <b/>)[. = '']", 2)), "<a/>",
        root(HEAD), empty(REPLICATE));
    check(func.args(REPLICATE.args(" <a/>", " <_>2</_>")), "<a/>",
        exists(REPLICATE));

    check(func.args(" (1, <a/>)"), 1, root(Int.class));
    check(func.args(" (1 to 2, <a/>)"), 1, root(Int.class));
    check(func.args(" (<a/>[. = ''], 1)"), "<a/>", root(Otherwise.class));

    check(func.args(" (<a/>[. = ''], <b/>, <c/>)"), "<a/>", root(Otherwise.class));
    check(func.args(" (<a/>[. = ''], <b/>[. = ''])"), "<a/>", root(Otherwise.class));
    check(func.args(" (<a/>[. = ''], <b/>[. = ''], <c/>[. = ''])"), "<a/>", empty(Otherwise.class));
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
  @Test public void indexOf() {
    final Function func = INDEX_OF;

    query(func.args(" 1 to 1000000", 0), "");
    query("count(" + func.args(" 1 to 1000000", 0) + ")", 0);

    query(func.args(" reverse(1 to 1000000)", 1000000), 1);
    query("count(" + func.args(" reverse(1 to 1000000)", 1000000) + ")", 1);

    query("count(" + func.args(" (1 to 1000000) ! 'x'", "x") + ")", 1000000);
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
  @Test public void innermost() {
    final Function func = INNERMOST;
    query("let $n := <li/> return " + func.args(" ($n, $n)"), "<li/>");
  }

  /** Test method. */
  @Test public void inScopeNamespaces() {
    final Function func = IN_SCOPE_NAMESPACES;
    query(func.args(" <a/>") + " => map:keys()", "xml");
    query(func.args(" <a xmlns='x'/>") + " => map:keys() => sort()", "\nxml");
    query(func.args(" <a xmlns:p='x'/>") + " => map:keys() => sort()", "p\nxml");
  }

  /** Test for namespace functions and in-scope namespaces. */
  @Test public void inScopePrefixes() {
    final Function func = IN_SCOPE_PREFIXES;
    query("sort(<e xmlns:p='u'>{" + func.args(" <e/>") + "}</e>/text()/tokenize(.))", "p\nxml");
  }

  /** Test method. */
  @Test public void insertBefore() {
    final Function func = INSERT_BEFORE;
    query(func.args(1, 1, 1), "1\n1");
    query("count(" + func.args(" ()", 2, " 1 to 100000000") + ')', 100000000);
    query("count(" + func.args(" 1 to 100000000", 3, " ()") + ')', 100000000);
    query("count(" + func.args(" 1 to 100000000", 4, " 1 to 100000000") + ')', 200000000);

    query("head(" + func.args(" 1 to 100000000", wrap(4), " 1 to 100000000") + ')', 1);
    query("subsequence(" + func.args(" 1 to 100000000", wrap(2), " 1 to 100000000") + ", 1, 3)",
        "1\n1\n2");

    query("for $p in (0 to 4)" +
        "return string-join(" +
        "  for $i in (5, 4, 3, 2, 1, 0)" +
        "  return insert-before(6 to 7, $p, 1 to 2)[$i]" +
        ")",
        "7621\n7621\n7216\n2176\n2176");
    query("for $p in (0 to 4)" +
        "return string-join(" +
        "  for $i in (5, 4, 3, 2, 1, 0)" +
        "  return insert-before((6, 5), $p, ('x', 'y', 'z'))[$i]" +
        ")",
        "56zyx\n56zyx\n5zyx6\nzyx56\nzyx56");
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
  @Test public void invisibleXml() {
    final Function func = INVISIBLE_XML;
    // unambiguous grammar
    query(func.args(
          "e: t;\n"
        + "   t, [\"+-\"], e.\n"
        + "t: f;\n"
        + "   t, [\"*/\"], f.\n"
        + "f: [\"0\"-\"9\"]+;\n"
        + "   \"(\", e, \")\".") + "('2*3+4')",
        "<e><t><t><f>2</f></t>*<f>3</f></t>+<e><t><f>4</f></t></e></e>");
    // ambiguous grammar
    query(func.args(
          "e: f;\n"
        + "   e, [\"+-*/\"], e.\n"
        + "f: [\"0\"-\"9\"]+;\n"
        + "   \"(\", e, \")\".") + "('2*3+4')",
          "<e xmlns:ixml=\"http://invisiblexml.org/NS\" ixml:state=\"ambiguous\">"
        + "<e><e><f>2</f></e>*<e><f>3</f></e></e>+<e><f>4</f></e></e>");
    // input with cr+lf
    query("string-to-codepoints("
        + func.args("s: ~[]*.")
        + "(codepoints-to-string((9,13,10,9,10,13,9,10,9,13,9))))",
        "9\n13\n10\n9\n10\n13\n9\n10\n9\n13\n9");
    // invalid grammar
    error(func.args("?%$"), IXML_GRM_X_X_X);
    // parser generation failure
    error(func.args("s: ~[#10ffff]."), IXML_GEN_X);
    // invalid input
    error("let $parser := " + func.args("s: ~[\"x\"]*.") + "\n"
        + "return $parser('x')", IXML_INP_X_X_X);
    // result processing error
    error("let $parser := " + func.args("-s: 'x'.") + "\n"
        + "return $parser('x')", IXML_RESULT_X);
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
  @Test public void jsonDoc() {
    final Function func = JSON_DOC;
    query(func.args("src/test/resources/example.json") + "('address')('state')", "NY");
    query(func.args("src/test/resources/example.json") + "?address?state", "NY");
  }

  /** Test method. */
  @Test public void jsonToXml() {
    final Function func = JSON_TO_XML;
    contains(func.args("null"), "xmlns");
    contains(func.args("null") + " update { }", "xmlns");
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
  @Test public void matches() {
    final Function func = MATCHES;
    query(func.args("a", ""), true);
    query(func.args("a", "", "j"), true);

    query(func.args("nop", 'o'), true);
    query(func.args("nöp", 'ö'), true);
    query(func.args("nop", '.'), true);

    check(func.args(wrap("nop"), 'o'), true, root(CONTAINS));
    check(func.args(wrap("nöp"), 'ö'), true, root(CONTAINS));
    check(func.args(wrap("nop"), '.'), true, root(func));

    query(func.args("nop", wrap("o")), true);
    query(func.args("nöp", wrap("ö")), true);
    query(func.args("nop", wrap(".")), true);

    query(func.args(wrap("nop"), wrap("o")), true);
    query(func.args(wrap("nöp"), wrap("ö")), true);
    query(func.args(wrap("nop"), wrap(".")), true);

    error(func.args("a", "+"), REGINVALID_X);
    error(func.args("a", "+", "j"), REGINVALID_X);
  }

  /** Test method. */
  @Test public void min() {
    final Function func = MIN;
    query(func.args(1), 1);
    query(func.args(1.1), 1.1);
    query(func.args(" 1e1"), 10);
    query(func.args(" (1, 1e1)"), 1);
    query(func.args(" (1, 1.1, 1e1)") + " instance of xs:double", true);
    query(func.args(wrap(1)) + " instance of xs:double", true);
    query(func.args(" [1]"), 1);
    query(func.args(" (7, 6, 6, 6.0, 5.0, 5.0, xs:float('5'), xs:float('4'), xs:float('4'), " +
        "4, 4e0, 3e0, 3e0, 2e0, 2, 2, 1, <x>0</x>, <x>0</x>)"), 0);
    query(func.args(" (xs:double('NaN'), xs:float('NaN'))") + " instance of xs:double", true);

    query(func.args(" (xs:anyURI('b'), xs:anyURI('a'))") +
        " ! (. = 'a' and . instance of xs:anyURI)", true);
    query(func.args(" (xs:anyURI('c'), xs:anyURI('b'), 'a')") +
        " ! (. = 'a' and . instance of xs:string)", true);
    query(func.args(" ('b', xs:anyURI('a'))") +
        " ! (. = 'a' and . instance of xs:string)", true);
    query(func.args(" (2, 3, 1)"), 1);
    query(func.args(" (xs:date('2002-01-01'), xs:date('2003-01-01'), xs:date('2001-01-01'))"),
        "2001-01-01");
    query(func.args(" (xs:dayTimeDuration('PT1S'), xs:dayTimeDuration('PT0S'))"), "PT0S");
    query(func.args(" (xs:hexBinary('42'), xs:hexBinary('43'), xs:hexBinary('41'))"), 'A');

    query("for $n in (1, 2) return " + func.args(" $n"), "1\n2");
    query("for $n in (1, 2) return " + func.args(" ($n, $n)"), "1\n2");

    query("for $s in (['a', 'b'], ['c']) return " + func.args(" ($s, $s)"), "a\nc");

    // query plan checks
    check(func.args(" ()"), "", empty());
    check(func.args(_PROF_VOID.args(123)), "", empty(func));
    check(func.args(" 123"), 123, empty(func));
    check(func.args(wrap(1)), 1, exists(func));
    check(func.args(" (0 to 99999999999) ! (1 to 10000000)"), 1, root(Int.class));

    // errors
    error(func.args(" xs:QName('a')"), COMPARE_X_X);
    error(func.args(" ('b', 'c', 'a', 1)"), ARGTYPE_X_X_X);
    error(func.args(" (2, 3, 1, 'a')"), ARGTYPE_X_X_X);
    error(func.args(" (false(), true(), false(), 1)"), ARGTYPE_X_X_X);
    error(func.args(" 'x'", 1), INVCONVERT_X_X_X);
  }

  /** Test method. */
  @Test public void namespaceUriForPrefix() {
    final Function func = NAMESPACE_URI_FOR_PREFIX;
    query("sort(<e xmlns:p='u'>{" + func.args("p", " <e/>") + "}</e>/text()/tokenize(.))", "u");
  }

  /** Test method. */
  @Test public void not() {
    final Function func = NOT;

    // pre-evaluated expressions
    check(func.args(1), false, empty(func));
    check(func.args(" ()"), true, empty(func));

    check(func.args(" empty((1, 2)[. = 1])"), true, root(Bln.class));
    check(func.args(" exists((1, 2)[. = 1])"), false, root(Bln.class));
    check(func.args(" <a/>/text()"), true, exists(EMPTY));
    // function is replaced with fn:boolean
    check(func.args(func.args(" ((1, 2)[. = 1])")), true, exists(BOOLEAN));

    // function is replaced with fn:boolean
    check("for $i in (1, 2)[. != 0] return " + func.args(" $i = $i + 1"),
        "true\ntrue", exists("*[@op != '=']"));
    check("for $i in (1, 2)[. != 0] return " + func.args(" $i eq $i + 1"),
        "true\ntrue", exists("*[@op != 'eq']"));
    check("for $i in (1, 2)[. != 0] return " + func.args(" [$i] eq $i + 1"),
        "true\ntrue", exists("*[@op != 'eq']"));
    check("for $i in (1, 2)[. != 0] return " + func.args(" $i = ($i, <_>{ $i }</_>)"),
        "false\nfalse", exists(func));
  }

  /** Test method. */
  @Test public void number() {
    final Function func = NUMBER;

    query(func.args(1), 1);
    query(func.args(" ()"), "NaN");
    query(func.args(" xs:double('NaN')"), "NaN");
    query(func.args("X"), "NaN");
    query(func.args(wrap(1)), 1);

    check("for $d in (1e0, 2e-1)[. != 0] return" + func.args(" $d"), "1\n0.2", empty(func));
    check("for $d in (1, 2.34)[. != 0] return" + func.args(" $d"), "1\n2.34", exists(func));
    check("for $d in (1e0, 2e-1)[. != 0] return $d[" + func.args() + ']', 1, empty(func));
    check("for $d in (1e0, 2e-1)[. != 0] return $d[" + func.args() + " = 1]", 1, empty(func));
    check("for $d in (1, 2.34)[. != 0] return $d[" + func.args() + ']', 1, exists(func));

    error(func.args(), NOCTX_X);
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
  @Test public void outermost() {
    final Function func = INNERMOST;
    query("let $n := <li/> return " + func.args(" ($n, $n)"), "<li/>");
  }

  /** Test method. */
  @Test public void parseIetfDate() {
    final Function func = PARSE_IETF_DATE;

    query(func.args("Wed, 06 Jun 1994 07:29:35 GMT"), "1994-06-06T07:29:35Z");
    query(func.args("Wed, 6 Jun 94 07:29:35 GMT"), "1994-06-06T07:29:35Z");
    query(func.args("Wed Jun 06 11:54:45 EST 0090"), "0090-06-06T11:54:45-05:00");
    query(func.args("Sunday, 06-Nov-94 08:49:37 GMT"), "1994-11-06T08:49:37Z");
    query(func.args("Wed, 6 Jun 94 07:29:35 +0500"), "1994-06-06T07:29:35+05:00");
    query(func.args("1 Nov 1234 05:06:07.89 gmt"), "1234-11-01T05:06:07.89Z");

    query(func.args("01-feb-3456 07:08:09 GMT"), "3456-02-01T07:08:09Z");
    query(func.args("01-FEB-3456 07:08:09 GMT"), "3456-02-01T07:08:09Z");
    query(func.args("Wed, 06 Jun 94 07:29:35 +0000 (GMT)"), "1994-06-06T07:29:35Z");
    query(func.args("Wed, 06 Jun 94 07:29:35"), "1994-06-06T07:29:35Z");

    String s = "Wed, Jan-01 07:29:35 GMT 19";
    query(func.args(s), "1919-01-01T07:29:35Z");
    for(int i = s.length(); --i >= 0;) {
      error(func.args(s.substring(0, i)), IETF_PARSE_X_X_X);
    }

    s = "Wed, 06 Jun 1994 07:29";
    query(func.args(s), "1994-06-06T07:29:00Z");
    for(int i = s.length(); --i >= 0;) {
      error(func.args(s.substring(0, i)), IETF_PARSE_X_X_X);
    }
    error(func.args(s + "X"), IETF_PARSE_X_X_X);

    error(func.args("Wed, 99 Jun 94 07:29:35 +0000 ("), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 +0000 (GT)"), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 +0000 (GMT"), IETF_PARSE_X_X_X);

    error(func.args("Wed, 99 Jun 94 07:29:35. GMT"), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 0500"), IETF_PARSE_X_X_X);
    error(func.args("Wed, 99 Jun 94 07:29:35 +0500"), IETF_INV_X);
  }

  /** Test method. */
  @Test public void parseInteger() {
    final Function func = PARSE_INTEGER;
    // successful queries
    query(func.args("100", 2), 4);
    query(func.args("1111111111111111", 2), 65535);
    query(func.args("10000000000000000", 2), 65536);
    query(func.args("4", 16), 4);
    query(func.args("ffff", 16), 65535);
    query(func.args("FFFF", 16), 65535);
    query(func.args("10000", 16), 65536);
    query(func.args("4", 10), 4);
    query(func.args("65535", 10), 65535);
    query(func.args("65536", 10), 65536);

    error(func.args("1", 1), INTRADIX_X);
    error(func.args("1", 100), INTRADIX_X);
    error(func.args("abc", 10), INTINVALID_X_X);
    error(func.args("012", 2), INTINVALID_X_X);
  }

  /** Test method. */
  @Test public void parseJson() {
    final Function func = PARSE_JSON;
    query(func.args("\"x\\u0000\""), "x\uFFFD");
  }

  /** Test method. */
  @Test public void parseXml() {
    final Function func = PARSE_XML;
    contains(func.args("<x>a</x>") + "//text()", "a");
    query(func.args("<a/>") + "/a[node()]", "");
    query(func.args("<a/>") + "/*[1]", "<a/>");
    query(func.args("<a/>") + "/*[self::a]", "<a/>");
    query(func.args("<a/>") + "/*[1][self::a]", "<a/>");
    query(func.args("<a/>") + "/*[1][not(child::node())]", "<a/>");
    query(func.args("<a/>") + "/*[1][self::a][not(child::node())]", "<a/>");
  }

  /** Test method. */
  @Test public void randomNumberGenerator() {
    final Function func = RANDOM_NUMBER_GENERATOR;

    // ensure that the same seed will generate the same result
    final String query = func.args(123) + "?number";
    assertEquals(query(query), query(query));
    // ensure that multiple number generators in a query will generate the same result
    query("let $seq := 1 to 10 "
        + "let $m1 := " + func.args() + " "
        + "let $m2 := " + func.args() + " "
        + "return every $test in ("
        + "  $m1('number') = $m2('number'), "
        + "  $m2('next')()('number') = $m1('next')()('number'), "
        + "  deep-equal($m1('permute')($seq), $m2('permute')($seq))"
        + ") satisfies true()", true);
    // ensure that the generator has no mutable state
    query("for $i in 1 to 100 "
        + "let $rng := " + func.args() + " "
        + "where $rng?next()?number ne $rng?next()?number "
        + "return error()");
  }

  /** Test method. */
  @Test public void remove() {
    final Function func = REMOVE;

    // static rewrites
    query(func.args(" ()", 1), "");
    query(func.args("A", 1), "");
    query(func.args(" (1, 2)", 1), 2);
    query(func.args(" (1 to 3)", 1), "2\n3");

    // known result size
    query(func.args(wrap(1) + "+ 1", 1), "");
    query(func.args(" (" + wrap(1) + "+ 1, 3), 1"), 3);
    query(func.args(_PROF_VOID.args(" ()"), 1), "");

    // unknown result size
    query(func.args(wrap(1) + "[. = 0]", 1), "");
    query(func.args(wrap(1) + "[. = 1]", 1), "");
    query(func.args(" (1 to 2)[. = 0]", 1), "");
    query(func.args(" (1 to 4)[. < 3]", 1), 2);

    // value-based iterator
    query(func.args(" tokenize(<_></_>)", 1), "");
    query(func.args(" tokenize(<_>X</_>)", 1), "");
    query(func.args(" tokenize(<_>X Y</_>)", 1), "Y");
    query(func.args(" tokenize(<_>X Y Z</_>)", 1), "Y\nZ");

    // static rewrites, dynamic position
    query(func.args(" ()", wrap(1)), "");
    query(func.args("A", wrap(1)), "");
    query(func.args(" (1, 2)", wrap(1)), 2);
    query(func.args(" (1 to 3)", wrap(1)), "2\n3");

    // known result size, dynamic position
    query(func.args(wrap(1) + "+ 1", wrap(1)), "");
    query(func.args(" (" + wrap(1) + "+ 1, 3), 1"), 3);
    query(func.args(_PROF_VOID.args(" ()"), wrap(1)), "");

    // unknown result size, dynamic position
    query(func.args(wrap(1) + "[. = 0]", wrap(1)), "");
    query(func.args(wrap(1) + "[. = 1]", wrap(1)), "");
    query(func.args(" (1 to 2)[. = 0]", wrap(1)), "");
    query(func.args(" (1 to 4)[. < 3]", wrap(1)), 2);

    // value-based iterator, dynamic position
    query(func.args(" tokenize(<_></_>)", wrap(1)), "");
    query(func.args(" tokenize(<_>X</_>)", wrap(1)), "");
    query(func.args(" tokenize(<_>X Y</_>)", wrap(1)), "Y");
    query(func.args(" tokenize(<_>X Y Z</_>)", wrap(1)), "Y\nZ");

    // known result size
    query(func.args(" (1, <_>2</_>, 3, 4)", 2), "1\n3\n4");
    query(func.args(" (1, <_>2</_>, 3, 4)", 2) + "[1]", 1);
    query(func.args(" (1, <_>2</_>, 3, 4)", 2) + "[2]", 3);
    query(func.args(" (1, <_>2</_>, 3, 4)", 2) + "[3]", 4);

    // multiple positions
    query(func.args(" (1, <_>2</_>, 3, 4)", " (1, 2)"), "3\n4");
    query(func.args(" (1, <_>2</_>, 3, 4)", " (2, 3)"), "1\n4");
    query(func.args(" (1, <_>2</_>, 3, 4)", " (2, 4)"), "1\n3");
    query(func.args(" (1, <_>2</_>, 3, 4)", " (2, 2)"), "1\n3\n4");
    query(func.args(" (1, <_>2</_>, 3, 4)", " (1 to 3)"), 4);
    query(func.args(" (1, <_>2</_>, 3, 4)", " (2 to 4)"), 1);
    query(func.args(" (1, <_>2</_>, 3, 4)", " (0 to 3)"), 4);
    query(func.args(" (1, <_>2</_>, 3, 4)", " (1 to 4)"), "");
    query(func.args(" (1, <_>2</_>, 3, 4)", " (1 to 5)"), "");
  }

  /** Test method. */
  @Test public void replace() {
    final Function func = REPLACE;

    query(func.args("a", "a", "b"), "b");
    query(func.args("ä", "ä", "b"), "b");
    query(func.args("a", ".", "b"), "b");

    query(func.args("a", "", "x", "j"), "xax");
    error(func.args("a", "", "x"), REGEMPTY_X);

    // GH-573
    query(func.args("aaaaa bbbbbbbb ddd ", "(.{6,15}) ", "$1@"), "aaaaa bbbbbbbb@ddd ");
    query(func.args("aaaa AAA 123", "(\\s+\\P{Ll}{3,280}?)", "$1@"), "aaaa AAA@ 123@");
    error(func.args("asdf", "a{12, 3}", ""), REGINVALID_X);

    // GH-1940
    query(func.args("hello", "hel(?:lo)", "$1"), "");
    query(func.args("abc", "b", "$0"), "abc");
    query(func.args("abc", "b", "$1"), "ac");
    query(func.args("abc", "b", "$10"), "a0c");

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
        "<a/>\n<a/>\n<a/>\n<a/>", count(func, 1));
    check(func.args(" <_/>", 2) + " ! " + func.args(" .", 2),
        "<_/>\n<_/>\n<_/>\n<_/>", count(func, 1));
    check("(1, 1) ! " + func.args(" .", 2),
        "1\n1\n1\n1", empty(func));
    check("(1, 1) ! " + func.args(" .", 2),
        "1\n1\n1\n1", empty(func));
  }

  /** Test method. */
  @Test public void resolveQName() {
    final Function func = RESOLVE_QNAME;
    query("sort(<e xmlns:p='u'>{" + func.args("p:p", " <e/>") + "}</e>/text()/tokenize(.))", "p:p");
  }

  /** Test method. */
  @Test public void reverse() {
    final Function func = REVERSE;
    query(func.args(" ()"), "");
    query(func.args(" 1"), 1);
    query(func.args(" 1 to 3"), "3\n2\n1");
    query(func.args(" (<a/>, <b/>)"), "<b/>\n<a/>");
    query(func.args(wrap(1) + "[. = 1]"), 1);
    query(func.args(" (1, 2)[. != 2]"), 1);
    query(func.args(" tokenize(<a/>)"), "");
    query(func.args(" tokenize(<a>1</a>)"), 1);
    query(func.args(" tokenize(<a>1 2</a>)"), "2\n1");
    query(func.args(" (1 to 2) ! 1"), "1\n1");
    query(func.args(" (1 to 2) ! (1, 2)"), "2\n1\n2\n1");

    check(func.args(" tail((<a/>, <b/>, <c/>))"),
        "<c/>\n<b/>", empty(TRUNK));
    check(func.args(" (<a/>, <b/>, <c/>)[position() < last()]"),
        "<b/>\n<a/>", empty(TAIL));
    check(func.args(" tail(" + func.args(" (1 to " + wrap(2) + ")[. > 0]") + ")"),
        1, exists(TRUNK));
    check(func.args(" (" + func.args(" (1 to " + wrap(2) + ")[. > 0]") + ")[position() < last()]"),
        2, exists(TAIL));
    check(func.args(REPLICATE.args(" <a/>", 2)),
        "<a/>\n<a/>", empty(func));
    check(func.args(REPLICATE.args(" (<a/>, <b/>)", 2)),
        null, exists(func));
    check(func.args(" (1, <a/>[. = ''])"),
        "<a/>\n1", root(List.class));

    check(func.args(" (<_/>, ('a', 'b'))"),
        "b\na\n<_/>", empty(func));
    check("(<a/>, <b/>)[. = ''] =>" + func.args() + " =>" + func.args(),
        "<a/>\n<b/>", empty(func));

    check(func.args(" (1 to 6, (7 to " + wrap(13) + ")[. > 12], (14 to " + wrap(20) + ")[. > 18])"),
        "20\n19\n13\n6\n5\n4\n3\n2\n1", count(REVERSE, 2));
  }

  /** Test method. */
  @Test public void serialize() {
    final Function func = SERIALIZE;
    contains(func.args(" <x/>"), "<x/>");
    contains(func.args(" <x/>", " " + serialParams("")), "<x/>");
    contains(func.args(" <x>a</x>", " " + serialParams("<method value='text'/>")), "a");

    // character maps
    query(func.args("1;2", " map { 'use-character-maps': ';=,,' }"), "1,2");
    query(func.args("1;2", " map { 'use-character-maps': map { ';': ',' } }"), "1,2");

    // boolean arguments
    query(func.args("1", " map { 'indent': 'yes' }"), 1);
    query(func.args("1", " map { 'indent': false() }"), 1);
    query(func.args("1", " map { 'indent': true() }"), 1);
    query(func.args("1", " map { 'indent': 1 }"), 1);
    error(func.args("1", " map { 'indent': 2 }"), SEROPT_X);

    query(func.args("<html/>", " map { 'html-version': 5 }"), "&lt;html/&gt;");
    query(func.args("<html/>", " map { 'html-version': 5.0 }"), "&lt;html/&gt;");
    query(func.args("<html/>", " map { 'html-version': 5.0000 }"), "&lt;html/&gt;");
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
  @Test public void sort() {
    final Function func = SORT;
    query(func.args(" ('b', 'a')", "http://www.w3.org/2005/xpath-functions/collation/codepoint"),
        "a\nb");

    query(func.args(" (1, 4, 6, 5, 3)"), "1\n3\n4\n5\n6");
    query(func.args(" (1, -2, 5, 10, -10, 10, 8)", " ()", " abs#1"), "1\n-2\n5\n8\n10\n-10\n10");
    query(func.args(" ((1, 0), (1, 1), (0, 1), (0, 0))"), "0\n0\n0\n0\n1\n1\n1\n1");
    query(func.args(" ('9', '8', '29', '310', '75', '85', '36-37', '93', '72', '185', '188', '86', "
        + "'87', '83', '79', '82', '71', '67', '63', '58', '57', '53', '31', '26', '22', '21', "
        + "'20', '15', '10')", " ()", " function($s) { number($s) }") + "[1]",
        "36-37");
    query(func.args(" (1, 2)", " ()", " function($s) { [$s] }"), "1\n2");

    query("for $i in (10000, 10001) return " + func.args(" 1 to $i") + "[1]", "1\n1");
    query("for $i in (10000, 10001) return " + func.args(" reverse(1 to $i)") + "[1]", "1\n1");
    query("for $i in (10000, 10001) return " + func.args(func.args(" reverse(1 to $i)")) + "[1]");
    query("for $i in (1, 2) return " + func.args(func.args(" (1, $i)")) + "[1]", "1\n1");

    check(func.args(" ()"), "", empty());
    check(func.args(1), 1, empty(func));

    check(func.args(" 1 to 100000000") + "[1]", 1, empty(func));
    check(func.args(" reverse(1 to 100000000)") + "[1]", 1, empty(func));
    check(func.args(" (1 to 100000000) ! 1") + "[1]", 1, empty(func));
    check(func.args(" reverse((1 to 100000000) ! 1)") + "[1]", 1, empty(func));

    check("(" + _RANDOM_DOUBLE.args() + " =>" + REPLICATE.args(10) + " => " +
        func.args() + ")[. > 1]", "", empty(func));
    check("(" + _RANDOM_DOUBLE.args() + " => " + REPLICATE.args(10, true) + " => " +
        func.args() + ")[. > 1]", "", exists(func));

    error(func.args(" true#0"), FIATOMIZE_X);
    error(func.args(" (1 to 2) ! true#0"), FIATOMIZE_X);
  }

  /** Test method. */
  @Test public void staticBaseUri() {
    final Function func = STATIC_BASE_URI;
    query("declare base-uri 'a/'; ends-with(" + func.args() + ", '/')", true);
    query("declare base-uri '.' ; ends-with(" + func.args() + ", '/')", true);
    query("declare base-uri '..'; ends-with(" + func.args() + ", '/')", true);
  }

  /** Test method. */
  @Test public void string() {
    final Function func = STRING;

    query(func.args(" ()"), "");
    query(func.args("A"), "A");
    query(func.args(wrap("A")), "A");

    check("for $s in ('a', 'b') return " + func.args(" $s"), "a\nb", empty(func));
    check("for $s in (<a/>, <b/>) return " + func.args(" $s"), "\n", exists(func));
    check("for $s in ('a', 'b') return $s[" + func.args() + ']', "a\nb", empty(func));
    check("for $s in ('a', 'b') return $s[" + func.args() + " = 'a']", "a", empty(func));
    check("for $s in (<a/>, <b/>) return $s[" + func.args() + ']', "",
        empty(func), exists(SingleIterPath.class));

    error(func.args(), NOCTX_X);
    error(func.args(" true#0"), FISTRING_X);
  }

  /** Test method. */
  @Test public void stringJoin() {
    final Function func = STRING_JOIN;
    check(func.args(CHARACTERS.args(wrap("ABC"))), "ABC", root(STRING));
    check(func.args(" string-to-codepoints(" + wrap("ABC") + ") ! codepoints-to-string(.)"),
        "ABC", root(STRING));
  }

  /** Test method. */
  @Test public void stringLength() {
    final Function func = STRING_LENGTH;
    query(func.args(" ()"), 0);
    query(func.args("A"), 1);
    query("<_>A</_>[" + func.args() + ']', "<_>A</_>");
    query(func.args(" ([()], 'a')"), 1);
    error("true#0[" + func.args() + ']', FIATOMIZE_X);
  }

  /** Test method. */
  @Test public void stringToCodepoints() {
    final Function func = STRING_TO_CODEPOINTS;
    query(func.args("ab"), "97\n98");
    query(func.args(wrap("ab")), "97\n98");

    query("subsequence(" + func.args(wrap("aaa")) + ", 3)", 97);
    query("subsequence(" + func.args(wrap("äaaa")) + ", 3)", "97\n97");
  }

  /** Test method. */
  @Test public void subsequence() {
    final Function func = SUBSEQUENCE;

    // static rewrites
    query(func.args(" ()", 0), "");
    query(func.args("A", 0), "A");
    query(func.args("A", 0, 0), "");
    query(func.args("A", 1), "A");
    query(func.args("A", 1), "A");
    query(func.args("A", 1, 1), "A");
    query(func.args(" (1, 2)", 2, 0), "");
    query(func.args(" (1, 2)", 2, 1), 2);
    query(func.args(" (1 to 3)", 2, 2), "2\n3");

    // special offset and length values
    query(func.args("A", 0.5), "A");
    query(func.args("A", " xs:double('NaN')"), "");
    query(func.args("A", 1, " xs:double('NaN')"), "");

    // known result size, iterative evaluation
    query(func.args(" (1 to 2) ! (. + 1)", 2), 3);
    query(func.args(" (1 to 3) ! (. + 1)", 2, 1), 3);
    query(func.args(" (1 to 3) ! (. + 1)", 2), "3\n4");
    query(func.args(" (1 to 3) ! (. + 1)", 3), 4);
    query(func.args(" (1 to 3) ! (. + 1)", 4), "");

    // non-numeric offsets and lengths
    query(func.args(" (1 to 3)", wrap(0)), "1\n2\n3");
    query(func.args(" (1 to 3)", wrap(0), 10), "1\n2\n3");
    query(func.args(" (1 to 3)", wrap(0), wrap(10)), "1\n2\n3");
    query(func.args(" (1 to 3)", 0, wrap(10)), "1\n2\n3");
    query(func.args(" (1 to 2)", wrap(2)), 2);
    query(func.args(" (1 to 2)", wrap(2), 1), 2);
    query(func.args(" (1 to 2)", wrap(2), wrap(1)), 2);
    query(func.args(" (1 to 2)", 2, wrap(1)), 2);

    // known result size
    query(func.args(wrap(1) + "+ 1", 1), 2);
    query(func.args(" (" + wrap(1) + "+ 1, 2)", 2), 2);
    query(func.args(" (" + wrap(1) + "+ 1, 2)", 3), "");
    query(func.args(" (" + wrap(1) + "+ 1, 2, 3)", 2) + "[2]", 3);
    query(func.args(_PROF_VOID.args(" ()"), 1), "");
    query(func.args(_PROF_VOID.args(" ()"), 2), "");

    // unknown result size
    query(func.args(wrap(1) + "[. = 0]", 1), "");
    query(func.args(wrap(1) + "[. = 1]", 1), 1);
    query(func.args(wrap(1) + "[. = 0]", 2), "");
    query(func.args(wrap(1) + "[. = 1]", 2), "");
    query(func.args(" (1 to 2)[. = 0]", 1), "");
    query(func.args(" (1 to 4)[. < 3]", 1), "1\n2");
    query(func.args(" (1 to 2)[. = 0]", 2), "");
    query(func.args(" (1 to 2)[. = 0]", 2, 1), "");
    query(func.args(" (1 to 2)[. = 0]", 2, 2), "");
    query(func.args(" (1 to 4)[. < 3]", 2), 2);

    // value-based iterator
    query(func.args(" tokenize(<_></_>)", 3), "");
    query(func.args(" tokenize(<_>W</_>)", 3), "");
    query(func.args(" tokenize(<_>W X</_>)", 3), "");
    query(func.args(" tokenize(<_>W X Y</_>)", 3), "Y");
    query(func.args(" tokenize(<_>W X Y Z</_>)", 3), "Y\nZ");

    query(func.args(1, wrap("NaN")), "");
    query(func.args(" (1 to 3)[. != 1]", wrap(2)), 3);
    query(func.args(" (1 to 4)[. != 1]", wrap(2)), "3\n4");
    query(func.args(" (1 to 5)[. != 1]", wrap(2), 2), "3\n4");
    query(func.args(" (1 to 4) ! (.*.)", 3), "9\n16");
    query(func.args(" reverse((<a/>, <b/>, <c/>, <d/>))", 3), "<b/>\n<a/>");
    query(func.args(" reverse((<a/>, <b/>, <c/>, <d/>))", wrap(1)),
        "<d/>\n<c/>\n<b/>\n<a/>");
    query(func.args(" reverse((<a/>, <b/>, <c/>, <d/>))", wrap(1), 4),
        "<d/>\n<c/>\n<b/>\n<a/>");
    query(func.args(" reverse((<a/>, <b/>, <c/>, <d/>))", wrap(2)) + "[2]", "<b/>");

    query("xs:integer(" + func.args(" (1, 2, 3)[. != 0]", 3) + ')', 3);
    query(func.args(" (1 to 6)[. != 0]", 3) + " instance of xs:integer+", true);
    query(func.args(" (1 to 6)[. != 0]", 3, 2) + " instance of xs:integer+", true);

    check(func.args(wrap(1) + "[. != 0]", 1, 2), 1, empty(func));
    check(func.args(wrap(1) + "[. != 0]", 2), "", empty());

    query(func.args(" reverse((<a/>, <b/>, <c/>))", wrap(2)) + "instance of node()+", true);
    query(func.args(" reverse((<a/>, <b/>, <c/>))", wrap(1) + ", 3") + " instance of node()+",
        true);

    query(func.args(" <_/>", " xs:double('-INF')", " xs:double('-INF')"), "");
    query(func.args(" <_/>", " xs:double('-INF')", " xs:double('INF')"), "");
    query(func.args(" <_/>", " xs:double('INF')", " xs:double('INF')"), "");
    query(func.args(" <_/>", " xs:double('NaN')"), "");
    query(func.args(" <_/>", 1, " xs:double('NaN')"), "");

    query(func.args(1, wrap("NaN")) + " instance of xs:integer", false);
    query(func.args(1, wrap(1)) + " instance of xs:integer", true);

    query(func.args(" <_/>", 1, 1), "<_/>");
    query(func.args(" (<_/>, <_/>, <_/>, <_/>)", 1, 2), "<_/>\n<_/>");

    query(func.args(" (<_/>, <_/>, <_/>)", 1, 0), "");
    query(func.args(" (<_/>, <_/>, <_/>)", 1, 1), "<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 1, 2), "<_/>\n<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 1, 3), "<_/>\n<_/>\n<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 2, 1), "<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 2, 2), "<_/>\n<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 3, 1), "<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 3, 2), "<_/>");
    query(func.args(" (<_/>, <_/>, <_/>)", 4, 0), "");
    query(func.args(" (<_/>, <_/>, <_/>)", 4, 1), "");

    check(func.args(" (<a/>, <b/>, <c/>, <d/>)", 2, 2), "<b/>\n<c/>",
        root(List.class));
    check(func.args(REPLICATE.args(" <a/>", 5), 2, 2), "<a/>\n<a/>",
        root(REPLICATE));
    check(func.args(REPLICATE.args(" <a/>", 5), 2, 3), "<a/>\n<a/>\n<a/>",
        root(REPLICATE));

    query("sort(" + func.args(" tokenize(<_/>)", 3) + ')', "");

    check(func.args(" ('x', (1 to 3)[.])", 2, 2), "1\n2", empty(Str.class), empty(List.class));
    check(func.args(" ('x', (1 to 3)[.], 4 to 6)", 2, 2), "1\n2", empty(Str.class));
    check(func.args(" ('x', (1 to 3)[.], 4 to 6)", 2, 4), "1\n2\n3\n4", empty(Str.class));
  }

  /** Test method. */
  @Test public void substring() {
    final Function func = SUBSTRING;
    contains(func.args("'ab'", " [2]"), "b");
    check(func.args(wrap("A"), 1), "A", empty(SUBSTRING), exists(STRING));
    check(func.args(wrap("A"), 0), "A", empty(SUBSTRING), exists(STRING));
    check(func.args(wrap("A"), " xs:double('NaN')"), "", root(Str.class));
    check(func.args(wrap("A"), 1, 0), "", root(Str.class));

    check(func.args(" ()", wrap(1), wrap(1)), "", root(Str.class));
    check(func.args("", wrap(1), wrap(1)), "", root(Str.class));
  }

  /** Test method. */
  @Test public void substringAfter() {
    final Function func = SUBSTRING_AFTER;
    check(func.args(" ()", wrap(1)), "", root(Str.class));
    check(func.args("", wrap(1)), "", root(Str.class));
    check(func.args(wrap(1), wrap(1)), "", root(Str.class));
    check(func.args(wrap(1), " () "), 1, root(STRING));
    check(func.args(wrap(1), ""), 1, root(STRING));

    check(func.args(wrap(1), wrap("")), 1, root(func));
    check(func.args(wrap(""), wrap(1)), "", root(func));

    check(func.args("12", "1"), 2, root(Str.class));
    check(func.args(wrap(12), "1"), 2, root(func));
    check(func.args("12", wrap(1)), 2, root(func));
    check(func.args(wrap(12), wrap(1)), 2, root(func));

    check(func.args(wrap(12), wrap(13)), "", root(func));
    check(func.args("A", "B", "?lang=de"), "", root(Str.class));
  }

  /** Test method. */
  @Test public void substringBefore() {
    final Function func = SUBSTRING_BEFORE;
    check(func.args(" ()", wrap(1)), "", root(Str.class));
    check(func.args("", wrap(1)), "", root(Str.class));
    check(func.args(wrap(1), wrap(1)), "", root(Str.class));
    check(func.args(" ()", wrap(1)), "", root(Str.class));
    check(func.args("", wrap(1)), "", root(Str.class));
    check(func.args(wrap(1), " ()"), "", root(Str.class));
    check(func.args(wrap(1), ""), "", root(Str.class));

    check(func.args(wrap(1), wrap("")), "", root(func));
    check(func.args(wrap(""), wrap(1)), "", root(func));

    check(func.args("12", "2"), 1, root(Str.class));
    check(func.args(wrap(12), "2"), 1, root(func));
    check(func.args("12", wrap(2)), 1, root(func));
    check(func.args(wrap(12), wrap(2)), 1, root(func));

    check(func.args(wrap(12), wrap(13)), "", root(func));
    check(func.args("A", "B", "?lang=de"), "", root(Str.class));
  }

  /** Test method. */
  @Test public void sum() {
    final Function func = SUM;
    query(func.args(1), 1);
    query(func.args(" 1 to 10"), 55);
    query(func.args(" 1 to 3037000499"), 4611686016981624750L);
    query(func.args(" 1 to 3037000500"), 4611686020018625250L);
    query(func.args(" 1 to 4294967295"), 9223372034707292160L);
    query(func.args(" 1 to <x>4294967295</x>"), 9223372034707292160L);
    query(func.args(" 1 to <x>0</x>"), 0);
    query(func.args(" reverse(1 to 10)"), 55);
    query(func.args(" sort(reverse(distinct-values(1 to 4294967295)))"), 9223372034707292160L);
    error(func.args(" 1 to 10000000000000"), RANGE_X);

    query(func.args(" (1 to 10) ! 1"), 10);
    query(func.args(" (1 to 10) ! 10"), 100);
    query(func.args(" (1 to 1000000) ! 1000000"), 1000000000000L);
    query(func.args(" (1 to 10) ! xs:untypedAtomic('10')"), 100);
    error(func.args(" (1 to 10) ! 'a'"), NUMDUR_X_X);
    error(func.args(" (1 to 1000000) ! 'b'"), NUMDUR_X_X);

    query("for $i in 1 to 2 return " + func.args(" ()", " $i"), "1\n2");
    query(func.args(" ()", wrap(0)), 0);
    query(func.args(" ()", "A"), "A");
    query(func.args(" ()", " 1"), 1);
    query(func.args(" ()", " ()"), "");
    query(func.args(" ()", _PROF_VOID.args("x")), "");

    query("for $i in 1 to 2 return " + func.args(_PROF_VOID.args("x"), " $i"), "1\n2");
    query(func.args(_PROF_VOID.args("x"), wrap(0)), 0);
    query(func.args(_PROF_VOID.args("x"), "A"), "A");
    query(func.args(_PROF_VOID.args("x"), " 1"), 1);
    query(func.args(_PROF_VOID.args("x"), " ()"), "");
    query(func.args(_PROF_VOID.args("x"), _PROF_VOID.args("x")), "");

    query(func.args(" 2 to 10"), 54);
    query(func.args(" 9 to 10"), 19);
    query(func.args(" -3037000500 to 3037000500"), 0);
    query(func.args(" ()", " ()"), "");
    query(func.args(1, "x"), 1);
    error(func.args(" ()", " (1, 2)"), SEQFOUND_X);

    query(func.args(" (1, 3, 5)"), 9);
    query(func.args(" (-3, -1, 1, 3)"), 0);
    query(func.args(" (1, 1.1, 1e0)"), 3.1);

    check("for $i in (1 to 2)[. != 0] return " + func.args(" $i"),
        "1\n2", type(SUM, "xs:integer"));
    check("for $i in (1 to 2)[. != 0] return " + func.args(" $i", "a"),
        "1\n2", type(SUM, "xs:integer"));
    check(func.args(" (1, 2)[. = 1]", " 0.0"),
        1, type(SUM, "xs:decimal"));
    check(func.args(" (1, 2)[. = 1]", "a"),
        1, type(SUM, "xs:anyAtomicType"));
    check(func.args(" (1, 2)[. = 1]", " (1, 2)[. = 1]"),
        1, type(SUM, "xs:integer?"));
    check(func.args(" (1, 2)[. = 1]", " ('a', 'b')[. = 'a']"),
        1, type(SUM, "xs:anyAtomicType?"));
  }

  /** Test method. */
  @Test public void tail() {
    final Function func = TAIL;

    // static rewrites
    query(func.args(" ()"), "");
    query(func.args("A"), "");
    query(func.args(" (1, 2)"), 2);
    query(func.args(" (1 to 3)"), "2\n3");

    // known result size
    query(func.args(wrap(1) + "+ 1"), "");
    query(func.args(" (" + wrap(1) + "+ 1, 3)"), 3);
    query(func.args(_PROF_VOID.args(" ()")), "");

    // unknown result size
    query(func.args(wrap(1) + "[. = 0]"), "");
    query(func.args(wrap(1) + "[. = 1]"), "");
    query(func.args(" (1 to 2)[. = 0]"), "");
    query(func.args(" (1 to 4)[. < 3]"), 2);

    // value-based iterator
    query(func.args(" tokenize(<_></_>)"), "");
    query(func.args(" tokenize(<_>X</_>)"), "");
    query(func.args(" tokenize(<_>X Y</_>)"), "Y");
    query(func.args(" tokenize(<_>X Y Z</_>)"), "Y\nZ");

    // nested function calls
    query(func.args(func.args(" tokenize(<_>X Y Z</_>)")), "Z");
    query(func.args(" subsequence(tokenize(<_>W X Y Z</_>), 3)"), "Z");
    query(func.args(" subsequence(tokenize(<_/>)," + wrap(1) + ")"), "");
    query(func.args(_UTIL_RANGE.args(" tokenize(<_>W X Y Z</_>)", 3, 4)), "Z");
    query(func.args(_UTIL_RANGE.args(" tokenize(<_/>)," + wrap(1), 1)), "");

    check(func.args(REPLICATE.args(" <a/>", 2)), "<a/>", root(CElem.class));
    check(func.args(REPLICATE.args(" <a/>", 3)), "<a/>\n<a/>", root(REPLICATE));
    check(func.args(REPLICATE.args(" <a/>[. = '']", 2)), "<a/>", root(IterFilter.class));

    check(func.args(" (<a/>, <b/>)"), "<b/>", root(CElem.class), empty(TAIL));
    check(func.args(" (<a/>, <b/>, <c/>)"), "<b/>\n<c/>", root(List.class), empty(TAIL));
    check(func.args(" (1 to 2, <a/>)"), "2\n<a/>", root(List.class), empty(TAIL));
  }

  /** Test method. */
  @Test public void tokenize() {
    final Function func = TOKENIZE;
    query(func.args("a", "", "j"), "\na\n");
    query(func.args(wrap("a"), "", "j"), "\na\n");
    query(func.args("a", wrap(""), "j"), "\na\n");
    query(func.args(wrap("a"), wrap(""), "j"), "\na\n");

    query("subsequence(" + func.args(wrap("a b c d")) + ", 3)", "c\nd");
    query("subsequence(" + func.args(wrap("a,b,c,d"), ",") + ", 3)", "c\nd");
    query("subsequence(" + func.args(wrap("a!!b!!c!!d"), "!!") + ", 3)", "c\nd");
    query("subsequence(" + func.args(wrap("")) + ", 3)", "");
    query("subsequence(" + func.args(wrap(""), "!!") + ", 3)", "");

    query("subsequence(" + func.args(wrap("aXbXcXd"), "x", "i") + ", 3)", "c\nd");

    query(func.args(wrap("a b c d")), "a\nb\nc\nd");
    query(func.args(wrap("a,b,c,d"), ","), "a\nb\nc\nd");
    query(func.args(wrap("a!!b!!c!!d"), "!!"), "a\nb\nc\nd");
    query(func.args(wrap("")), "");
    query(func.args(wrap(""), "!!"), "");

    check(func.args(" normalize-space(" + wrap("A") + ")", " ' '"), "A", empty(NORMALIZE_SPACE));
    check("(<_>A</_>, <_>B</_>) ! " + func.args(" normalize-space()", " ' '"), "A\nB",
        empty(NORMALIZE_SPACE));
    check(func.args(" normalize-space(" + wrap("A") + ")", wrap(";")), "A",
        exists(NORMALIZE_SPACE));
    check(func.args(" normalize-space(" + wrap("A") + ")", ";"), "A",
        exists(NORMALIZE_SPACE));

    error(func.args("a", ""), REGEMPTY_X);
  }

  /** Test method. */
  @Test public void translate() {
    final Function func = TRANSLATE;
    query(func.args("a", "a", "b"), "b");
    query(func.args("a", "", "b"), "a");

    check(func.args("a", wrap(""), "b"), "a", root(func));
    check(func.args(wrap(""), "a", "b"), "", root(func));
    check(func.args(wrap("abcd"), "bd", "B"), "aBc", root(func));
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
  @Test public void unparsedText() {
    final Function func = UNPARSED_TEXT;
    contains(func.args(TEXT), "<html");
    contains(func.args(TEXT, "US-ASCII"), "<html");
    error(func.args(TEXT, "xyz"), ENCODING_X);
  }

  /** Test method. */
  @Test public void unparsedTextLines() {
    final Function func = UNPARSED_TEXT_LINES;
    query(func.args(" ()"), "");
  }

  /** Test method. */
  @Test public void xmlToJson() {
    final Function func = XML_TO_JSON;
    query(func.args(" <map xmlns='http://www.w3.org/2005/xpath-functions'>"
        + "<string key=''>í</string></map>", " map { 'indent' : 'no' }"), "{\"\":\"\u00ed\"}");
    query(func.args(" <fn:string key='root'>X</fn:string>"), "\"X\"");
  }
}
