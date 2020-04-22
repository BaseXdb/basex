package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.junit.Test;

/**
 * Tests for the simple map operator.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class SimpleMapTest extends QueryPlanTest {
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
    check("1 ! prof:void('x')", "", empty(Int.class));
    check("1 ! prof:void(.) ! 1", "", count(Int.class, 1));
    check("<a/> ! <b/> ! ()", "", empty());

    check("prof:void('x') ! 1", "", empty(Int.class));
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
    query("map {} ! head(?_) ! string()", "");

    check("1 ! .", 1, empty(IterMap.class));
    check("(1, 2)[. = 1] ! .", 1, empty(IterMap.class));
    check("(1, (2, 3)[. = 2]) ! .", "1\n2", empty(IterMap.class));
    check("(1, 2) !.!.!.!.!.!.!.!.!.!.!.", "1\n2", empty(IterMap.class));
    check("<a/> ! . ! .", "<a/>", empty(IterMap.class));
    check("(1, 2)[. ! number() = 2]", 2, empty("*[name() = 'IterMap']"));

    check("trace(1) ! (. + 1)", 2, exists(ItemMap.class));
    check("<_>1</_>[. = 1] ! trace(.)", "<_>1</_>", exists(TRACE));
  }

  /** Typing. */
  @Test public void types() {
    check("(1, 2) ! .[. = 1]", 1, root(IterFilter.class));
    check("(1, 2) ! <_>{ . }</_>[. = 1]", "<_>1</_>", exists(IterMap.class));
    check("<_>1</_>[. = 1] ! 2", "2", type(ItemMap.class, "xs:integer?"));
    check("<_>4</_>[. = 4] ! (4, 5)[. = 4]", 4, type(IterMap.class, "xs:integer*"));
  }

  /** Flatten nested operators. */
  @Test public void flatten() {
    check("(1, 2) ! ((. + .) ! (. * .))", "4\n16", count(IterMap.class, 1));
    // do not rewrite positional access
    check("(1, 2) ! ((1 to .) ! position())", "1\n1\n2", count(CachedMap.class, 1));
  }

  /** Inline simple expressions into next operand. */
  @Test public void inline() {
    check("'1' ! (., number())", "1\n1", empty(IterMap.class));
    check("let $a := document { <a/> } return $a ! (., /)", "<a/>\n<a/>", count(VarRef.class, 2));
    check("let $d := document{} return $d ! /", "", root(CDoc.class));
    check("map { 1: 2 } ! ?*", 2, root(Int.class));
    check("let $n := map { 1: 2 } return $n ! ?*", 2, root(Int.class));
  }

  /** Errors. */
  @Test public void error() {
    error("(1 + 'a') ! 2", NONUMBER_X_X);
  }

  /** Replicate results. */
  @Test public void replicate() {
    check("<x/> ! (2, 3)[. = 2]", "2", empty(CElem.class));
    check("(1 to 2) ! ('a', 'a')[.]", "a\na\na\na", exists(_UTIL_REPLICATE));
    check("(1 to 2) ! (4, 5)[. = 4]", "4\n4", exists(_UTIL_REPLICATE));
    check("(1 to 2) ! ('a', '')[.]", "a\na", exists(_UTIL_REPLICATE));
    check("(1 to 2) ! prof:void(.)", "", empty(_UTIL_REPLICATE));

    // replace first or both expressions with singleton sequence
    check("(1 to 2) ! 3", "3\n3", exists(SingletonSeq.class), empty(IterMap.class));
    check("(1 to 2) ! 'a'[.]", "a\na", exists(SingletonSeq.class), empty(IterMap.class));
    check("(1 to 2) ! <x/>", "<x/>\n<x/>", exists(SingletonSeq.class), exists(IterMap.class));

    // combine identical values in singleton sequence
    check("(1 to 2) ! ('a', 'a')", "a\na\na\na", exists(SingletonSeq.class) + " and .//@size = 4");
    check("(1 to 2) ! util:replicate('a', 2) ! util:replicate('a', 2)", "a\na\na\na\na\na\na\na",
        exists(SingletonSeq.class) + " and .//@size = 8");
  }
}
