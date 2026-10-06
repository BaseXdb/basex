package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the simple map operator.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SimpleMapTest extends SandboxTest {
  /** Basic tests. */
  @Test public void basic() {
    query("1 ! 2", 2);
    query("1 ! (1 to 2)", "1\n2");
    query("(1 to 2) ! 3", "3\n3");
    query("(1 to 2) ! (3 to 4)", "3\n4\n3\n4");

    query("(1 to 2) ! <a/>", "<a/>\n<a/>");
  }

  /** Mixed operands. */
  @Test public void mixed() {
    query("<a/> ! (('b' ! 'c'), name())", "c\na");
    query("((1 to 100000) ! 5)[1]", 5);
    query("<a><b/></a>/b ! ancestor-or-self::node() ! name()", "a\nb");
    query("<_ a='a' b='b'/> ! (@a, @b) ! string()", "a\nb");
  }

  /** Empty results. */
  @Test public void noResults() {
    check("() ! ()", "", empty());
    check("1 ! ()", "", empty());
    check("() ! 1", "", empty());
    check("1 ! () ! 1", "", empty());
    check("1 ! void('x')", "", empty(Itr.class));
    check("1 ! void(.) ! 1", "", count(Itr.class, 1));
    check("<a/> ! <b/> ! ()", "", empty());

    check("void('x') ! 1", "", empty(Itr.class));
    check("() ! 'a'[.]", "", empty());
    check("() ! ('a', 'b')[.]", "", empty());
    check("() ! <_>a</_>[.]", "", empty());
  }

  /** Context item. */
  @Test public void context() {
    query("2 ! number()", 2);
    query("3 ! number(.)", 3);
    query("4 ! string()", "4");
    query("5 ! string(.)", "5");
    query("(1, 2) ! position()", "1\n2");
    query("(1, 2) ! last()", "2\n2");
    query("{} ! head(?_) ! string()", "");

    check("1 ! .", 1, root(Itr.class));
    check("(1, 2)[. = 1] ! .", 1, root(IterFilter.class));
    check("(1, (2, 3)[. = 2]) ! .", "1\n2", root(List.class));
    check("(1, 2) !.!.!.!.!.!.!.!.!.!.!.", "1\n2", root(RangeSeq.class));
    check("<a/> ! . ! .", "<a/>", root(CElem.class));
    check("(1, 2)[. ! number() = 2]", 2, empty(DualMap.class));

    check("trace(1) ! (. + 1)", 2, exists(Pipeline.class));
    check("<_>1</_>[. = 1] ! trace(.)", "<_>1</_>", exists(TRACE));
  }

  /** Typing. */
  @Test public void types() {
    check("(1, 2)[. != 0] ! .[. = 1]", 1, root(IterFilter.class));
    check("(1, 2)[. != 0] ! <_>{ . }</_>[. = 1]", "<_>1</_>", exists(DualMap.class));
    check("<_>1</_>[. = 1] ! 2", "2", type(DualMap.class, "xs:integer?"));
    check("<_>4</_>[. = 4] ! (4, 5)[. = 4]", 4, type(DualIterMap.class, "xs:integer*"));
  }

  /** Flatten nested operators. */
  @Test public void flatten() {
    check("(1, 2)[. != 0] ! ((. + .) ! (. * .))", "4\n16", count(Pipeline.class, 1));
    // do not rewrite positional access
    check("(1, 2)[. != 0] ! ((1 to .) ! position())", "1\n1\n2", count(CachedMap.class, 1));
  }

  /** Inline simple expressions into next operand. */
  @Test public void inline() {
    check("'1' ! (., number())", "1\n1", root(ItemSeq.class));
    check("let $a := document { <a/> } return $a ! (., /)", "<a/>\n<a/>", empty(VarRef.class));
    check("let $d := document{} return $d ! /", "", root(CDoc.class));
    check("{ 1: 2 } ! ?*", 2, root(Itr.class));
    check("let $n := { 1: 2 } return $n ! ?*", 2, root(Itr.class));
  }

  /** Errors. */
  @Test public void error() {
    error("(1 + 'a') ! 2", CALCTYPE_X_X_X_X_X);
    query("('a' || <?_ x?>)[. = 'x'] ! error()", "");
    query("('a' || <?_ x?>) ! () ! error()", "");
  }

  /** Replicate results. */
  @Test public void replicate() {
    check("<x/> ! (2, 3)[. = 2]", "2", empty(CElem.class));
    check("(1 to 2) ! ('a', 'a')[.]", "a\na\na\na", exists(REPLICATE));
    check("(1 to 2) ! (4, 5)[. = 4]", "4\n4", exists(REPLICATE));
    check("(1 to 2) ! ('a', '')[.]", "a\na", exists(REPLICATE));
    check("(1 to 2) ! <x/>", "<x/>\n<x/>", exists(REPLICATE));

    check("(1 to 2) ! void(.)", "", empty(REPLICATE));

    // replace first or both expressions with singleton sequence
    check("(1 to 2) ! 3", "3\n3", exists(SingletonSeq.class), root(SingletonSeq.class));
    check("(1 to 2) ! 'a'[.]", "a\na", exists(SingletonSeq.class), root(SingletonSeq.class));

    // combine identical values in singleton sequence
    check("(1 to 2) ! ('a', 'a')", "a\na\na\na", exists(SingletonSeq.class) + " and .//@size = 4");
    check("(1 to 2) !" + REPLICATE.args("a", 2) + " !" + REPLICATE.args("a", 2),
        "a\na\na\na\na\na\na\na", exists(SingletonSeq.class) + " and .//@size = 8");
  }

  /** Positional access. */
  @Test public void positional() {
    check("for $i in 2 to 3 return (1 to 4)[$i]", "2\n3", root(RangeSeq.class));
    check("(2 to 3) !" + ITEMS_AT.args(" 1 to 4", " ."), "2\n3", root(RangeSeq.class));
  }

  /** Inline sequences. */
  @Test public void inlineSequences() {
    check("(<a/>, <b/>) ! data()", "\n", root(DATA));
    check("(<a/>, <b/>) ! data(.)", "\n", root(DATA));
  }

  /** XQuery: Unroll simple map expressions. */
  @Test public void gh1994() {
    // do not unroll
    check("(1 to 6) ! (. * 2)", "2\n4\n6\n8\n10\n12", root(DualMap.class));

    // unroll expression
    unroll(true);
    check("(1, 2) ! (. * 2)", "2\n4", root(BytSeq.class));
    check("(true(), false()) ! (. = true())", "true\nfalse", root(BlnSeq.class));
  }

  /** Self steps on generalized nodes must not be dropped. */
  @Test public void generalizedNodes() {
    query("count((jtree({ 'a': 1 }), <x/>) ! (self::xnode()))", 1);
    query("count((jtree({ 'a': 1 }), <x/>) ! (self::jnode()))", 1);
    query("count((jtree({ 'a': 1 }), <x/>) ! (self::node()))", 2);
    query("count(<x/> ! (self::xnode()))", 1);
    query("count({ 'a': 1 } ! (self::node()))", 1);
    // self steps must not be dropped if the context value is no node
    error("count((1, 2) ! (self::xnode()))", PATHNODE_X_X_X);
    error("count((1, 2) ! (self::node()))", PATHNODE_X_X_X);
  }

  /** Unrolled operands must not share the mapped expression. */
  @Test public void unrolledOperands() {
    inline(true);
    unroll(true);
    // list with empty operands: number of items is smaller than number of operands
    check("declare function local:f($a as xs:string?, $b as xs:string?, $c as xs:string?, " +
        "$d as xs:string?, $e as xs:string?, $f as xs:string?) { " +
        "  ($a, $b, $c, $d, $e, $f) ! ('[' || . || ']') };" +
        "local:f('A', (), 'C', 'D', (), ())", "[A]\n[C]\n[D]");
  }

  /** Maps over nested nodes must not be merged to sorted paths. */
  @Test public void nestedNodes() {
    query("<a><b><c/></b><d/></a>/descendant-or-self::node() ! * ! name()", "b\nd\nc");
    query("jtree([[1, 2], [3]])/descendant-or-self::* ! * !"
        + "serialize(jvalue(), { 'method': 'json' })", "[1,2]\n[3]\n1\n2\n3");
    check("<a><b/></a>/* ! *", "", root(IterPath.class));

    // order is irrelevant: merge maps to paths
    final String nested = "<a><b><c/></b><d/></a>/descendant-or-self::node()";
    check(COUNT.args(" " + nested + " ! *"), 3, empty(SimpleMap.class));
    check(EXISTS.args(" " + nested + " ! d"), true, empty(SimpleMap.class));
    check(COUNT.args(" " + nested + " ! .."), 3, exists(SimpleMap.class));
  }

  /** Maps over FLWOR results are moved to the return clause. */
  @Test public void flworReturn() {
    final String let = "let $s := (1 to 6)[. > <_>2</_>] where count($s) > 1 return $s";
    check("(" + let + ") ! (. * 2)", "6\n8\n10\n12", root(GFLWOR.class));
    check("(" + let + ") ! position()", "1\n2\n3\n4", root(GFLWOR.class));
    check("(for $i in 1 to <_>2</_> for $j in 1 to $i return $j) ! position()", "1\n2\n3",
        root(SimpleMap.class));
    check("(for $i in (3, 1)[. > <_>0</_>] for $j in (1, 2) order by $i return $i + $j) ! " +
        "(. * 10)", "20\n30\n40\n50", root(GFLWOR.class));
    check("(let $s := (1 to 6)[. > <_>2</_>] return " + JTREE.args(" $s") + ") ! " +
        JVALUE.args(), "3\n4\n5\n6", empty(JTREE), empty(JVALUE));
  }

  /** Maps over conditional expressions with an empty branch are moved into the other branch. */
  @Test public void ifBranch() {
    final String seq = "(1 to 3)[. > <_>1</_>]", cond = wrap("a") + " = 'a'";
    check("(if(" + cond + ") then " + seq + " else ()) ! (. * 2)", "4\n6", root(If.class));
    check("(if(" + cond + ") then () else " + seq + ") ! (. * 2)", "", root(If.class));
    check("(if(" + cond + ") then " + seq + " else ()) ! position()", "1\n2", root(If.class));
    check("(if(" + cond + ") then " + seq + " else error()) ! (. * 2)", "4\n6", root(If.class));
    // both branches non-empty: no rewrite
    check("(if(" + cond + ") then " + seq + " else 0) ! (. * 2)", "4\n6",
        root(SimpleMap.class));
  }

  /** Single context item: the value of the right operand is accessed directly. */
  @Test public void singleItem() {
    final String query = "let $n := <_>10</_> let $r := " +
        "({ 'a': (1 to $n) ! (. * 2) }, { 'a': (1 to $n) ! (. * 3) }) for $c in 1 to 3 return ";
    final String plan = exists(DualIterMap.class);
    check(query + "$r[$c]?a[5]", "10\n15", plan);
    check(query + "count($r[$c]?a)", "10\n10\n0", plan);
    check(query + FOOT.args(" $r[$c]?a"), "20\n30", plan);
  }
}
