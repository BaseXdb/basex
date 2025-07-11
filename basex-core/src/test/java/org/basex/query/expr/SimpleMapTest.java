package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.var.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the simple map operator.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SimpleMapTest extends SandboxTest {
  /** Resets optimizations. */
  @AfterEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Basic tests. */
  @Test public void basic() {
    query("1 ! 2", 2);
    query("1 ! (1 to 2)", "1\n2");
    query("(1 to 2) ! 3", "3\n3");
    query("(1 to 2) ! (3 to 4)", "3\n4\n3\n4");

    query("(1 to 2) ! <a/>", "<a/>\n<a/>");
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
    check("'1' ! (., number())", "1\n1", root(SmallSeq.class));
    check("let $a := document { <a/> } return $a ! (., /)", "<a/>\n<a/>", empty(VarRef.class));
    check("let $d := document{} return $d ! /", "", root(CDoc.class));
    check("{ 1: 2 } ! ?*", 2, root(Itr.class));
    check("let $n := { 1: 2 } return $n ! ?*", 2, root(Itr.class));
  }

  /** Errors. */
  @Test public void error() {
    error("(1 + 'a') ! 2", NONUMBER_X_X);
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
    check("(1, 2) ! (. * 2)", "2\n4", root(IntSeq.class));
    check("(true(), false()) ! (. = true())", "true\nfalse", root(BlnSeq.class));
  }
}
