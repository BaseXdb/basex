package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.ast.*;
import org.junit.*;

/**
 * This class tests the functions of the Utility Module.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class UtilModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void chars() {
    final Function func = _UTIL_CHARS;

    // test pre-evaluation
    query(func.args(" ()"), "");
    check(func.args(" ()"), "", empty());
    query(func.args(""), "");
    query(func.args("abc"), "a\nb\nc");
    query("count(" + func.args(" string-join(util:replicate('A', 100000))") + ')', 100000);
    check("count(" + func.args(" string-join(util:replicate('A', 100000))") + ')', 100000,
        empty(func), empty(STRING_LENGTH));

    // test iterative evaluation
    query(func.args(" <_/>"), "");
    query(func.args(" <_>abc</_>"), "a\nb\nc");
    query(func.args(" <_>abc</_>") + "[2]", "b");
    query(func.args(" <_>abc</_>") + "[last()]", "c");
    check("count(" + func.args(" string-join(util:replicate(<_>A</_>, 100000))") + ')', 100000,
        exists(STRING_LENGTH));
  }

  /** Test method. */
  @Test public void deepEquals() {
    final Function func = _UTIL_DEEP_EQUAL;
    query(func.args(1, 1), true);
    query(func.args(1, 1, "ALLNODES"), true);
    error(func.args("(1 to 2)", "(1 to 2)", "X"), QueryError.INVALIDOPTION_X);
  }

  /** Test method. */
  @Test public void iff() {
    final Function func = _UTIL_IF;
    query(func.args(" 1", 1), 1);
    query(func.args(" ()", 1), "");

    query(func.args(" 1", 1, 2), 1);
    query(func.args(" ()", 1, 2), 2);
    query(func.args(" (<a/>,<b/>)", 1, 2), 1);
    error(func.args(" (1,2)", 1, 2), EBV_X);
  }

  /** Test method. */
  @Test public void init() {
    final Function func = _UTIL_INIT;

    // static rewrites
    query(func.args(" ()"), "");
    query(func.args("A"), "");
    query(func.args(" (1,2)"), 1);
    query(func.args(" (1 to 3)"), "1\n2");

    // known result size
    query(func.args(" <_>1</_> + 1"), "");
    query(func.args(" (<_>1</_> + 1, 3)"), 2);
    query(func.args(" prof:void(())"), "");

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
    check(func.args(" (<a/>,<b/>)"), "<a/>", exists(HEAD));
    check(func.args(" sort((1 to 3) ! <_>{ . }</_>)"), "<_>1</_>\n<_>2</_>", exists(func));
    check("reverse(" + func.args(" (<a/>,<b/>,<c/>))"), "<b/>\n<a/>", exists(func));

    // nested function calls
    check(func.args(func.args(" ()")), "", empty());
    check(func.args(func.args(" (<a/>)")), "", empty());
    check(func.args(func.args(" (<a/>,<b/>)")), "", empty());
    check(func.args(func.args(" (<a/>,<b/>,<c/>)")), "<a/>", exists(HEAD));
    check(func.args(func.args(" (<a/>,<b/>,<c/>,<d/>)")), "<a/>\n<b/>", exists(SUBSEQUENCE));
  }

  /** Test method. */
  @Test public void item() {
    final Function func = _UTIL_ITEM;

    query(func.args(" ()", 1), "");
    query(func.args(1, 1), 1);
    query(func.args(1, 0), "");
    query(func.args(1, 2), "");
    query(func.args(" 1 to 2", 2), 2);
    query(func.args(" 1 to 2", 3), "");
    query(func.args(" 1 to 2", 0), "");
    query(func.args(" 1 to 2", -1), "");
    query(func.args(" 1 to 2", 1.5), "");

    query("for $i in 1 to 2 return " + func.args(" $i", 1), "1\n2");
    query(func.args(" (<a/>,<b/>)", 1), "<a/>");
    query(func.args(" (<a/>,<b/>)", 3), "");

    query(func.args(" (<a/>,<b/>)[name()]", 1), "<a/>");
    query(func.args(" (<a/>,<b/>)[name()]", 2), "<b/>");
    query(func.args(" (<a/>,<b/>)[name()]", 3), "");

    query(func.args(" (<a/>,<b/>)", 1.5), "");
    query(func.args(" <a/>", 2), "");
    query(func.args(" (<a/>,<b/>)", " <_>1</_>"), "<a/>");
    query(func.args(" tokenize(<_>1</_>)", 2), "");

    query(func.args(" 1", " <_>0</_>"), "");
    query(func.args(" 1[. = 1]", " <_>1</_>"), 1);
    query(func.args(" 1[. = 1]", " <_>2</_>"), "");

    check(func.args(" prof:void(())", 0), "", empty(func));
    check(func.args(" (7 to 9)[. = 8]", -1), "", empty());
    check(func.args(" (7 to 9)[. = 8]", 0), "", empty());
    check(func.args(" (7 to 9)[. = 8]", 1.5), "", empty());
    check(func.args(" 1[. = 1]", 1), 1, empty(func));
    check(func.args(" 1[. = 1]", 2), "", empty());

    check(func.args(" (1,2,<_/>)", 3), "<_/>", exists(_UTIL_LAST));
    check(func.args(" reverse((1,2,<_/>))", 2), 2, empty(REVERSE));

    check(func.args(" tail((1,2,3,<_/>))", 2), 3, empty(TAIL));
    check(func.args(" util:init((1,2,3,<_/>))", 2), 2, empty(_UTIL_INIT));

    check(func.args(" (7 to 9)[. = 8]", 1), 8, exists(HEAD), type(HEAD, "xs:integer?"));
  }

  /** Test method. */
  @Test public void last() {
    final Function func = _UTIL_LAST;

    query(func.args(" ()"), "");
    query(func.args(1), 1);
    query(func.args(" 1 to 2"), 2);

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>,<b/>)"), "<b/>");
    query(func.args(" (<a/>,<b/>)[position() > 2]"), "");

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>,<b/>)"), "<b/>");

    check(func.args(" prof:void(())"), "", empty(func));
    check(func.args(" <a/>"), "<a/>", empty(func));
    check(func.args(" (<a/>,<b/>)[name()]"), "<b/>", type(func, "element()?"));
    check(func.args(" reverse((1, 2, 3)[. > 1])"), 2, exists(HEAD));

    check(func.args(" tokenize(<_/>)"), "", exists(_UTIL_LAST));
    check(func.args(" tokenize(<_>1</_>)"), 1, exists(_UTIL_LAST));
    check(func.args(" tokenize(<_>1 2</_>)"), 2, exists(_UTIL_LAST));

    check(func.args(" tail(tokenize(<a/>))"), "", exists(TAIL));
    check(func.args(" tail(1 ! <_>{.}</_>)"), "", empty());
    check(func.args(" tail((1 to 2) ! <_>{.}</_>)"), "<_>2</_>", empty(TAIL));
    check(func.args(" tail((1 to 3) ! <_>{.}</_>)"), "<_>3</_>", empty(TAIL));

    check(func.args(" util:init((1 to 3) ! <_>{.}</_>)"), "<_>2</_>", empty(_UTIL_INIT));
    check(func.args(" util:init(tokenize(<a/>))"), "", exists(_UTIL_INIT));
  }

  /** Test method. */
  @Test public void or() {
    final Function func = _UTIL_OR;
    query(func.args(1, 2), 1);
    query(func.args(" <x/>", 2), "<x/>");
    query(func.args(" (1 to 2)[. = 1]", 2), 1);
    // test if second branch will be evaluated
    query(func.args(" (1 to 2)[. != 0]", " (1 to 1000000000000)[. != 0]"), "1\n2");

    query(func.args(" ()", 2), 2);
    query(func.args(" ()", " <x/>"), "<x/>");
    query(func.args(" (1 to 2)[. = 0]", " <x/>"), "<x/>");

    query(func.args(" tokenize(<a/>)", 2), 2);
    query(func.args(" tokenize(<a>1</a>)", 2), 1);
    query("sort(" + func.args(" tokenize(<a>1</a>)", 2) + ")", 1);
    query("sort(" + func.args(" tokenize(<a/>)", 2) + ")", 2);
  }

  /** Test method. */
  @Test public void range() {
    final Function func = _UTIL_RANGE;
    query(func.args(" ()", 1, 2), "");
    query(func.args(1, 1, 2), 1);
    query(func.args(1, 0, 2), 1);
    query(func.args(1, 2, 2), "");
    query(func.args(" 1 to 2", 2, 2), 2);
    query(func.args(" 1 to 2", 3, 2), "");
    query(func.args(" 1 to 2", 0, 2), "1\n2");
    query(func.args(" 1 to 2", -1, 2), "1\n2");
    query(func.args(" 1 to 2", 1.5, 2), 2);

    query("for $i in 1 to 2 return " + func.args(" $i", 1, 2), "1\n2");
    query(func.args(" (<a/>,<b/>)", 0, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>,<b/>)", 1, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>,<b/>)", 2.5, 3.5), "");
    query(func.args(" (<a/>,<b/>)", 3, 4), "");

    query(func.args(" (<a/>,<b/>)[name()]", 1, 9223372036854775807L), "<a/>\n<b/>");
  }

  /** Test method. */
  @Test public void replicate() {
    final Function func = _UTIL_REPLICATE;

    query(func.args(" ()", 0), "");
    query(func.args(" ()", 1), "");
    query(func.args(1, 0), "");
    query(func.args("A", 1), "A");
    query(func.args("A", 2), "A\nA");
    query(func.args(" (0,'A')", 1), "0\nA");
    query(func.args(" (0,'A')", 2), "0\nA\n0\nA");
    query(func.args(" 1 to 10000", 10000) + "[last()]", "10000");
    query(func.args(" 1 to 10000", 10000) + "[1]", "1");
    query(func.args(" 1 to 10000", 10000) + "[10000]", "10000");
    query(func.args(" 1 to 10000", 10000) + "[10001]", "1");
    query("count(" + func.args(" 1 to 1000000", 1000000) + ")", 1000000000000L);
    query("count(" + func.args(func.args(" 1 to 3", 3), 3) + ")", 27);

    query("for $i in 1 to 2 return " + func.args(1, " $i"), "1\n1\n1");
    query(func.args(" <a/>", 2), "<a/>\n<a/>");
    query(func.args(" <a/>", " <_>2</_>"), "<a/>\n<a/>");

    query(func.args(" 1[. = 1]", 2), "1\n1");

    check(func.args(" <a/>", 0), "", empty());
    check(func.args(" ()", " <_>2</_>"), "", empty());
    check(func.args(" <a/>", 1), "<a/>", empty(func));
    check(func.args(" <a/>", 2), "<a/>\n<a/>", type(func, "element()+"));
    check(func.args(" <a/>", " <_>2</_>"), "<a/>\n<a/>", type(func, "element()*"));

    error(func.args(1, -1), QueryError.UTIL_NEGATIVE_X);
  }
}
