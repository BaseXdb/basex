package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Utility Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void arrayMembers() {
    final Function func = _UTIL_ARRAY_MEMBERS;

    query(func.args(" []"), "");
    query(func.args(" [ () ]"), "[()]");
    query(func.args(" [ 1 ]"), "[1]");
    query(func.args(" [ 1, 2 ]"), "[1]\n[2]");
    query(func.args(" [ (1, 2) ]"), "[(1, 2)]");
    query(func.args(" [ (1, 2), 3 ]"), "[(1, 2)]\n[3]");
  }

  /** Test method. */
  @Test public void arrayValues() {
    final Function func = _UTIL_ARRAY_VALUES;

    query(func.args(" []"), "");
    query(func.args(" [ () ]"), "");
    query(func.args(" [ 1 ]"), "1");
    query(func.args(" [ 1, 2 ]"), "1\n2");
    query(func.args(" [ (1, 2) ]"), "1\n2");
    query(func.args(" [ (1, 2), 3 ]"), "1\n2\n3");
  }

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
  @Test public void ddo() {
    final Function func = _UTIL_DDO;
    query(func.args(" <a/>"), "<a/>");
    query(func.args(" (<a/>, <b/>)"), "<a/>\n<b/>");
    query(func.args(" reverse((<a/>, <b/>))"), "<b/>\n<a/>");
    error(func.args(1), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void deepEquals() {
    final Function func = _UTIL_DEEP_EQUAL;
    query(func.args(1, 1), true);
    query(func.args(1, 1, "ALLNODES"), true);
    error(func.args("(1 to 2)", "(1 to 2)", "X"), INVALIDOPTION_X);
  }

  /** Test method. */
  @Test public void duplicates() {
    final Function func = _UTIL_DUPLICATES;

    query(func.args(1), "");
    query(func.args(" (1, 2)"), "");
    query(func.args(" (1, 2, 1)"), 1);
    query(func.args(" (1, 2, 1, 1)"), 1);
    query(func.args(" (1, 2, 2, 1)"), "2\n1");
    query(func.args(" (1, 'a', true())"), "");
    query(func.args(" 1 to 5000000000"), "");
    query(func.args(" (1 to 5000000000) ! 1"), 1);
    query(func.args(" <_>1</_> to 5000000000"), "");

    query(func.args(" (1 to 5) ! <_>1</_>"), 1);
    query(func.args(" (<a>1</a>, <b>1</b>)"), 1);

    query(func.args(" 'a'", "?lang=de"), "");
    query(func.args(" ('a', 'a')", "?lang=de"), "a");
    query(func.args(" ('a', 'a', 'a')", "?lang=de"), "a");

    error(func.args(" (1, true#0)"), FIATOM_X);
    error(func.args(" (1 to 5) ! true#0"), FIATOM_X);

    // optimizations
    String seq = "let $seq := (<_>1</_>, 2, <_>1</_>) return ";
    check(seq + "count($seq)  = count(distinct-values($seq))", false, root(EMPTY), exists(func));
    check(seq + "count($seq) <= count(distinct-values($seq))", false, root(EMPTY), exists(func));
    check(seq + "count($seq) <  count(distinct-values($seq))", false, root(Bln.class));
    check(seq + "count($seq) >= count(distinct-values($seq))", true, root(Bln.class));
    check(seq + "count($seq) >  count(distinct-values($seq))", true, root(EXISTS), exists(func));
    check(seq + "count($seq) != count(distinct-values($seq))", true, root(EXISTS), exists(func));

    check(seq + "count(distinct-values($seq))  = count($seq)", false, root(EMPTY), exists(func));
    check(seq + "count(distinct-values($seq)) <= count($seq)", true, root(Bln.class));
    check(seq + "count(distinct-values($seq)) <  count($seq)", true, root(EXISTS), exists(func));
    check(seq + "count(distinct-values($seq)) >= count($seq)", false, root(EMPTY), exists(func));
    check(seq + "count(distinct-values($seq)) >  count($seq)", false, root(Bln.class));
    check(seq + "count(distinct-values($seq)) != count($seq)", true, root(EXISTS), exists(func));

    seq = "let $seq := (<_>1</_>, 2, <_>1</_>)[. = 1] return ";
    check(seq + "count($seq)  = count(distinct-values($seq))", false, root(EMPTY), exists(func));
    check(seq + "count($seq) <= count(distinct-values($seq))", false, root(EMPTY), exists(func));
    check(seq + "count($seq) <  count(distinct-values($seq))", false, root(Bln.class));
    check(seq + "count($seq) >= count(distinct-values($seq))", true, root(Bln.class));
    check(seq + "count($seq) >  count(distinct-values($seq))", true, root(EXISTS), exists(func));
    check(seq + "count($seq) != count(distinct-values($seq))", true, root(EXISTS), exists(func));

    check(seq + "count(distinct-values($seq))  = count($seq)", false, root(EMPTY), exists(func));
    check(seq + "count(distinct-values($seq)) <= count($seq)", true, root(Bln.class));
    check(seq + "count(distinct-values($seq)) <  count($seq)", true, root(EXISTS), exists(func));
    check(seq + "count(distinct-values($seq)) >= count($seq)", false, root(EMPTY), exists(func));
    check(seq + "count(distinct-values($seq)) >  count($seq)", false, root(Bln.class));
    check(seq + "count(distinct-values($seq)) != count($seq)", true, root(EXISTS), exists(func));
  }

  /** Test method. */
  @Test public void iff() {
    final Function func = _UTIL_IF;
    query(func.args(" 1", 1), 1);
    query(func.args(" ()", 1), "");

    query(func.args(" 1", 1, 2), 1);
    query(func.args(" ()", 1, 2), 2);
    query(func.args(" (<a/>, <b/>)", 1, 2), 1);
    error(func.args(" (1, 2)", 1, 2), EBV_X);
  }

  /** Test method. */
  @Test public void init() {
    final Function func = _UTIL_INIT;

    // static rewrites
    query(func.args(" ()"), "");
    query(func.args("A"), "");
    query(func.args(" (1, 2)"), 1);
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
        root(SUBSEQUENCE));

    check(func.args(" util:replicate(<a/>, 2)"), "<a/>", root(CElem.class));
    check(func.args(" util:replicate(<a/>, 3)"), "<a/>\n<a/>", root(_UTIL_REPLICATE));
    check(func.args(" util:replicate(<a/>[. = ''], 2)"), "<a/>", root(IterFilter.class));

    check(func.args(" (<a/>, <b/>)"), "<a/>", root(CElem.class), empty(_UTIL_INIT));
    check(func.args(" (<a/>, <b/>, <c/>)"), "<a/>\n<b/>", root(List.class), empty(_UTIL_INIT));
    check(func.args(" (<a/>, 1 to 2)"), "<a/>\n1", root(List.class), empty(_UTIL_INIT));

    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 1, 1)"),
        "", empty());
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 1, 2)"),
        "<_>1</_>", root(HEAD));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 1, 3)"),
        "<_>1</_>\n<_>2</_>", root(SUBSEQUENCE));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 2, 3)"),
        "<_>2</_>\n<_>3</_>", root(SUBSEQUENCE));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 4, 2)"),
        "<_>4</_>", root(_UTIL_ITEM));
    check(func.args(" subsequence((1 to 10) ! <_>{ . }</_>, 5, 1)"),
        "", empty());
  }

  /** Test method. */
  @Test public void intersperse() {
    final Function func = _UTIL_INTERSPERSE;

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
    query(func.args(" (<a/>, <b/>)", 1), "<a/>");
    query(func.args(" (<a/>, <b/>)", 3), "");

    query(func.args(" (<a/>, <b/>)[name()]", 1), "<a/>");
    query(func.args(" (<a/>, <b/>)[name()]", 2), "<b/>");
    query(func.args(" (<a/>, <b/>)[name()]", 3), "");

    query(func.args(" (<a/>, <b/>)", 1.5), "");
    query(func.args(" <a/>", 2), "");
    query(func.args(" (<a/>, <b/>)", " <_>1</_>"), "<a/>");
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

    check(func.args(" (1, 2, <_/>)", 3), "<_/>", root(CElem.class));
    check(func.args(" reverse((1, 2, <_/>))", 2), 2, empty(REVERSE));

    check(func.args(" tail((1, 2, 3, <_/>))", 2), 3, empty(TAIL));
    check(func.args(" tail((<_/>[data()], <_/>, <_/>))", 2), "", empty(TAIL), root(_UTIL_ITEM));

    check(func.args(" util:init((1, 2, 3, <_/>))", 2), 2, empty(_UTIL_INIT));

    check(func.args(" (7 to 9)[. = 8]", 1), 8, exists(HEAD), type(HEAD, "xs:integer?"));

    check(func.args(" (<a/>, <b/>, <c/>)", 2), "<b/>", root(CElem.class));
    check(func.args(" (<a/>, <b/>, <c/>, <d/>)", 2), "<b/>", root(CElem.class));
    check(func.args(" (<a/>, <b/>[data()], <c/>)", 2), "<c/>", root(_UTIL_OR));
    check(func.args(" (<a/>, <b/>[data()], <c/>, <d/>)", 2), "<c/>", root(_UTIL_OR));
    check(func.args(" (<a/>[data()], <b/>, <c/>)", 2), "<c/>", root(_UTIL_ITEM));

    check(func.args(" util:replicate(<a/>, 2)", 1), "<a/>", root(CElem.class));
    check(func.args(" util:replicate(<a/>, 2)", 2), "<a/>", root(CElem.class));
    check(func.args(" util:replicate(<a/>, 2)", 3), "", empty());
  }

  /** Test method. */
  @Test public void last() {
    final Function func = _UTIL_LAST;

    query(func.args(" ()"), "");
    query(func.args(1), 1);
    query(func.args(" 1 to 2"), 2);

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>, <b/>)"), "<b/>");
    query(func.args(" (<a/>, <b/>)[position() > 2]"), "");

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>, <b/>)"), "<b/>");

    check(func.args(" prof:void(())"), "", empty(func));
    check(func.args(" <a/>"), "<a/>", empty(func));
    check(func.args(" (<a/>, <b/>)[name()]"), "<b/>", type(func, "element(a)|element(b)?"));
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

    check(func.args(" util:replicate(<a/>, 2)"), "<a/>", root(CElem.class));
    check(func.args(" util:replicate(<a/>[. = ''], 2)"), "<a/>",
        root(IterFilter.class), empty(_UTIL_REPLICATE));
    check(func.args(" util:replicate((<a/>, <b/>)[. = ''], 2)"), "<b/>",
        root(_UTIL_LAST), empty(_UTIL_REPLICATE));
    check(func.args(" util:replicate(<a/>, <_>2</_>)"), "<a/>", exists(_UTIL_REPLICATE));

    check(func.args(" (<a/>, <b/>)"), "<b/>", root(CElem.class));
    check(func.args(" (<a/>, 1 to 2)"), 2, root(Int.class));
  }

  /** Test method. */
  @Test public void mapEntries() {
    final Function func = _UTIL_MAP_ENTRIES;

    query(func.args(" map {}"), "");
    query(func.args(" map { 1: 2 }") + "?key", 1);
    query(func.args(" map { 1: 2 }") + "?value", 2);
    query(func.args(" map { 1: (2, 3) }") + "?key", 1);
    query(func.args(" map { 1: (2, 3) }") + "?value", "2\n3");
    query(func.args(" map { 1: 2, 3: 4 }") + "?key", "1\n3");
    query(func.args(" map { 1: 2, 3: 4 }") + "?value", "2\n4");
  }

  /** Test method. */
  @Test public void mapValues() {
    final Function func = _UTIL_MAP_VALUES;

    query(func.args(" map {}"), "");
    query(func.args(" map { 1: 2 }"), 2);
    query(func.args(" map { 1: (2, 3) }"), "2\n3");
    query(func.args(" map { 1: 2, 3: 4 }"), "2\n4");
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

    query("count(" + func.args(" <_>1</_>[. = 1]", 2) + ')', 1);
    query("count(" + func.args(" (1, 2)[. = 1]", 3) + ')', 1);
    query("count(" + func.args(" (1, 2, 3)[. = 1]", 4) + ')', 1);
    query("count(" + func.args(" (1, 2, 3)[. = 4]", 4) + ')', 1);
    query("count(" + func.args(" (1, 2, 3)[. = 4]", " (4, 5)") + ')', 2);

    check(func.args(null, null), "", empty());
    check(func.args(null, 1), 1, root(Int.class));
    check(func.args(1, null), 1, root(Int.class));
    check(func.args(null, " <x/>"), "<x/>", root(CElem.class));
    check(func.args(" <x/>", null), "<x/>", root(CElem.class));

    check(func.args(" (1, <_>2</_>[. = 3])", null), 1, root(List.class));
    check(func.args(" (2, <_>3</_>[. = 4])", "<z/>"), 2, root(List.class));

    check(func.args(" (3, <_>4</_>)[. = 3]", null), 3, root(IterFilter.class));
    check(func.args(" (4, <_>5</_>)[. = 4]", "<z/>"), 4, root(_UTIL_OR));

    check(func.args(" prof:void(1)", 2), 2, root(List.class));
    check(func.args(" prof:void(2)", " prof:void(3)"), "", root(List.class));

    check(func.args(" <_>6</_>[. = 6]", 7), "<_>6</_>", root(_UTIL_OR));
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
    query(func.args(" (<a/>, <b/>)", 0, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>, <b/>)", 1, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>, <b/>)", 2.5, 3.5), "");
    query(func.args(" (<a/>, <b/>)", 3, 4), "");

    query(func.args(" (<a/>, <b/>)[name()]", 1, 9223372036854775807L), "<a/>\n<b/>");
  }

  /** Test method. */
  @Test public void replicate() {
    final Function func = _UTIL_REPLICATE;

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
    query(func.args(" <a/>", " <_>2</_>"), "<a/>\n<a/>");

    query(func.args(" 1[. = 1]", 2), "1\n1");

    check(func.args(" <a/>", -1), "", empty());
    check(func.args(" <a/>", 0), "", empty());
    check(func.args(" ()", " <_>2</_>"), "", empty());
    check(func.args(" <a/>", 1), "<a/>", empty(func));
    check(func.args(" <a/>", 2), "<a/>\n<a/>", type(func, "element(a)+"));
    check(func.args(" <a/>", " <_>2</_>"), "<a/>\n<a/>", type(func, "element(a)*"));

    check(func.args(func.args(" <a/>", 2), 2),
        "<a/>\n<a/>\n<a/>\n<a/>", count(_UTIL_REPLICATE, 1));
    check(func.args(" <_/>", 2) + " ! " + func.args(" .", 2),
        "<_/>\n<_/>\n<_/>\n<_/>", count(_UTIL_REPLICATE, 1));
    check("(1,1) ! " + func.args(" .", 2),
        "1\n1\n1\n1", empty(_UTIL_REPLICATE));

    check("(1,1) ! " + func.args(" .", 2),
        "1\n1\n1\n1", empty(_UTIL_REPLICATE));
  }

  /** Test method. */
  @Test public void within() {
    final Function func = _UTIL_WITHIN;

    // minimum
    query(func.args(" ()", -1), true);
    query(func.args(" ()", 0), true);
    query(func.args(" ()", 1), false);

    query(func.args(9, -1), true);
    query(func.args(9, 0), true);
    query(func.args(9, 1), true);
    query(func.args(9, 2), false);

    query(func.args(" <a/>", -1), true);
    query(func.args(" <a/>", 0), true);
    query(func.args(" <a/>", 1), true);
    query(func.args(" <a/>", 2), false);

    query(func.args(" trace(1)", 1), true);

    query(func.args(" (8, 9)", 1), true);
    query(func.args(" (8, 9)", 2), true);
    query(func.args(" (8, 9)", 3), false);

    check(func.args(" (8, 9, 9)[. = 9]", 0), true, root(Bln.class));
    check(func.args(" (8, 9, 9)[. = 9]", 1), true, root(Bln.class));
    query(func.args(" (8, 9, 9)[. = 9]", 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", 3), false);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>1</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>2</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>3</_>"), false);

    // minimum and maximum
    query(func.args(" ()", 0, 0), true);
    query(func.args(" ()", 0, 1), true);
    query(func.args(" ()", 1, 0), false);

    query(func.args(9, 0, 0), false);
    query(func.args(9, 0, 1), true);
    query(func.args(9, 0, 1), true);
    query(func.args(9, 1, 2), true);
    query(func.args(9, 2, 1), false);

    query(func.args(" <a/>", 0, 0), false);
    query(func.args(" <a/>", 0, 1), true);
    query(func.args(" <a/>", 0, 1), true);
    query(func.args(" <a/>", 1, 2), true);
    query(func.args(" <a/>", 2, 1), false);

    query(func.args(" trace(1)", 1, 2), true);

    query(func.args(" (8, 9)", 1, 1), false);
    query(func.args(" (8, 9)", 1, 2), true);
    query(func.args(" (8, 9)", 2, 2), true);
    query(func.args(" (8, 9)", 2, 3), true);
    query(func.args(" (8, 9)", 3, 2), false);

    check(func.args(" (8, 9, 9)[. = 9]", 0, 0), false, root(Bln.class));
    query(func.args(" (8, 9, 9)[. = 9]", 1, 1), false);
    query(func.args(" (8, 9, 9)[. = 9]", 1, 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, 3), true);
    check(func.args(" (8, 9, 9)[. = 9]", 3, 2), false, root(Bln.class));

    query(func.args(" (8, 9, 9)[. = 9]", 0, " <_>0</_>"), false);
    query(func.args(" (8, 9, 9)[. = 9]", 1, " <_>1</_>"), false);
    query(func.args(" (8, 9, 9)[. = 9]", 1, " <_>2</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, " <_>2</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", 2, " <_>3</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", 3, " <_>2</_>"), false);

    query(func.args(" (8, 9, 9)[. = 9]", " <_>0</_>", 0), false);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>1</_>", 1), false);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>1</_>", 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>2</_>", 2), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>2</_>", 3), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>3</_>", 2), false);

    query(func.args(" (8, 9, 9)[. = 9]", " <_>0</_>", " <_>0</_>"), false);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>1</_>", " <_>1</_>"), false);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>1</_>", " <_>2</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>2</_>", " <_>2</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>2</_>", " <_>3</_>"), true);
    query(func.args(" (8, 9, 9)[. = 9]", " <_>3</_>", " <_>2</_>"), false);

    // simplified arguments
    check(func.args(" sort(<_/>)", " <_>0</_>"), true, empty(SORT));
    check(func.args(" reverse(<_/>)", " <_>0</_>"), true, empty(REVERSE));
    check(func.args(" for $i in 1 to 2 order by $i return $i", " <_>0</_>"),
        true, empty(GFLWOR.class));

    // rewritings
    check("count((8, 9, 9)[. >= 9]) < 3", true, root(func));
    check("count((8, 9, 9)[. >= 9]) < 2.1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) < 2", false, root(func));

    check("count((8, 9, 9)[. >= 9]) <= 2.1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) <= 2", true, root(func));
    check("count((8, 9, 9)[. >= 9]) <= 1.9", false, root(func));

    check("count((8, 9, 9)[. >= 9]) > 1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) > 1.1", true, root(func));
    check("count((8, 9, 9)[. >= 9]) > 1.9", true, root(func));
    check("count((8, 9, 9)[. >= 9]) > 2", false, root(func));
    check("count((8, 9, 9)[. >= 9]) > 2.1", false, root(func));

    check("count((8, 9, 9)[. >= 9]) >= 1.9", true, root(func));
    check("count((8, 9, 9)[. >= 9]) >= 2", true, root(func));
    check("count((8, 9, 9)[. >= 9]) >= 2.1", false, root(func));

    check("count((8, 9, 9)[. >= 9]) = 1", false, root(func));
    check("count((8, 9, 9)[. >= 9]) = 2", true, root(func));
    check("count((8, 9, 9)[. >= 9]) = 3", false, root(func));

    check("count((1 to 10)[. < 5]) = 1 to 3", false, root(func));
    check("count((1 to 10)[. < 5]) = 3 to 5", true, root(func));
    check("count((1 to 10)[. < 5]) = 5 to 7", false, root(func));

    check("(1 to 2) ! (count((1 to 10)[. < 5]) = .)", "false\nfalse", exists(func));
  }
}
