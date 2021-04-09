package org.basex.query.ast;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.core.cmd.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.index.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.up.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.var.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Checks query rewritings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RewritingsTest extends QueryPlanTest {
  /** Input file. */
  private static final String FILE = "src/test/resources/input.xml";

  /** Drops the database, resets optimizations. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
    inline(false);
    unroll(false);
  }

  /** Checks if the count function is pre-compiled. */
  @Test public void preEval() {
    check("count(1)", 1, exists(Int.class));

    execute(new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a><a/></xml>"));
    check("count(//a)", 3, exists(Int.class));
    check("count(/xml/a)", 3, exists(Int.class));
    check("count(//text())", 2, exists(Int.class));
    check("count(//*)", 4, exists(Int.class));
    check("count(//node())", 6, exists(Int.class));
    check("count(//comment())", 0, exists(Int.class));
    check("count(/self::document-node())", 1, exists(Int.class));
  }

  /** Checks if descendant-or-self::node() steps are rewritten. */
  @Test public void mergeDesc() {
    execute(new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>"));

    check("//*", null, "//@axis = 'descendant'");
    check("//(b, *)", null, exists(IterPath.class), "//@axis = 'descendant'");
    check("//(b | *)", null, exists(IterPath.class), "//@axis = 'descendant'");
    check("//(b | *)[text()]", null, exists(IterPath.class), empty(Union.class),
        "//@axis = 'descendant'");
    check("//(b, *)[1]", null, "not(//@axis = 'descendant')");
  }

  /** Checks if descendant steps are rewritten to child steps. */
  @Test public void descToChild() {
    execute(new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>"));

    check("descendant::a", null, "//@axis = 'child'");
    check("descendant::b", null, "//@axis = 'child'");
    check("descendant::c", null, "//@axis = 'child'");
    check("descendant::*", null, "not(//@axis = 'child')");
  }

  /** Checks EBV optimizations. */
  @Test public void optimizeEbv() {
    query("not(<a/>[b])", true);
    query("empty(<a/>[b])", true);
    query("exists(<a/>[b])", false);

    query("not(<a/>[b = 'c'])", true);
    query("empty(<a/>[b = 'c'])", true);
    query("exists(<a/>[b = 'c'])", false);

    query("let $n := <n/> where $n[<a><b/><b/></a>/*] return $n", "<n/>");

    check("empty(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("exists(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("boolean(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("not(<a>X</a>[text()])", null, "//@axis = 'child'");

    check("if(<a>X</a>[text()]) then 1 else 2", null, "//@axis = 'child'");
    check("<a>X</a>[text()] and <a/>", null, "//@axis = 'child'");
    check("<a>X</a>[text()] or <a/>", null, "//Bln = 'true'");
    check("<a>X</a>[text()] or <a/>[text()]", null, "//@axis = 'child'");
    check("for $a in <a>X</a> where $a[text()] return $a", null, "//@axis = 'child'");

    check("empty(<a>X</a>/.[text()])", null, "//@axis = 'child'");
  }

  /** Checks if iterative evaluation of XPaths is used if no duplicates occur. */
  @Test public void gh1001() {
    execute(new CreateDB(NAME, "<a id='0' x:id='' x='' xmlns:x='x'><b id='1'/><c id='2'/>"
        + "<d id='3'/><e id='4'/></a>"));
    check("(/a/*/../*) ! name()", "b\nc\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a/b)/../*) ! name()", "b\nc\nd\ne", exists(IterPath.class));
    check("(/a/*/following::*) ! name()", "c\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a/b)/following::*) ! name()", "c\nd\ne", exists(IterPath.class));
    check("(/a/*/following-sibling::*) ! name()", "c\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a/b)/following-sibling::*) ! name()", "c\nd\ne", exists(IterPath.class));
    check("(/*/@id/../*) ! name()", "b\nc\nd\ne", empty(IterPath.class));
    check("(exactly-one(/a)/@id/../*) ! name()", "b\nc\nd\ne", exists(IterPath.class));
  }

  /** Checks OR optimizations. */
  @Test public void or() {
    check("('' or '')", false, empty(Or.class));
    check("('x' or 'x' = 'x')", true, empty(Or.class));
    check("(false()   or <x/> = 'x')", false, empty(Or.class));
    check("(true()    or <x/> = 'x')", true, empty(Or.class));
    check("('x' = 'x' or <x/> = 'x')", true, empty(Or.class));

    // {@link CmpG} rewritings
    check("let $x := <x/>     return ($x = 'x' or $x = 'y')", false, empty(Or.class));
    check("let $x := <x>x</x> return ($x = 'x' or $x = 'y')", true,  empty(Or.class));
  }

  /** Checks AND optimizations. */
  @Test public void and() {
    check("('x' and 'y')", true, empty(And.class));
    check("('x' and 'x' = 'x')", true, empty(And.class));
    check("(true()    and <x>x</x> = 'x')", true, empty(And.class));
    check("(false()   and <x>x</x> = 'x')", false, empty(And.class));
    check("('x' = 'x' and <x>x</x> = 'x')", true, empty(And.class));
  }

  /** Checks {@link CmpIR} optimizations. */
  @Test public void cmpIR() {
    final Class<CmpIR> cmpir = CmpIR.class;
    check("(1, 2)[. = 1] = 1 to 2", true, exists(cmpir));
    check("(1, 2)[. = 3] = 1 to 2", false, exists(cmpir));
    check("(1, 2)[. = 3] = 1 to 2", false, exists(cmpir));

    // do not rewrite equality comparisons against single integers
    check("(1, 2)[. = 1] = 1", true, empty(cmpir));

    // rewrite to positional test
    check("(1 to 5)[let $p := position() return $p = 2]", 2,
        empty(cmpir), empty(Let.class), empty(POSITION));
    check("1[let $p := position() return $p = 0]", "", empty());
    check("1[let $p := position() return $p = (-5 to -1)]", "", empty());
  }

  /** Checks {@link CmpR} optimizations. */
  @Test public void cmpR() {
    final Class<CmpR> cmpr = CmpR.class;
    check("<a>5</a>[text() > 1 and text() < 9]", "<a>5</a>", count(cmpr, 1));
    check("<a>5</a>[text() > 1 and text() < 9 and <b/>]", "<a>5</a>", count(cmpr, 1));
    check("<a>5</a>[text() > 1 and . < 9]", "<a>5</a>", count(cmpr, 2));

    // GH-1744
    check("<a>5</a>[text() < 5 or text() > 5]", "", count(cmpr, 2));
    check("<a>5</a>[text() > 5 or text() < 5]", "", count(cmpr, 2));
    check("<a>5</a>[5 > text() or 5 < text()]", "", count(cmpr, 2));
    check("<a>5</a>[5 < text() or 5 > text()]", "", count(cmpr, 2));

    check("<a>5</a>[text() > 800000000]", "", exists(cmpr));
    check("<a>5</a>[text() < -800000000]", "", exists(cmpr));
    check("<a>5</a>[text() <= -800000000]", "", exists(cmpr));
    check("exists(<x>1234567890.12345678</x>[. = 1234567890.1234567])", true, empty(cmpr));

    check("exists(<x>123456789012345678</x> [. = 123456789012345679])", true, empty(cmpr));
    check("<a>5</a>[text() > 8000000000000000000]", "", empty(cmpr));
    check("<a>5</a>[text() < -8000000000000000000]", "", empty(cmpr));
    check("(1, 1234567890.12345678)[. = 1234567890.1234567]", "", empty(cmpr));
    check("(1, 123456789012345678 )[. = 123456789012345679]", "", empty(cmpr));

    // rewrite equality comparisons
    check("(0, 1)[. = 1] >= 1.0", true, exists(cmpr));
    check("(0, 1)[. = 1] >= 1e0", true, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1", true, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1e0", true, exists(cmpr));
    check("<_>1.1</_> >= 1.1", true, exists(cmpr));

    // do not rewrite decimal/double comparisons
    check("(0e0, 1e0)[. = 1] >= 1.0", true, empty(cmpr));
    check("(0.0, 1.0)[. = 1] >= 1e0", true, empty(cmpr));

    // do not rewrite equality comparisons
    check("(0, 1)[. = 1] = 1.0", true, empty(cmpr));
    check("(0, 1)[. = 1] = 1e0", true, empty(cmpr));
    check("(0e0, 1e0)[. = 1] = 1", true, empty(cmpr));
    check("(0e0, 1e0)[. = 1] = 1.0", true, empty(cmpr));
    check("(0e0, 1e0)[. = 1] = 1e0", true, empty(cmpr));
    check("<_>1.1</_> = 1.1", true, empty(cmpr));

    // suppressed rewritings
    check("random:double() = 2", false, empty(cmpr));
    check("(0.1, 1.1)[. != 0] = 1.3", false, empty(cmpr));
    check("('x', 'y')[. = 'x'] = 'x'", true, empty(cmpr));
    check("('x', 'x')[. != 'x'] = 1.3", false, empty(cmpr));

    check("(0.1, 1.1)[. = 1.1] = 1.1", true, empty(cmpr));

    // rewrite to positional test
    check("1[let $p := position() return $p = 0.0]", "", empty());
  }

  /** Checks {@link CmpSR} optimizations. */
  @Test public void cmpSR() {
    check("<a>5</a>[text() > '1' and text() < '9']", "<a>5</a>", count(CmpSR.class, 1));
    check("<a>5</a>[text() > '1' and text() < '9' and <b/>]", "<a>5</a>", count(CmpSR.class, 1));
    check("<a>5</a>[text() > '1' and . < '9']", "<a>5</a>", count(CmpSR.class, 2));
  }

  /** Checks string-length optimizations. */
  @Test public void stringLength() {
    check("<a/>[string-length() >  -1]", "<a/>", empty(IterFilter.class));
    check("<a/>[string-length() != -1]", "<a/>", empty(IterFilter.class));
    check("<a/>[string-length() ge  0]", "<a/>", empty(IterFilter.class));
    check("<a/>[string-length() ne 1.1]", "<a/>", empty(IterFilter.class));

    check("<a/>[string-length() <   0]", "", empty(IterFilter.class));
    check("<a/>[string-length() <= -1]", "", empty(IterFilter.class));
    check("<a/>[string-length() eq -1]", "", empty(IterFilter.class));
    check("<a/>[string-length() eq 1.1]", "", empty(IterFilter.class));

    check("<a/>[string-length() >  0]", "", exists(SingleIterPath.class));
    check("<a/>[string-length() >= 0.5]", "", exists(SingleIterPath.class));
    check("<a/>[string-length() ne 0]", "", exists(SingleIterPath.class));

    check("<a/>[string-length() <  0.5]", "<a/>", exists(SingleIterPath.class));
    check("<a/>[string-length() <= 0.5]", "<a/>", exists(SingleIterPath.class));
    check("<a/>[string-length() eq 0]", "<a/>", exists(SingleIterPath.class));

    check("<a/>[string-length() gt 1]", "", exists(STRING_LENGTH));
    check("<a/>[string-length() = <a>1</a>]", "", exists(STRING_LENGTH));
  }

  /** Checks count optimizations. */
  @Test public void count() {
    // static occurrence: zero-or-one
    String count = "count(<_>1</_>[. = 1])";

    // static result: no need to evaluate count
    check(count + " <    0", false, root(Bln.class));
    check(count + " <= -.1", false, root(Bln.class));
    check(count + " >=   0", true, root(Bln.class));
    check(count + " > -0.1", true, root(Bln.class));
    check(count + " =  1.1", false, root(Bln.class));
    check(count + " != 1.1", true, root(Bln.class));
    check(count + " =   -1", false, root(Bln.class));
    check(count + " !=  -1", true, root(Bln.class));

    // rewrite to empty/exists (faster)
    check(count + " >  0", true, root(CmpSimpleG.class));
    check(count + " >= 1", true, root(CmpSimpleG.class));
    check(count + " != 0", true, root(CmpSimpleG.class));
    check(count + " <  1", false, root(CmpSimpleG.class));
    check(count + " <= 0", false, root(CmpSimpleG.class));
    check(count + " =  0", false, root(CmpSimpleG.class));

    // zero-or-one result: no need to evaluate count
    check(count + " <  2", true, root(Bln.class));
    check(count + " <= 2", true, root(Bln.class));
    check(count + " <= 1", true, root(Bln.class));
    check(count + " != 2", true, root(Bln.class));
    check(count + " >  1", false, root(Bln.class));
    check(count + " >= 2", false, root(Bln.class));
    check(count + " =  2", false, root(Bln.class));

    // no pre-evaluation possible
    check(count + " != 1", false, root(CmpSimpleG.class));
    check(count + " =  1", true, root(CmpSimpleG.class));
    check(count + " - 1 = 0", true, root(CmpSimpleG.class));

    // one-or-more results: no need to evaluate count
    count = "count((1, <_>1</_>[. = 1]))";
    check(count + " >  0", true, root(Bln.class));
    check(count + " >= 1", true, root(Bln.class));
    check(count + " != 0", true, root(Bln.class));
    check(count + " <  1", false, root(Bln.class));
    check(count + " <= 0", false, root(Bln.class));
    check(count + " =  0", false, root(Bln.class));
    check(count + " =  1.1", false, root(Bln.class));

    // no pre-evaluation possible
    check(count + " != 1", true, exists(COUNT));
    check(count + " =  1", false, root(_UTIL_WITHIN));
    check(count + " =  2", true, root(_UTIL_WITHIN));
    check(count + " div 2 = 1", true, root(_UTIL_WITHIN));
  }

  /** Checks that empty sequences are eliminated and that singleton lists are flattened. */
  @Test public void list() {
    check("((), <x/>, ())", "<x/>", empty(List.class), empty(Empty.class), exists(CElem.class));
  }

  /** Checks that expressions marked as non-deterministic will not be rewritten. */
  @Test public void nonDeterministic() {
    check("count((# basex:non-deterministic #) { <x/> })", 1, exists(COUNT));
  }

  /** Ensures that fn:doc with URLs will not be rewritten. */
  @Test public void doc() {
    check("<a>{ doc('" + FILE + "') }</a>//x", "", exists(DBNode.class));
    check("if(<x>1</x> = 1) then 2 else doc('" + FILE + "')", 2, exists(DBNode.class));
    check("if(<x>1</x> = 1) then 2 else doc('http://abc.de/')", 2, exists(DOC));
    check("if(<x>1</x> = 1) then 2 else collection('http://abc.de/')", 2, exists(COLLECTION));
  }

  /** Positional predicates. */
  @Test public void pos() {
    // check if positional predicates are pre-evaluated
    check("'a'[1]", "a", exists(Str.class));
    check("'a'[position() = 1]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() = 1 to 2]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() > 0]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() < 2]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() >= 1]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() <= 1]", "a", "exists(QueryPlan/Str)");

    // check if positional predicates are rewritten to utility functions
    check("for $i in (1, 2) return 'a'[$i]", "a", root(Str.class));
    check("for $i in (1, 2) return 'a'[position() = $i]", "a", root(Str.class));
    check("for $i in (1, 2) return 'a'[position() = $i to $i]", "a", root(Str.class));

    check("for $i in (1, 2)[. > 0] return 9[position() = $i to $i + 1]", 9, exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() = $i to 1]", 9, exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() >= $i]", 9, exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() > $i]", "", exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() <= $i]", "9\n9", exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() < $i]", 9, exists(_UTIL_RANGE));

    // check if positional predicates are rewritten to utility functions
    final String seq = " (1, 1.1, 1.9, 2, 2.1, 2.2, 2.1, 2.2) ";
    check("for $i in" + seq + "return ('a', 'b')[$i]",
        "a\nb", exists(_UTIL_ITEM));
    check("for $i in" + seq + "return ('a', 'b')[position() = $i]",
        "a\nb", exists(_UTIL_ITEM));
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i]",
        "a\nb\nb\nb\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() > $i]",
        "b\nb\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() <= $i]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb\na\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb", exists(_UTIL_RANGE));

    // check if multiple positional predicates are rewritten to utility functions
    check("for $i in" + seq + "return ('a', 'b')[$i][$i]",
        "a", count(_UTIL_ITEM, 2));
    check("for $i in" + seq + "return ('a', 'b')[position() = $i][position() = $i]",
        "a", count(_UTIL_ITEM, 2));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i][position() < $i]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb", count(_UTIL_RANGE, 2));

    // check if positional predicates are merged and rewritten to utility functions
    check("for $i in" + seq + "return ('a', 'b')[position() = $i and position() = $i]", "a\nb",
        exists(_UTIL_ITEM));
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i and position() <= $i]", "a\nb",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() <= $i and position() >= $i]",
        "a\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() > $i and position() < $i]",
        "", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i and position() > $i]",
        "", exists(_UTIL_RANGE));

    // no rewriting possible (conflicting positional predicates)
    check("for $i in" + seq + "return ('a', 'b')[position() = $i and position() = $i + 1]",
        "", exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i and position() > $i]",
        "b\nb\nb", exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i and position() >= $i + 1]",
        "b", exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i and position() < $i + 1]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb", exists(CachedFilter.class));

    check("(<a/>, <b/>)[last()]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[last()]",
        "<b/>", count(_UTIL_LAST, 1));
    check("(<a/>, <b/>)[position() > 1 and position() < 3]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[position() > 1 and position() < 3]",
        "<b/>", root(IterFilter.class));
    check("(<a/>[. = ''], <b/>)[position() > 1 and position() < 3]",
        "<b/>", count(_UTIL_ITEM, 1));
    check("(<a/>, <b/>)[position() > 1 and position() < 3 and <b/>]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[position() > 1 and position() < 3 and <b/>]",
        "<b/>", root(IterFilter.class));
    check("(<a/>[. = ''], <b/>)[position() > 1 and position() < 3 and <b/>]",
        "<b/>", count(_UTIL_ITEM, 1));
    check("(<a/>, <b/>)[position() > 1 and position() < 4]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[position() > 1 and position() < 4]",
        "<b/>", count(_UTIL_RANGE, 1));
  }

  /** Predicates. */
  @Test public void preds() {
    // context value: rewrite if root is of type string or node
    check("('s', 't')[.]", "s\nt", exists(ContextValue.class));
    check("<a/>[.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>[.][.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("('a', 'b')[. ! position()]", "a", exists("*[contains(name(), 'Map')]"));
    check("(1, 0)[.]", 1, exists(ContextValue.class));
    error("true#0[.]", EBV_X_X);
    error("(true#0, false#0)[.]", EBV_X_X);

    // map expression
    check("'s'['s' ! <a/>]", "s", root(Str.class));
    check("'s'['s' ! <a/>]", "s", root(Str.class));
    check("'s'['x' ! <a/> ! <b/>]", "s", root(Str.class));
    check("'s'['x' ! (<a/>, <b/>) ! <b/>]", "s", root(Str.class));
    check("'s'['x' ! <a>{ . }</a>[. = 'x']]", "s", root(If.class));

    // path expression
    check("let $a := <a/> return $a[$a/self::a]", "<a/>", empty(VarRef.class));
    check("let $a := <a/> return $a[$a]", "<a/>", empty(VarRef.class));
  }

  /** Comparison expressions. */
  @Test public void cmpG() {
    check("count(let $s := (-1, 1 to 99999) return $s[. = $s])", 100000, exists(CmpHashG.class));
  }

  /** Count, big sequences. */
  @Test public void gh1519() {
    query("declare function local:replicate($seq, $n, $out) { "
        + "  if($n eq 0) then $out "
        + "  else ( "
        + "    let $out2 := if($n mod 2 eq 0) then $out else ($out, $seq) "
        + "    return local:replicate(($seq, $seq), $n idiv 2, $out2) "
        + "  ) "
        + "};"
        + "let $n := 1000000 "
        + "return ( "
        + "  count(local:replicate((1, 2, 3), $n, ())) eq 3 * $n, "
        + "  count(local:replicate((1, 2, 3), $n, ())) = 3 * $n "
        + ")",
        "true\ntrue");
  }

  /** Checks simplification of empty path expressions. */
  @Test public void gh1587() {
    check("document {}/..", "", empty(CDoc.class));
    check("function() { document {}/.. }()", "", empty(CDoc.class));
    check("declare function local:f() { document {}/.. }; local:f()", "", empty(CDoc.class));
  }

  /**
   * Remove redundant self steps.
   */
  @Test public void selfSteps() {
    check("<a/>/.", "<a/>", root(CElem.class));
    check("<a/>/./././.", "<a/>", root(CElem.class));
    check("<a/>[.]", "<a/>", root(CElem.class));
    check("<a/>/self::element()", "<a/>", root(CElem.class));
    check("attribute a { 0 }/self::attribute()", "a=\"0\"", root(CAttr.class));
    check("<a/>/self::*", "<a/>", root(CElem.class));
  }

  /** Static optimizations of paths without results (see also gh1630). */
  @Test public void emptyPath() {
    // check combination of axis and node test and axis
    check("<e a='A'/>/attribute::text()", "", empty());
    check("<e a='A'/>/attribute::attribute()", "a=\"A\"", exists(IterPath.class));
    check("<e a='A'/>/ancestor::text()", "", empty());
    check("<e a='A'/>/parent::text()", "", empty());
    check("<e a='A'/>/parent::*", "", exists(IterPath.class));
    check("attribute a { 0 }/child::attribute()", "", empty());
    check("<e a='A'/>/attribute::a/child::attribute()", "", empty());

    // check step after expression that yields document nodes
    check("document { <a/> }/self::*", "", empty());
    check("document { <a/> }/self::*", "", empty());
    check("document { <a/> }/self::text()", "", empty());

    check("document { <a/> }/child::document-node()", "", empty());
    check("document { <a/> }/child::attribute()", "", empty());
    check("document { <a/> }/child::*", "<a/>", exists(IterPath.class));

    check("document { <a/> }/descendant-or-self::attribute()", "", empty());
    check("document { <a/> }/parent::node()", "", empty());
    check("document { <a/> }/ancestor::node()", "", empty());
    check("document { <a/> }/following::node()", "", empty());
    check("document { <a/> }/preceding-sibling::node()", "", empty());

    // skip further tests if previous node type is unknown, or if current test accepts all nodes
    check("(<a/>, <_>1</_>[. = 0])/node()", "", exists(IterStep.class));

    // check step after any other expression
    check("<a/>/self::text()", "", empty());
    check("comment {}/child::node()", "", empty());
    check("text { 0 }/child::node()", "", empty());
    check("attribute a { 0 }/following-sibling::node()", "", empty());
    check("attribute a { 0 }/preceding-sibling::node()", "", empty());
    check("comment { }/following-sibling::node()", "", exists(IterPath.class));
    check("comment { }/preceding-sibling::node()", "", exists(IterStep.class));

    check("attribute a { 0 }/child::node()", "", empty());
    check("attribute a { 0 }/descendant::*", "", empty());
    check("attribute a { 0 }/self::*", "", empty());

    // namespaces
    check("(<a/>, comment{})/child::namespace-node()", "", empty());
    check("(<a/>, comment{})/descendant::namespace-node()", "", empty());
    check("(<a/>, comment{})/attribute::namespace-node()", "", empty());
    check("(<a/>, comment{})/self::namespace-node()", "", exists(IterStep.class));
    check("(<a/>, comment{})/descendant-or-self::namespace-node()", "", exists(IterStep.class));
  }

  /** Casts. */
  @Test public void gh1795() {
    check("for $n in 1 to 3 return xs:integer($n)[. = 1]", 1, empty(Cast.class));
    check("('a', 'b') ! xs:string(.)", "a\nb", empty(Cast.class), root(StrSeq.class));
    check("xs:string(''[. = <_/>])", "", empty(Cast.class), root(If.class));

    check("xs:string(<_/>[. = '']) = ''", true, empty(Cast.class), root(CmpSimpleG.class));
    check("xs:double(<_>1</_>) + 2", 3, empty(Cast.class), type(Arith.class, "xs:double"));
    check("(1, 2)[. != 0] ! (xs:byte(.)) ! (xs:integer(.) + 2)", "3\n4",
        count(Cast.class, 1), type(Arith.class, "xs:integer"));

    error("(if(<_>!</_> = 'a') then 'b') cast as xs:string", INVTYPE_X_X_X);
    error("((1 to 1000000000) ! (. || 'x')) cast as xs:string", INVTYPE_X_X_X);
    error("() cast as xs:string", INVTYPE_X_X_X);
    error("(1 to 100000)[. < 3] cast as xs:integer", INVTYPE_X_X_X);
  }

  /** Type promotions. */
  @Test public void gh1801() {
    check("map { xs:string(<_/>): '' } instance of map(xs:string, xs:string)", true);
    check("map { string(<_/>): '' } instance of map(xs:string, xs:string)", true);
  }

  /** Type checks. */
  @Test public void typeCheck() {
    inline(true);
    check("declare function local:a($e) as xs:string? { local:b($e) }; " +
        "declare function local:b($e) as xs:string? { $e }; local:a(<_>X</_>)", "X",
        count(TypeCheck.class, 1));
    check("declare function local:a($e) as xs:string? { local:b($e) }; " +
        "declare function local:b($e) as xs:string* { $e }; local:a(<_>X</_>)", "X",
        count(TypeCheck.class, 1));
    check("declare function local:a($e) as xs:string* { local:b($e) }; " +
        "declare function local:b($e) as xs:string? { $e }; local:a(<_>X</_>)", "X",
        count(TypeCheck.class, 1));

    query("declare function local:f() as item()  { data([ <_/> ]) }; local:f()", "");
    query("declare function local:f() as item()? { data([ <_/> ]) }; local:f()", "");
    query("declare function local:f() as item()+ { data([ <_/> ]) }; local:f()", "");
    query("declare function local:f() as item()* { data([ <_/> ]) }; local:f()", "");

    query("declare function local:f($a) as item()  { data($a) }; local:f(<_/>)", "");
    query("declare function local:f($a) as item()? { data($a) }; local:f(<_/>)", "");
    query("declare function local:f($a) as item()+ { data($a) }; local:f(<_/>)", "");
    query("declare function local:f($a) as item()* { data($a) }; local:f(<_/>)", "");
  }

  /** Test. */
  @Test public void gh1694() {
    check("count(1)", 1, exists(Int.class));
    execute(new CreateDB(NAME, "<_/>"));
    final String query =
      "declare function local:b($e) as xs:string { $e };\n" +
      "declare function local:a($db) {\n" +
      "  let $ids := local:b(db:open($db))\n" +
      "  return db:open('" + NAME + "')[*[1] = $ids]\n" +
      "};\n" +
      "local:a('" + NAME + "')";

    query(query, "<_/>");
  }

  /** Test. */
  @Test public void gh1726() {
    final String query =
      "let $xml := if((1, 0)[.]) then ( " +
      "  element a { element b { } update { } } " +
      ") else ( " +
      "  error() " +
      ") " +
      "let $b := $xml/* " +
      "return ($b, $b/..)";

    query(query, "<b/>\n<a>\n<b/>\n</a>");
  }

  /** Test. */
  @Test public void gh1723() {
    check("count(<a/>)", 1, root(Int.class));
    check("count(<a/>/<b/>)", 1, root(Int.class));
    check("count(<a/>/<b/>/<c/>/<d/>)", 1, root(Int.class));
  }

  /** Test. */
  @Test public void gh1733() {
    check("(<a/>, <b/>)/1", "1\n1", root(SingletonSeq.class));
    check("<a/>/1", 1, root(Int.class));
    check("<a/>/<a/>", "<a/>", root(CElem.class));
    check("(<a/>/map { 1:2 })?1", 2, root(Int.class));
    check("<a/>/self::a/count(.)", 1, root("ItemMap"));

    // no rewriting possible
    check("(<a/>, <b/>)/<c/>", "<c/>\n<c/>", root(MixedPath.class));
  }

  /** Test. */
  @Test public void gh1741() {
    check("<a/>/<b/>[1]", "<b/>", root(CElem.class));
    check("<a/>/.[1]", "<a/>", root(CElem.class));
    check("<doc><x/><y/></doc>/*/..[1] ! name()", "doc", empty(ItrPos.class));

    check("<a/>/<b/>[2]", "", empty());
    check("<a/>/.[2]", "", empty());
    check("<doc><x/><y/></doc>/*/..[2] ! name()", "", empty());
  }

  /** Combined kind tests. */
  @Test public void gh1737() {
    // merge identical steps, rewrite to iterative path
    check("<a/>/(* | *)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(*, *)",  "", root(IterPath.class), empty(List.class));

    // rewrite to single union node test, rewrite to iterative path
    check("<a/>/(a | b)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a, b)",  "", root(IterPath.class), empty(List.class));

    // merge descendant-or-self step, rewrite to iterative path
    check("<a/>//(a | b)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a, b)",   "", root(IterPath.class), empty(List.class));

    // rewrite to single union node test, rewrite to iterative path
    check("<a/>/(a | b)[text()]", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a, b)[text()]",  "", root(IterPath.class), empty(List.class));
    check("<_><a>x</a><b/></_>/(a, b)[text()]", "<a>x</a>",
        root(IterPath.class), empty(List.class));

    // rewrite to union expression
    check("<a/>/(*, @*)", "", root(MixedPath.class), exists(Union.class));
  }

  /** Merge adjacent steps in path expressions. */
  @Test public void gh1761() {
    // merge self steps
    check("<a/>/self::*/self::a", "<a/>", count(IterStep.class, 1));
    check("<a/>/self::*/self::b", "", count(IterStep.class, 1));
    check("<a/>/self::a/self::*", "<a/>", count(IterStep.class, 1));
    check("<a/>/self::a/self::node()", "<a/>", count(IterStep.class, 1));

    // merge descendant and self steps
    check("document { <a/> }//self::a", "<a/>", count(IterStep.class, 1));
    check("document { <a/> }//*/self::a", "<a/>", count(IterStep.class, 1));

    // combined kind tests
    check("document { <a/>, <b/> }/(a, b)/self::a", "<a/>", count(IterStep.class, 1));
    check("document { <a/>, <b/> }/a/(self::a, self::b)", "<a/>", count(IterStep.class, 1));
    check("document { <a/>, <b/> }/(a, b)/(self::b, self::a)", "<a/>\n<b/>",
        count(IterStep.class, 1));
  }

  /** Merge steps and predicates with self steps. */
  @Test public void gh1762() {
    // merge self steps
    check("<a/>/self::*[self::a]", "<a/>", count(IterStep.class, 1));
    check("<a/>/self::*[self::b]", "", count(IterStep.class, 1));
    check("<a/>/self::a[self::*]", "<a/>", count(IterStep.class, 1));
    check("<a/>/self::a[self::node()]", "<a/>", count(IterStep.class, 1));

    // nested predicates
    check("<a/>/self::a[self::a[self::a[self::a]]]", "<a/>", count(IterStep.class, 1));

    // combined kind test
    check("document { <a/>, <b/> }/a[self::a | self::b]", "<a/>", count(IterStep.class, 1));
  }

  /** Path tests. */
  @Test public void gh1729() {
    check("let $x := 'g' return <g/> ! self::g[name() = $x]", "<g/>",
        root(IterPath.class));
    check("let $x := 'g' return <g/> ! self::*[local-name() = $x]", "<g/>",
        root(IterPath.class));
  }

  /** Path tests. */
  @Test public void gh1752() {
    check("'1' ! json:parse(.)/descendant::*[text()] = 1", true, empty(IterMap.class));
  }

  /** Static typing of computed maps. */
  @Test public void gh1766() {
    check("let $map := map { 'c': 'x' } return $map?(1 to 6)", "",
        type(DualMap.class, "xs:string*"));
    check("let $map := map { 'c': 'x' } return count($map?(1 to 6))", 0,
        type(DualMap.class, "xs:string*"));
  }

  /** Rewrite boolean comparisons. */
  @Test public void gh1775() {
    check("(false(), true()) ! (.  = true())", "false\ntrue", root(BlnSeq.class));
    check("(false(), true()) ! (. != false())", "false\ntrue", root(BlnSeq.class));

    check("(false(), true())[random:double() < 2] ! (.  = false())", "true\nfalse", exists(NOT));
    check("(false(), true())[random:double() < 2] ! (. != true())", "true\nfalse", exists(NOT));
  }

  /** Merge predicates. */
  @Test public void gh1777() {
    check("(5, 6, 7)[. > 5][. < 7]", 6, count(CmpIR.class, 1));
    check("('a', 'b')[2][2]", "", empty());
    check("('a', 'b')[1][1]", "a", root(Str.class));

    check("for $n in <x a='1'/>/@* where $n >= 1 where $n <= 2 return $n", "a=\"1\"",
      root(IterPath.class), count(CmpR.class, 1));
    check("for $n in <x a='1'/>/@* where data($n) >= 1 where data($n) <= 2 return $n", "a=\"1\"",
      root(IterPath.class), count(CmpR.class, 1));
    check("for $n in <x a='1'/>/@* where $n/data() >= 1 where $n/data() <= 2 return $n", "a=\"1\"",
      root(IterPath.class), count(CmpR.class, 1));
  }

  /** Merge conjunctions. */
  @Test public void gh1776() {
    check("for $n in (1, 2, 3) where $n != 2 and $n != 3 return $n", 1,
        exists(NOT), exists(RangeSeq.class));

    check("<_>A</_>[. = <a>A</a> and . = 'A']", "<_>A</_>", exists(NOT), exists(List.class));
    check("(<a/>, <b/>, <c/>)[name() = 'a' and name() = 'b']", "",
        exists(NOT), exists(StrSeq.class));
  }

  /** Merge of consecutive operations. */
  @Test public void gh1778() {
    check("(1 to 3)[. = 1 or . != 2 or . = 3 or . != 4]", "1\n2\n3", count(IntSeq.class, 2));

    check("for $n in (<a/>, <b/>, <c/>) " +
        "where name($n) != 'a' where $n = '' where name($n) != 'b' " +
        "return $n", "<c/>", exists(NOT), exists(StrSeq.class));
    check("for $n in (<a/>, <b/>, <c/>) " +
        "where name($n) != 'a' and $n = '' and name($n) != 'b' " +
        "return $n", "<c/>", exists(NOT), exists(StrSeq.class));
  }

  /** EBV checks. */
  @Test public void gh1769ebv() {
    check("if((<a/>, <b/>)) then 1 else 2", 1, root(Int.class));
    check("if(data(<a/>)) then 1 else 2", 2, exists(IterPath.class));
  }

  /** Remove redundant atomizations. */
  @Test public void gh1769data() {
    check("data(<_>1</_>) + 2", 3, empty(DATA));
    check("string-join(data(<_>X</_>))", "X", empty(DATA));
    check("data(data(<_>X</_>))", "X", count(DATA, 1));
    check("string(data(<_>X</_>))", "X", empty(DATA));
    check("data(string(<_>X</_>))", "X", empty(DATA));

    check("<x>A</x>[data() = 'A']", "<x>A</x>", empty(DATA), count(ContextValue.class, 1));
    check("<x>A</x>[data() ! data() ! data() = 'A']", "<x>A</x>",
        empty(DATA), count(ContextValue.class, 1));
    check("<x>A</x>[data() = 'A']", "<x>A</x>", empty(DATA));

    check("<A>A</A> ! data() = data(<A>B</A>)", false, empty(DATA));

    check("data(string(<_/>)) instance of xs:string", true, root(Bln.class));
    check("data(xs:NMTOKENS(<_/>) ! xs:untypedAtomic(.)) instance of xs:untypedAtomic*",
        true, root(Bln.class));
    error("data(data(<_>a</_>) promote to element(e))", INVPROMOTE_X_X_X);
    error("data(<_>a</_> promote to element(e))", INVPROMOTE_X_X_X);
  }

  /** Remove redundant atomizations. */
  @Test public void gh1769string() {
    check("('a', 'b')[string() = 'a']", "a", empty(STRING));
    check("('a', 'b')[string(.) = 'b']", "b", empty(STRING));

    check("<x>A</x>[string() = 'A']", "<x>A</x>", empty(STRING));
    check("<x>A</x>[string(.) = 'A']", "<x>A</x>", empty(STRING));
    check("<A>A</A> ! string() = data(<A>B</A>)", false, empty(STRING));

    check("max(<_>1</_> ! string(@a))", "", root(STRING));
    check("max((<_>1</_>, <_>2</_>) ! string(@a))", "", root(MAX));
    check("min(<_ _='A'/>/@_ ! string())", "A", root(MIN));

    check("string(<_>1</_>[.= 1]) = <_>1</_>", true, exists(STRING));
  }

  /** Remove redundant atomizations. */
  @Test public void gh1769number() {
    check("(0e0, 1e0)[number() = 1]", 1, empty(NUMBER));
    check("(0e0, 1e0)[number(.) = 1]", 1, empty(NUMBER));

    check("<_>1</_>[number() = 1]", "<_>1</_>", empty(NUMBER));
    check("<_>1</_>[number(.) = 1]", "<_>1</_>", empty(NUMBER));

    check("number(<_>1</_>) + 2", 3, empty(NUMBER));
    check("(1e0, 2e0) ! (number() + 1)", "2\n3", empty(NUMBER));
    check("for $v in (1e0, 2e0) return number($v) + 1", "2\n3", empty(NUMBER));

    check("for $n in (10000000000000000, 1)[. != 0] return number($n) = 10000000000000001",
        "true\nfalse", exists(NUMBER));
  }

  /** Inlining of where clauses. */
  @Test public void gh1782() {
    check("1 ! (for $a in (1, 2) where $a = last() return $a)", 1, exists(GFLWOR.class));
    check("(3, 4)[. != 0] ! (for $a in (1, 2) where . = $a return $a)", "", exists(GFLWOR.class));
  }

  /** Rewriting of positional tests that might yield an error. */
  @Test public void gh1783() {
    execute(new Close());
    error("(position() = 3) = (position() = 3)", NOCTX_X);
    error(". = .", NOCTX_X);
  }

  /** Pre-evaluate predicates in filter expressions. */
  @Test public void gh1785() {
    check("<a/>[self::node()]", "<a/>", root(CElem.class));
    check("<a/>[self::*]", "<a/>", root(CElem.class));
    check("<a/>[self::*[name(..) = 'a']]", "", root(IterFilter.class));
    check("<a/>[self::text()]", "", empty());
  }

  /** Better typing of functions with context arguments. */
  @Test public void gh1787() {
    check("path(<a/>) instance of xs:string", true, root(Bln.class));
    check("<a/>[path() instance of xs:string]", "<a/>", root(CElem.class));
    check("<a/>[name() = 'a']", "<a/>", exists(CmpSimpleG.class));

    check("node-name(prof:void(()))", "", root(_PROF_VOID));
    check("prefix-from-QName(prof:void(()))", "", root(_PROF_VOID));
  }

  /** Rewrite name tests to self steps. */
  @Test public void gh1770() {
    check("<a/>[node-name() eq xs:QName('a')]", "<a/>", exists(SingleIterPath.class));
    check("<a/>[local-name() eq 'a']", "<a/>", exists(SingleIterPath.class));

    check("<a/>[local-name() = ('a', 'b', '')]", "<a/>", exists(SingleIterPath.class));
    check("<a/>[local-name() = 'a' or local-name() = 'b']", "<a/>", exists(SingleIterPath.class));
    check("<a/>[node-name() = (xs:QName('a'), xs:QName('b'))]", "<a/>",
        exists(SingleIterPath.class));
    check("<a/>[local-name() = ('a', 'a', 'a')]", "<a/>", exists(SingleIterPath.class));

    check("(<a/>, <b/>)[. = '!'][local-name() = 'a']", "", empty(LOCAL_NAME));

    check("comment {}[local-name() = '']", "<!---->", root(CComm.class));
    check("text { 'a' }[local-name() = '']", "a", root(CTxt.class));

    final String prolog = "declare default element namespace 'A'; ";
    check(prolog + "<a/>[node-name() eq QName('A', 'a')]",
        "<a xmlns=\"A\"/>", exists(SingleIterPath.class));
    check(prolog + "<a/>[namespace-uri() eq 'A']",
        "<a xmlns=\"A\"/>", exists(SingleIterPath.class));

    // no rewritings
    check("<a/>[local-name() != 'a']", "", exists(LOCAL_NAME));
    check("<a/>[local-name() = <_>a</_>]", "<a/>", exists(LOCAL_NAME));
    check("<a/>[node-name() = xs:QName(<_>a</_>)]", "<a/>", exists(NODE_NAME));
    check("parse-xml('<a/>')[name(*) = 'a']", "<a/>", exists(Function.NAME));
  }

  /** Functions with database access. */
  @Test public void gh1788() {
    execute(new CreateDB(NAME, "<x>A</x>"));
    check("db:text('" + NAME + "', 'A')/parent::x", "<x>A</x>", exists(_DB_TEXT));
    check("db:text('" + NAME + "', 'A')/parent::unknown", "", empty());
  }

  /** Inline context in filter expressions. */
  @Test public void gh1792() {
    check("1[. = 0]", "", empty());
    check("1[. * .]", 1, root(Int.class));
    check("2[. * . = 4]", 2, root(Int.class));
    check("2[number()]", "", empty());
    check("1[count(.)]", 1, root(Int.class));
    check("1[count(.)]", 1, root(Int.class));
    check("1[.[.[.[.[.[.[.[.[. = 1]]]]]]]]]", 1, root(Int.class));
    check("1[1[1[1[1[1[1[1[1[1 = .]]]]]]]]]", 1, root(Int.class));

    check("''[string()]", "", empty());
    check("'a'[string()]", "a", root(Str.class));
    check("'a'[string(.)]", "a", root(Str.class));
    check("'a'[string-length() = 1]", "a", root(Str.class));
    check("'a'[string-length(.) = 1]", "a", root(Str.class));
    check("' '[normalize-space()]", "", empty());
    check("''[data()]", "", empty());

    check("<_/>[data()]", "", root(IterFilter.class));
    check("(1, 2)[. = 3]", "", root(IterFilter.class));
  }

  /** Type checks. */
  @Test public void gh1791() {
    inline(true);
    check("function() as xs:double { <x>1</x> }() + 2", 3, empty(TypeCheck.class));
    check("declare function local:_() as xs:string { <x>A</x> }; local:_() = 'A'",
        true, empty(TypeCheck.class));
    check("declare function local:f($s as xs:string) { tokenize($s) };\n" +
        "<x/> ! local:f(.)", "", empty(TypeCheck.class));
  }

  /** Unary expression. */
  @Test public void gh1796() {
    check("-xs:byte(-128) instance of xs:byte", false, empty(Unary.class), empty(Cast.class));
    check("for $i in -128 return --xs:byte($i) instance of xs:byte", false,
        empty(Unary.class), empty(Cast.class));
    check("let $i := -128 return --xs:byte($i) instance of xs:byte", false,
        empty(Unary.class), empty(Cast.class));

    check("--xs:byte(<_>-128</_>)", -128, empty(Unary.class), count(Cast.class, 2));
  }

  /** Promote to expression. */
  @Test public void gh1798() {
    check("<a>1</a> promote to xs:string", 1, root(TypeCheck.class));
    check("(1, 2) promote to xs:double+", "1\n2",
        empty(TypeCheck.class), root(ItemSeq.class), count(Dbl.class, 2));

    error("<a/> promote to empty-sequence()", INVPROMOTE_X_X_X);
    error("(1, 2) promote to xs:byte+", INVPROMOTE_X_X_X);
  }

  /** Treats and promotions, error messages. */
  @Test public void gh1799() {
    error("'a' promote to node()", INVPROMOTE_X_X_X);
    error("'a' treat as  node()", NOTREAT_X_X_X);
  }

  /** Merge of operations with fn:not. */
  @Test public void gh1805() {
    check("<_/>[not(. = ('A', 'B'))][not(. = ('C', 'D'))]", "<_/>", count(CmpHashG.class, 1));
    check("<_/>[. != 'A'][. != 'B'][. != 'C'][. != 'D']", "<_/>",   count(CmpHashG.class, 1));

    check("(3, 4)[not(. = 1) and not(. = (2, 4))]", 3, count(NOT, 1), count(CmpHashG.class, 1));
    check("(3, 4)[not(. = (2, 4)) and . != 1]", 3, count(NOT, 1), count(CmpHashG.class, 1));

    check("(3, 4)[not(. = (2, 3)) and not(. = (1, 4))]", "",
        count(NOT, 1), count(CmpHashG.class, 1));
  }

  /** Comparisons with empty strings. */
  @Test public void gh1803() {
    check("<a/>[namespace-uri() eq '']", "<a/>", exists(NOT), empty(STRING));
    check("<a/>[local-name() eq '']", "", exists(NOT), empty(STRING));
    check("attribute { 'a' } { '' }[local-name() = '']", "", exists(NOT), empty(STRING));
    check("let $x := (<a/>, <a/>) where $x[. eq ''] return $x", "<a/>\n<a/>",
        exists(EMPTY), exists(SingleIterPath.class));

    check("string(<_/>) != ''", false, exists(EXISTS), exists(IterPath.class));
    check("string(<_/>) = ''", true, exists(EMPTY), exists(IterPath.class));
    check("string(<_/>) <= ''", true, exists(EMPTY), exists(IterPath.class));
    check("string(<_/>) >= ''", true, root(Bln.class));
    check("string(<_/>) < ''", false, root(Bln.class));

    check("('', 'a')[string() != '']", "a", root(IterFilter.class), empty(CmpG.class));
    check("('', 'a')[string() = '']", "", root(IterFilter.class), exists(NOT));
  }

  /** Comparisons with simple map operands. */
  @Test public void gh1804() {
    // rewritings in comparisons
    check("<_>A</_> ! text() = 'A'", true, exists(IterPath.class));
    check("<_>A</_> ! text() = 'A'", true, exists(IterPath.class));
    check("let $a := <_>A</_> return $a ! text() = $a ! text()", true,
        root(ItemMap.class), empty(IterPath.class), empty(IterMap.class));

    // EBV rewritings
    check("<a><b/></a>[b ! ..]", "<a>\n<b/>\n</a>", exists(CachedPath.class));

    // do not rewrite absolute paths
    check("text { 'a' } ! <x>{ . }</x>/text() = 'a'", true, exists(IterMap.class));
    check("<a>a</a>/string() ! <x>{ . }</x>/text() = 'a'", true, exists(IterMap.class));
  }

  /** Rewrite if to where. */
  @Test public void gh1806() {
    check("let $a := <_/> return if ($a = '') then $a else ()", "<_/>",
        empty(If.class), empty(Where.class), exists(IterFilter.class));
    check("for $a in (1, 2) return if ($a = 3) then $a else ()", "",
        empty(If.class), empty(Where.class), root(IterFilter.class));

    check("for $t in ('a', 'b') return if($t) then $t else ()", "a\nb",
        root(IterFilter.class), exists(ContextValue.class));
    check("for $t in ('a', 'b') return $t[$t]", "a\nb",
        root(IterFilter.class), exists(ContextValue.class));

    check("for $i in 1 to 2\n" +
        "for $f in if ($i = 1) then 1 else ()\n" +
        "return $f + $i\n", 2, empty(GFLWOR.class));
  }

  /** If expression with empty branches. */
  @Test public void gh1809() {
    check("if(<_>1</_>[. = 1]) then () else ()", "", empty());
    check("if(prof:void(1)) then () else ()", "", root(_PROF_VOID), count(_PROF_VOID, 1));
  }

  /** Redundant predicates in paths. */
  @Test public void gh1812() {
    check("<a/>/*[*]/*", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a>X</a>/text()[..]/..", "<a>X</a>", empty(SingleIterPath.class));

    // no rewriting
    check("<a/>/*[*]/text()", "", count(IterPath.class, 1), exists(SingleIterPath.class));
  }

  /** EBV simplifications: if, switch, typeswitch. */
  @Test public void gh1813() {
    // if expression
    check("(1, 2) ! boolean(if(.) then 'a' else <a/>)", "true\ntrue", root(SingletonSeq.class));
    check("(1 to 2) ! boolean(if(.) then '' else ())", "false\nfalse", root(SingletonSeq.class));
    check("boolean(if(random:double()) then '' else 0)", "false",
        root(List.class), exists(_PROF_VOID));

    check("(1, 2)[if(.) then 0.0e0 else 0.0]", "", empty());
    check("(1, 2)[if(.) then '' else xs:anyURI('')]", "", empty());

    // no rewriting of numbers > 0
    check("(1, 2)[. != 0] ! boolean(if(.) then 'a' else 1)", "true\ntrue", exists(If.class));

    // switch expression
    check("for $i in (1 to 3)\n" +
        "return if(switch($i)\n" +
        "  case 1 return 0\n" +
        "  case 2 return ''\n" +
        "  default return ()\n" +
        ") then 'fail' else ''", "\n\n", root(SingletonSeq.class));

    // typeswitch expression
    check("for $i in ('a', 1)\n" +
        "return if(typeswitch($i)\n" +
        "  case xs:integer return 0\n" +
        "  case xs:string  return ''\n" +
        "  default return ()\n" +
        ") then 'fail' else ''", "\n", root(SingletonSeq.class));
  }

  /** Simple map, index rewritings. */
  @Test public void gh1814() {
    execute(new CreateDB(NAME, "<x><y><z>A</z></y></x>"));
    check("exists(x[y/z = 'A'])", true, exists(ValueAccess.class));
    check("exists(x[y ! z = 'A'])", true, exists(ValueAccess.class));
  }

  /** Typing: data references. */
  @Test public void gh1816() {
    execute(new CreateDB(NAME, "<x><y/></x>"));
    check("x/z", "", empty());
    check("(x, y)/z", "", empty());
    check("(x | y)/z", "", empty());
    check("(if(random:double()) then x else y)/z", "", empty());
    check("(let $_ := random:double() return x)/z", "", empty());
    check("((# bla #) { x })/z", "", empty());
    check("(switch (random:double())\n" +
      "  case 1 return ()\n" +
      "  default return x\n" +
      ")/z", "", empty());

    check("(x, prof:void(()))/z", "", empty());
    check("(x | prof:void(()))/z", "", empty());
    check("(if(random:double()) then x)/z", "", empty());
  }

  /** List to union in root of path expression. */
  @Test public void gh1817() {
    check("<a/>[(b, c)/d]", "", empty(List.class), count(IterPath.class, 1));

    // do not rewrite paths that yield no nodes
    check("(<a/>, <b/>)/name()", "a\nb", exists(List.class));
    check("let $_ := <_/> return ($_, $_)/0", "0\n0", exists(SingletonSeq.class));
  }

  /** List to union. */
  @Test public void gh1818() {
    check("<a/>[b, text()]", "", exists(Union.class), count(SingleIterPath.class, 2));

    // union expression will be further rewritten to single path
    check("<a/>[b, c]", "", empty(List.class), count(SingleIterPath.class, 1));
    check("<a/>[(b, c) = '']", "", empty(List.class), count(SingleIterPath.class, 1));
    check("<a/>[(b, c) = (b, c)]", "", empty(List.class), count(SingleIterPath.class, 2));
  }

  /** FLWOR, no results, non-deterministic expressions. */
  @Test public void gh1819() {
    check("for $_ in () return <x/>", "", empty());
    check("for $_ in prof:void(1) return 1", "", root(_PROF_VOID));
    check("let $_ := 1 return <x/>", "<x/>", root(CElem.class));
    check("let $_ := prof:void(1) return 1", 1, root(List.class), exists(_PROF_VOID));
    check("for $_ in 1 to 2 return 3", "3\n3", root(SingletonSeq.class));
    check("for $_ in 1 to 2 return ()", "", empty());

    check("let $_ := prof:void(1) return 1", 1, root(List.class), exists(_PROF_VOID));

    // rewrite to simple map
    check("for $_ in 1 to 2 return <a/>", "<a/>\n<a/>", root(_UTIL_REPLICATE));
    check("for $_ in 1 to 2 return prof:void(1)", "", root(_UTIL_REPLICATE));
  }

  /** Merge and/or expressions. */
  @Test public void gh1820() {
    // OR: merge
    check("(<_/>, <_/>) = 'a' or (<_/>, <_/>) = 'b'", false, empty(Or.class));
    check("(<_/>, <_/>) = 'a' or (<_/>, <_/>) = ('b', 'c')", false, empty(Or.class));
    check("<_>1</_>[. = 1 or . = (2, 3)]", "<_>1</_>", empty(Or.class), exists(CmpG.class));
    check("<_>1</_>[not(. = 1) or . != (2, 3)]", "<_>1</_>", empty(Or.class));
    check("exists(let $x := <a><b>c</b><b>d</b></a> return $x[b = 'c' or b = 'd'])", true,
        count(CmpHashG.class, 1));

    // OR: no merge
    check("<_>1</_>[not(. = 1) or not(. = (2, 3))]", "<_>1</_>", exists(Or.class));
    check("<_>1</_>[. = 1 or not(. != (2, 3))]", "<_>1</_>", exists(Or.class));
    check("<_>1</_>[not(. = 1) or . = (2, 3)]", "", exists(Or.class));
    check("<_>1</_>[. = 1 or not(. = (2, 3))]", "<_>1</_>", exists(Or.class));

    check("<_>a</_>[. = 'a' or . != 'b']", "<_>a</_>", exists(Or.class));
    check("<_>a</_>[. = 'a' or .. != 'b']", "<_>a</_>", exists(Or.class));

    // AND: merge
    check("<_>1</_>[not(. = 1) and not(. = (2, 3))]", "",
        exists(CmpG.class), empty(CmpSimpleG.class));
    check("not((<_/>, <_/>) != 'a') and not((<_/>, <_/>) != 'b')", false,
        exists(CmpG.class), empty(CmpSimpleG.class));

    // AND: no merge
    check("(<_/>, <_/>) = 'a' and (<_/>, <_/>) = ('b', 'c')", false, exists(And.class));

    check("exists(let $x := <a><b>c</b><b>d</b></a> return $x[b = 'c' and b = 'd'])", true,
        count(CmpG.class, 2));

    check("<_>1</_>[. = 1 and . = (2, 3)]", "",
        exists(CmpG.class), exists(CmpSimpleG.class));
    check("<_>1</_>[not(. = 1) and . = (2, 3)]", "",
        exists(CmpG.class), exists(CmpSimpleG.class));
    check("<_>1</_>[. = 1 and not(. = (2, 3))]", "<_>1</_>",
        exists(CmpG.class), exists(CmpSimpleG.class));

    check("(<_/>, <_/>) = '' and (<_/>, <_/>) = 'a'", false, exists(And.class));
  }

  /** Map/array lookups: better typing. */
  @Test public void gh1825() {
    check("[ 1 ](<_>1</_>) instance of xs:integer", true, root(Bln.class));

    check("map { 'a': 2 }(<_>a</_>) instance of xs:integer?", true, root(Bln.class));
    check("map { 1: 2 }(<_/>) instance of xs:integer?", true, root(Bln.class));
    check("map { 1: (2, 'a') }(<_/>) instance of xs:anyAtomicType*", true, root(Bln.class));
  }

  /** Rewriting of positional predicate. */
  @Test public void gh1827() {
    inline(true);
    check("declare function local:f($pos) { (1, 2)[position() < $pos] };\n" +
        "local:f(1)", "", empty());
  }

  /** Merge fn:empty and fn:exist functions. */
  @Test public void gh1829() {
    check("exists(<a/>/a) or exists(<b/>/b)",
        false, root(EXISTS), empty(Or.class), empty(BOOLEAN), exists(Union.class));
    check("for $i in (1 to 2)[. != 0] return exists($i[. = 1]) or exists($i[. = 2])",
        "true\ntrue", empty(Or.class), empty(EXISTS), root(DualMap.class));

    check("<a/>/a or <b/>/b", false, root(EXISTS), empty(Or.class), exists(Union.class));
    check("<a/>[a or b]", "", empty(Or.class), count(SingleIterPath.class, 1));

    check("<a/>[empty(b)][empty(c)]", "<a/>", count(EMPTY, 1), count(SingleIterPath.class, 1));
    check("<a/>[empty((b, c))]", "<a/>", count(SingleIterPath.class, 1));
    check("for $a in (<b/>, <c/>) return <a/>[empty(($a[. = 'i'], $a[. = 'j']))]", "<a/>\n<a/>",
        root(DualMap.class), exists(NOT), empty(If.class));

    // no rewritings
    query("for $a in (1 to 2)[. != 0] return <a/>[empty(($a[. = 1], $a[. = 1]))]", "<a/>");
    check("exists(<a/>/a) and exists(<b/>/b)", false, exists(And.class));
    check("for $i in (1 to 2)[. != 0] return exists($i[. = 1]) and exists($i[. = 2])",
        "false\nfalse", empty(And.class), empty(EXISTS), root(DualMap.class));
    check("<a/>/a and <b/>/b", false, exists(And.class));
    check("<a/>[a and b]", "", count(SingleIterPath.class, 2));

    check("<a/>[empty(b) or empty(c)]", "<a/>", exists(Or.class));
  }

  /** Documents with different default namespaces. */
  @Test public void gh1831() {
    execute(new CreateDB(NAME));
    execute(new Add("a.xml", "<x xmlns='x'/>"));
    execute(new Add("b.xml", "<x/>"));
    query("x", "<x/>");
  }

  /** Equality tests on QNames. */
  @Test public void gh1823() {
    query("declare namespace p = 'U';\n" +
      "prefix-from-QName(QName('U', 'a')[. = xs:QName(<_>p:a</_>)]) or\n" +
      "prefix-from-QName(QName('U', 'p:a')[. = xs:QName(<_>p:a</_>)])",
      true);
    query("let $f := function($a) { string($a) }\n" +
      "return distinct-values(($f(QName('U', 'l')), $f(QName('U', 'p:l'))))",
      "l\np:l");
  }

  /** Static typing, maps. */
  @Test public void gh1834() {
    query("declare function local:f() as map(xs:string, xs:string) {" +
      "  map:entry(<e/> ! string(), '')" +
      "}; local:f()?*", "");
    query("declare function local:f() as map(xs:string, xs:string) {" +
      "  map:put(map {}, <e/> ! string(), '')" +
      "}; local:f()?*", "");
  }

  /** Set Expressions: Merge operands. */
  @Test public void gh1838() {
    check("<_><a/></_>/(* union *[b])", "<a/>",
        empty(Union.class), empty(SingleIterPath.class), count(IterStep.class, 1));
    check("<_><a/></_>/(* intersect *[b])", "",
        empty(Intersect.class), count(SingleIterPath.class, 1), count(IterStep.class, 2));
    check("<_><a/></_>/(* except *[b])", "<a/>",
        empty(Except.class), exists(EMPTY), count(SingleIterPath.class, 1));

    check("<_><a/></_>/*/(.[self::a] union .[self::b])", "<a/>",
        empty(Union.class));
    check("<_><a/></_>/*/(.[self::a] intersect .[self::b])", "",
        empty(Intersect.class));
    check("<_><a/></_>/*/(.[self::a] except .[self::b])", "<a/>",
        empty(Except.class), exists(EMPTY));

    check("<_><a/></_>/(.[b][c] union .[d])", "",
        empty(Union.class), exists(And.class), exists(Or.class));
    check("<_><a/></_>/(.[b][c] intersect .[d])", "",
        empty(Intersect.class), empty(And.class));
    check("<_><a/></_>/(.[b][c] except .[d])", "",
        empty(Except.class), exists(EMPTY), empty(And.class));

    // no optimization
    check("<_><a/></_>/(a union a/*[b])", "<a/>", exists(Union.class));
    check("<_><a/></_>/(a union a/<b/>)", "<a/>\n<b/>", exists(Union.class));
    check("<_><a/></_>/(a union self::a)", "<a/>", exists(Union.class));
    check("<_><a/></_>/(a[1] union a[2])", "<a/>", exists(Union.class));
    check("<_/>/(<b/>[*] union <c/>[*])", "", exists(Union.class));
  }

  /** Logical expressions, DNFs/CNFs. */
  @Test public void gh1839() {
    // no rewriting
    check(gh1839("$a or $b"), "fttt",
        count(And.class, 0), count(Or.class, 1));
    // no rewriting
    check(gh1839("$a and $b"), "ffft",
        count(And.class, 1), count(Or.class, 0));

    // optimized: $a
    check(gh1839("$a or ($a and $b)"), "fftt",
        count(And.class, 0), count(Or.class, 0));
    // optimized: $a
    check(gh1839("$a and ($a or $b)"), "fftt",
        count(And.class, 0), count(Or.class, 0));

    // optimized: $a and ($b or $c)
    check(gh1839("($a and $b) or ($a and $c)"), "fffffttt",
        count(And.class, 1), count(Or.class, 1));
    // optimized: $a or ($b and $c)
    check(gh1839("($a or $b) and ($a or $c)"), "fffttttt",
        count(And.class, 1), count(Or.class, 1));

    // optimized: $a or ($b and ($c or $d))
    check(gh1839("($a or $b) and ($a or $c or $d)"), "fffffttttttttttt",
        count(And.class, 1), count(Or.class, 2));
    // optimized: $a and ($b or ($c and $d))
    check(gh1839("($a and $b) or ($a and $c and $d)"), "fffffffffffttttt",
        count(And.class, 2), count(Or.class, 1));

    // optimized: $a and $b
    check(gh1839("($a and $b) or ($a and $b and $c)"), "fffffftt",
        count(And.class, 1), count(Or.class, 0));
    // optimized: $a or $b
    check(gh1839("($a or $b) and ($a or $b or $c)"), "fftttttt",
        count(And.class, 0), count(Or.class, 1));
  }

  /**
   * Creates a query that concatenates booleans in a string.
   * @param query query string
   * @return query
   */
  private static String gh1839(final String query) {
    // extract variable names from query
    final StringList vars = new StringList();
    for(final String var : query.split("\\$")) {
      if(var.isEmpty()) continue;
      final char ch = var.charAt(0);
      if(Character.isLetter(ch)) vars.addUnique(String.valueOf(ch));
    }

    // generate query string with FLWOR expression
    final StringBuilder sb = new StringBuilder();
    sb.append("string-join(\n");
    for(final String var : vars) {
      sb.append("  for $").append(var).append(" in (0, 1) ! <_>{ . }</_> ! xs:boolean(.)\n");
    }
    sb.append("  return if(").append(query).append(") then 't' else 'f'\n");
    return sb.append(")").toString();
  }

    /** Combine position predicates. */
  @Test public void gh1840() {
    check("(1,2,3)[position() = 1 or position() = 1]", 1, root(Int.class));
    check("(1,2,3)[position() = 1 or position() = 2]", "1\n2", root(RangeSeq.class));
    check("(1,2,3)[position() = 1 or position() = 3]", "1\n3", count(ItrPos.class, 2));

    check("(1,2,3)[position() = 1 to 2 or position() = 1]", "1\n2", root(RangeSeq.class));
    check("(1,2,3)[position() = 1 to 2 or position() = 2]", "1\n2", root(RangeSeq.class));
    check("(1,2,3)[position() = 1 to 2 or position() = 3]", "1\n2\n3", root(RangeSeq.class));

    check("(1,2,3)[position() = 1 to 2 and position() = 1]", 1, root(Int.class));
    check("(1,2,3)[position() = 1 to 2 and position() = 2 to 3]", 2, root(Int.class));
    check("(1,2,3)[position() = 1 to 2 and position() = 3]", "", empty());
  }

  /** Distinct integer sequences. */
  @Test public void gh1841() {
    check("<_/>[position() = (1, 1)]", "<_/>", root(CElem.class));
    check("<_/>[position() = (1, 2, 1)]", "<_/>", root(CElem.class));
    check("<_/>[position() = (1, 2, 2.1)]", "<_/>", count(Int.class, 2));
  }

  /** Flatten expression lists. */
  @Test public void gh1842() {
    check("(<a/>, (<b/>, <c/>))", "<a/>\n<b/>\n<c/>", count(List.class, 1));

    check("count(<a/> union (<b/> union <c/>))", 3, count(Union.class, 1));
    check("count(<a/> intersect (<b/> intersect <c/>))", 0, count(Intersect.class, 1));
    check("count(<a/> except (<b/> except <c/>))", 1, count(Except.class, 2));

    check("<a/> ! (. > '0' or (. < '1' or . < '2'))", true, count(Or.class, 1));
    check("<a/> ! (. = '0' and (. < '1' and . != '2'))", false, count(And.class, 1));

    check("(<_/>[. != 'a'])[. != 'b']\n", "<_/>", count(IterFilter.class, 1), exists(NOT));
  }

  /** Merge simple map and filter expressions. */
  @Test public void gh1843() {
    check("(1, 2) ! .[. = 1]", 1, root(IterFilter.class));
    check("('a', 'b') ! .[. = 'b']", "b", root(IterFilter.class));
    check("(1, 2)[. = 1] ! .[. = 1]", 1, root(IterFilter.class), count(CmpSimpleG.class, 1));
  }

  /** Rewrite filters to simple maps. */
  @Test public void gh1845() {
    check("boolean(<a>A</a>[contains(., 'A')])", true, empty(IterFilter.class));
    check("for $n in (<a/>, <b/>) return if($n[name() = 'a']) then 1 else 2", "1\n2",
        empty(IterFilter.class));
    check("boolean(<a/>[data()][. = 'A'])", false, empty(BOOLEAN));
  }

  /** Merge descendant steps. */
  @Test public void gh1848() {
    check("<a/>//descendant::*", "", count(IterPath.class, 1), count(IterStep.class, 1));
    check("<a/>//descendant::text()", "", count(IterPath.class, 1), count(IterStep.class, 1));
    check("<a/>//(descendant::a, descendant::b)", "",
        count(IterPath.class, 1), count(IterStep.class, 1));
  }

  /** Merge steps and predicates with self steps. */
  @Test public void gh1850() {
    check("<a/>/*[self::b]", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[self::b and true()]", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[self::b][true()]", "", count(IterPath.class, 1), empty(SingleIterPath.class));
  }

  /** Name tests in where clauses, index rewritings. */
  @Test public void gh1851() {
    check("for $c in 1 to 10 return $c[. = 1]", 1, root(IterFilter.class));

    // move filters with positional tests and variable references into simple map
    check("for $c in 1 to 10 return $c[.]", 1, root(DualMap.class));
    check("for $c in 1 to 10 return $c[position() = (0, 2)]", "", root(DualMap.class));

    // no rewriting: position checks are replaced by util:item
    check("for $c in 1 to 10 return $c[position() = <_>2</_>]", "", root(DualMap.class));
    check("for $c in 1 to 10 return $c[$c]", 1, root(DualMap.class));
  }

  /** Set expressions, DNFs/CNFs. */
  @Test public void gh1852() {
    // no rewriting
    check(gh1852("$a() union $b()"),
        "fttt", count(Intersect.class, 0), count(Union.class, 1));
    // no rewriting
    check(gh1852("$a() intersect $b()"),
        "ffft", count(Intersect.class, 1), count(Union.class, 0));

    // optimized: $a()
    check(gh1852("$a() union ($a() intersect $b())"),
        "fftt", count(Intersect.class, 0), count(Union.class, 0));
    // optimized: $a()
    check(gh1852("$a() intersect ($a() union $b())"),
        "fftt", count(Intersect.class, 0), count(Union.class, 0));

    // optimized: $a() intersect ($b() union $c)
    check(gh1852("($a() intersect $b()) union ($a() intersect $c())"),
        "fffffttt", count(Intersect.class, 1), count(Union.class, 1));
    // optimized: $a() union ($b() intersect $c())
    check(gh1852("($a() union $b()) intersect ($a() union $c())"),
        "fffttttt", count(Intersect.class, 1), count(Union.class, 1));

    // optimized: $a() union ($b() intersect ($c() union $d))
    check(gh1852("($a() union $b()) intersect ($a() union $c() union $d())"),
        "fffffttttttttttt", count(Intersect.class, 1), count(Union.class, 2));
    // optimized: $a() intersect ($b() union ($c() intersect $d()))
    check(gh1852("($a() intersect $b()) union ($a() intersect $c() intersect $d())"),
        "fffffffffffttttt", count(Intersect.class, 2), count(Union.class, 1));

    // optimized: $a() intersect $b()
    check(gh1852("($a() intersect $b()) union ($a() intersect $b() intersect $c())"),
        "fffffftt", count(Intersect.class, 1), count(Union.class, 0));
    // optimized: $a() union $b()
    check(gh1852("($a() union $b()) intersect ($a() union $b() union $c())"),
        "fftttttt", count(Intersect.class, 0), count(Union.class, 1));
  }

  /**
   * Creates a query that concatenates booleans in a string.
   * @param query query string
   * @return query
   */
  private static String gh1852(final String query) {
    // extract variable names from query
    final StringList vars = new StringList();
    for(final String var : query.split("\\$")) {
      if(var.isEmpty()) continue;
      final char ch = var.charAt(0);
      if(Character.isLetter(ch)) vars.addUnique(String.valueOf(ch));
    }

    // generate query string with FLWOR expression
    final StringBuilder sb = new StringBuilder();
    sb.append("let $n := <n/>\n");
    sb.append("return string-join(\n");
    for(final String var : vars) {
      sb.append("  for $").append(var).append(" in (function() { }, function() { $n })\n");
    }
    sb.append("  return if(").append(query).append(") then 't' else 'f' \n");
    return sb.append(")").toString();
  }

  /** Name tests in where clauses, index rewritings. */
  @Test public void gh1853() {
    execute(new CreateDB(NAME, "<a><b/></a>"));
    check("for $e in //* where name($e) = 'b' return $e", "<b/>", empty(Function.NAME));
    check("for $e in //* where local-name($e) = 'e' return $e", "", empty());
  }

  /** Nested predicates in FLWOR expression. */
  @Test public void gh1855() {
    execute(new CreateDB(NAME, "<a><b c='d'>e</b></a>"));
    check("count(let $e := 'e' return a[b[@c = 'd'] = $e])", 1,
        exists(ValueAccess.class));
    check("count(let $e := 'e' let $f := a[b[@c = 'd'] = $e] return $f)", 1,
        exists(ValueAccess.class));
  }

  /** Reoptimize newly created fn:boolean instances. */
  @Test public void gh1856() {
    check("if(<a/>/text()) then true() else false()", false, root(EXISTS));
  }

  /** Enforce index pragma, full-text. */
  @Test public void gh1860() {
    query(_DB_CREATE.args(NAME, " <_>a</_>", "_.xml", " map { 'ftindex': true() }"));
    query("(# db:enforceindex #) {\n" +
      "  let $t := 'a'\n" +
      "  for $db in '" + NAME + "'\n" +
      "  return db:open($db)/_[text() contains text { $t }]\n" +
      "}",
      "<_>a</_>");
  }

  /** Logical expressions, tertium non datur. */
  @Test public void gh1863() {
    check("(true(), false()) ! (. and not(.))", "false\nfalse", root(SingletonSeq.class));
    check("(true(), false()) ! (. or  not(.))", "true\ntrue",   root(SingletonSeq.class));
    check("(true(), false()) ! (not(.) and .)", "false\nfalse", root(SingletonSeq.class));
    check("(true(), false()) ! (not(.) or  .)", "true\ntrue",   root(SingletonSeq.class));

    check("(true(), false()) ! (. = false() and . = true())", "false\nfalse",
        root(SingletonSeq.class));
    check("(true(), false()) ! (. = true() or . = false())", "true\ntrue",
        root(SingletonSeq.class));

    check("for $i in 1 to 2 return $i and $i = 1 and not($i)", "false\nfalse",
        root(SingletonSeq.class));

    check("<a/>[text() = 'a']['ignored'][not(text() = 'a')]", "", empty());
    check("<a/>[not(text() = 'a')]['ignored'][text() = 'a']", "", empty());
  }

  /** Remove redundant paths. */
  @Test public void gh1864() {
    check("<a/>/*[a]/a", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[a]/a/b", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[a/b]/a/b", "", count(IterPath.class, 1), empty(SingleIterPath.class));
    check("<a/>/*[a/b]/a", "", count(IterPath.class, 1), count(SingleIterPath.class, 1));
    check("<a/>/*[a/b]/a/c", "", count(IterPath.class, 1), count(SingleIterPath.class, 1));
    check("<a/>/*[a]/a[b]/b", "", count(IterPath.class, 1), empty(SingleIterPath.class));
  }

  /** Inline paths in FLWOR expressions. */
  @Test public void gh1865() {
    execute(new CreateDB(NAME, "<a><b c='d'><c/></b><c/></a>"));

    // child, attribute, self axis: merge steps
    check("for $a in /* return $a/c", "<c/>", empty(GFLWOR.class));
    check("for $n in //* return $n/c", "<c/>\n<c/>", empty(GFLWOR.class));
    check("for $n in //(a,b,c) return $n/@*", "c=\"d\"", empty(GFLWOR.class));
    check("for $n in //(a,b,c) return $n/self::d", "", empty(GFLWOR.class));

    // other axes: rewrite to simple map operator
    check("count(for $c in //c return $c/..)", 2, exists(DualMap.class));
    check("count(for $c in //* return $c//c)", 3, exists(IterMap.class));
    check("count(for $c in //* return $c/following::*)", 2, exists(IterMap.class));
    check("let $n := reverse((<a>A</a>, <b>B</b>)) return $n/text()", "B\nA",
        root(CachedPath.class));
  }

  /** FLWOR: Return clause, filter expression. */
  @Test public void gh1867() {
    check("for $a at $p in (1, 2) return $a[. = $p]", "1\n2", root(GFLWOR.class));
  }

  /** Distinct-values, optimization of arguments. */
  @Test public void gh1868() {
    check("let $s as xs:string* := distinct-values(<_>x</_> ! string()) return $s",
        "x", root(STRING));
  }

  /** Simple map, positional predicates. */
  @Test public void gh1874() {
    check("(1 to 10) ! .[.]", 1);
  }

  /** EBV Rewritings: count(expr), numeric checks. */
  @Test public void gh1875() {
    check("boolean(count(<_>A</_>[. >= 'A']))", true, empty(COUNT));
    check("boolean(count(<_>A</_>[. >= 'A']) != 0)", true, empty(COUNT));
  }

  /** Switch Expression: Rewrite to if expression. */
  @Test public void gh1877() {
    check("switch(<_/>) case '' return 1 default return 2", 1, root(If.class));
    check("switch(<_/>) case '' case 'x' return 1 default return 2", 1, root(If.class));
  }

  /** Singleton sequences in predicates. */
  @Test public void gh1878() {
    error("<a/>[util:replicate(1, 2)]", EBV_X);
  }

  /** Lacking filter rewriting. */
  @Test public void gh1879() {
    check("let $a := <a/> return $a[$a/self::a]", "<a/>",
        empty(CachedFilter.class), count(IterFilter.class, 1));
    check("let $a := <a/> return $a[./self::a]", "<a/>",
        empty(CachedFilter.class), count(IterFilter.class, 1));
    check("let $a := <a/> return $a[self::a]", "<a/>",
        empty(CachedFilter.class), count(IterFilter.class, 1));
  }

  /** Single let, inline where clause. */
  @Test public void gh1880() {
    check("let $a := <a/> where $a return $a", "<a/>",
        empty(GFLWOR.class), root(CElem.class));
    check("let $a := <a/> where $a/self::a return $a", "<a/>",
        empty(GFLWOR.class), root(IterFilter.class));
    check("let $a := <a/> where $a/self::a return $a[. = '']", "<a/>",
        empty(GFLWOR.class), count(IterFilter.class, 1));
    check("let $a := <a/> where $a[. = ''] return $a/self::a", "<a/>",
        empty(GFLWOR.class), root(IterPath.class));
    check("let $a as element(a) := <a/> where $a return $a", "<a/>",
        root(CElem.class));

    // skip rewritings: let
    check("let $e := (<a/>, <b/>) where $e/self::a return $e", "<a/>\n<b/>", root(GFLWOR.class));
    check("let score $s := <a/> where $s return $s", "", root(GFLWOR.class));
    check("let $a := <a/> where $a = '' return data($a)", "", root(GFLWOR.class));
    // skip rewritings: for
    check("for $a score $s in (<a/>, <b/>) where $a/self::a return $s", 0, root(GFLWOR.class));
    check("for $a score $s in <a/> where $a/self::a return $s", 0, root(GFLWOR.class));
  }

  /** Typeswitch: default branch with variable. */
  @Test public void gh1883() {
    query(
      "typeswitch(<x/> update delete node x) " +
      " case $i as xs:int return '-' " +
      " default $i return $i", "<x/>");
  }

  /** Simplify if expressions and comparisons. */
  @Test public void gh1885() {
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'S')", "false\ntrue", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') != 'S')", "true\nfalse", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'P')", "true\nfalse", empty(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') != 'P')", "false\ntrue", empty(If.class));

    check("(0, 1) ! ((if(.) then <_/> else ()) = <_/>)", "false\ntrue", empty(If.class));
    check("(0, 1) ! ((if(.) then (1, 2) else ()) = (1, 2))", "false\ntrue", empty(If.class));
    check("(if((1, 2)[. = 0]) then () else (1, 2)) = (1, 2)", true, empty(If.class), root(NOT));

    // do not rewrite...
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'X')", "false\nfalse", exists(If.class));
    check("(0, 1)[. >= 0] ! ((if(.) then 'S' else 'P') = 'X')", "false\nfalse", exists(If.class));
  }

  /** Optimize inlined path steps. */
  @Test public void gh1886() {
    execute(new CreateDB(NAME, "<_>X</_>"));

    inline(true);
    check("function($db) { $db/UNKNOWN }(.)", "", empty());
    check("let $f := function($a) { $a/UNKNOWN } return ./$f(.)", "", empty());
    check("function($db) { $db/_[. = 'X'] }(.)", "<_>X</_>", root(ValueAccess.class));
  }

  /** Cardinality of self steps. */
  @Test public void gh1887() {
    check("<a/>[count(self::*) = 1]", "<a/>", root(CElem.class));
    check("<a/>[self::* = self::*]", "<a/>", root(CElem.class));
  }

  /** Distinct document order. */
  @Test public void gh1888() {
    check("let $a := <a/> return util:ddo(($a, $a))", "<a/>", root(CElem.class));
    check("let $a := <a/> return ($a, $a)/.", "<a/>", root(CElem.class));
  }

  /** Simple map implementation for two operands. */
  @Test public void gh1889() {
    check("(1 to 2) ! <a/>", "<a/>\n<a/>", root(_UTIL_REPLICATE));
    check("(3 to 13) ! prof:void(.)", "", root(DualMap.class));
  }

  /** Rewrite FLWOR to simple map. */
  @Test public void gh1890() {
    check("for $i in (1 to 2)[. >= 0] return ($i + $i)", "2\n4", root(DualMap.class));
    check("for $a in (1 to 2)[. >= 0] for $b in (1 to 2) return ($a * 2)", "2\n2\n4\n4",
        root(IterMap.class), exists(_UTIL_REPLICATE));
  }

  /** Inlining in update expression. */
  @Test public void gh1891() {
    query("(<a/>, <b/>) update { rename node . as name() }", "<a/>\n<b/>");
    query("(<a/>, <b/>) ! . update { rename node . as name() }", "<a/>\n<b/>");
    query("(<a/>, <b/>) ! (. update { rename node . as name() })", "<a/>\n<b/>");
    query("for $n in (<a/>, <b/>) return $n ! . update { rename node . as name() }", "<a/>\n<b/>");
    query("'s' ! (<a/> update delete node .)", "<a/>");
  }

  /** Static properties of closures. */
  @Test public void gh1892() {
    inline(true);
    check("1 ! (let $a := . return function() { $a }) ! .()", 1, root(Int.class));
    query("function() { for $a in (1, 2) return function() { $a } }() ! .()", "1\n2");
  }

  /** FLWOR, positional variable. */
  @Test public void gh1893() {
    check("for $a at $p in 6 to 8 return $p", "1\n2\n3", root(RangeSeq.class));
    check("for $a at $p in ('a', 'b') return $p", "1\n2", root(RangeSeq.class));
  }

  /** Rewrite simple map to path. */
  @Test public void gh1894() {
    execute(new CreateDB(NAME, "<xml><a/></xml>"));
    check("/xml ! a", "<a/>", root(IterPath.class));
    check("<a/> ! a ! b ! c ! d ! e", "", root(IterPath.class), empty(IterMap.class));
    check("<a/> ! a ! descendant::x", "", root(IterMap.class), exists(IterPath.class));
  }

  /** Inline variables into simple map. */
  @Test public void gh1895() {
    check("let $a := (<a/>, <b/>) return $a ! name()", "a\nb", root(DualMap.class));
    check("let $doc := <e a=''/> return $doc/@a ! node-name()", "a", root(NODE_NAME));
  }

  /** Simple maps, inline operands. */
  @Test public void gh1897() {
    inline(true);
    check("<a/> ! name()", "a", root(Function.NAME));
    check("'s' ! <_>{ . }</_>", "<_>s</_>", root(CElem.class));
    check("count#1 ! .('a')", 1, root(Int.class));

    // do not generate nested node constructors
    check("namespace-uri(<a><b/></a>) ! <x xmlns='x'>{ . }</x> ! name()", "x", root(ItemMap.class));
  }

  /** Rewritings of positional tests. */
  @Test public void gh1898() {
    check("for $a in (1, 2) return $a[$a[.]]", 1, root(GFLWOR.class));
  }

  /** Inline filter expressions. */
  @Test public void gh1899() {
    check("for $a in (1, 'a') let $b := $a[.] return $a[$b]", "1\na", empty(Let.class));
    query("function($a, $b) { $a ! element s { .[$b] } }(<a/>, ())", "<s/>");
  }

  /** Inline cast expressions. */
  @Test public void gh1901() {
    check("<a id='x'/>/@id ! xs:string(.)", "x", root(Cast.class));
    check("<_/>/@id ! data()", "", root(DATA));
    check("<_/>[data()] ! base-uri()", "", root(BASE_URI));
    check("<_/>[not(data())] ! base-uri()", "", root(BASE_URI));
  }

  /** Size of type check results. */
  @Test public void gh1902() {
    inline(true);
    check("function() as xs:string+ { util:replicate(<x/>, 10000000000) }() => count()",
      "10000000000", root(Int.class));
    check("function() as xs:double+ { 1 to 100000000000000 }() => count()",
      "100000000000000", root(Int.class));
  }

  /** Inlining of simple map operands. */
  @Test public void gh1905() {
    check("<a>b</a>/self::a ! string() ! element xml { . }", "<xml>b</xml>");
    check("<a>b</a>/self::a ! string() ! string() ! element xml { . }", "<xml>b</xml>");
    check("<a>b</a>/self::a ! string() ! string() ! string() ! element xml { . }", "<xml>b</xml>");
  }

  /** Type check, refine occurrence indicator. */
  @Test public void gh1906() {
    inline(true);
    check("function() as xs:string* { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string* { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string* { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string* { <_/> }() instance of xs:string ", true, root(Bln.class));

    check("function() as xs:string+ { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string+ { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string+ { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string+ { <_/> }() instance of xs:string ", true, root(Bln.class));

    check("function() as xs:string? { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string? { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string? { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string? { <_/> }() instance of xs:string ", true, root(Bln.class));

    check("function() as xs:string  { <_/> }() instance of xs:string*", true, root(Bln.class));
    check("function() as xs:string  { <_/> }() instance of xs:string+", true, root(Bln.class));
    check("function() as xs:string  { <_/> }() instance of xs:string?", true, root(Bln.class));
    check("function() as xs:string  { <_/> }() instance of xs:string ", true, root(Bln.class));
  }

  /** Atomization of group by expressions. */
  @Test public void gh1907() {
    query("let $i := 1 group by $n := ([], [$i]) return $n", 1);
    query("let $i := 1 group by $n := ([], [$i]) return $i", 1);
    query("let $i := 1 group by $n := ([], [1]) return $i", 1);
    query("let $i := 1 group by $n := ([], []) return $i", 1);
    query("let $i := 1 group by $n := () return $i", 1);
  }

  /** Node constructors, better typing. */
  @Test public void gh1908() {
    check("<a/> treat as element(a)", "<a/>", root(CElem.class));
    check("attribute a {} treat as attribute(a)", "a=\"\"", root(CAttr.class));
    check("<?a ?> treat as processing-instruction(a)", "<?a ?>", root(CPI.class));
    check("<xml:a/> treat as element(xml:a)", "<xml:a/>", root(CElem.class));
    check("element Q{_}a {} treat as element(Q{_}a)", "<a xmlns=\"_\"/>", root(CElem.class));

    check("function() as element(a) { <a/> }() " +
        "instance of element(a)",
        true, root(Bln.class));
    check("function() as attribute(a) { attribute a {} }() " +
        "instance of attribute(a)",
        true, root(Bln.class));
    check("function() as processing-instruction(a) { <?a ?> }() " +
        "instance of processing-instruction(a)",
        true, root(Bln.class));

    check("function() as element(xml:a) { <xml:a/> }() " +
        "instance of element(xml:a)",
        true, root(Bln.class));
    check("function() as element(Q{_}a) { element Q{_}a {} }() " +
        "instance of element(Q{_}a)",
        true, root(Bln.class));
  }

  /** Axis steps, better typing. */
  @Test public void gh1909() {
    // fragments
    inline(true);
    check("function() as element(a)? { <a/>/self::a }()",
        "<a/>", root(IterPath.class));
    check("function() as element(a)? { <a/>/self::Q{}a }()",
        "<a/>", root(IterPath.class));
    check("function() as element(Q{}a)? { <a/>/self::Q{}a }()",
        "<a/>", root(IterPath.class));

    // database nodes
    execute(new CreateDB(NAME, "<x>A</x>"));
    check("function() as element(x)* { x }()[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));
    check("function() as element(x)* { /x }()[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));
    check("function() as element(x)* { db:open('" + NAME + "')/x }()[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));
    check("function() as element(x) { db:open('" + NAME + "')/x }()[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));
    check("function() as document-node() { db:open('" + NAME + "') }()/x[text() = 'A']",
        "<x>A</x>", exists(ValueAccess.class));

    // no rewriting allowed
    check("function() as element(a)? { <a/>/self::*:a }()",
        "<a/>", root(TypeCheck.class));
    check("function() as element(xml:a)? { <xml:a/>/self::xml:* }()",
        "<xml:a/>", root(TypeCheck.class));
    error("function() as element(a)? { <xml:a/>/self::xml:a }()", INVPROMOTE_X_X_X);
  }

  /** Axis followed by attribute step. */
  @Test public void gh1910() {
    check("<x/>//@*", null, type("IterStep[@axis = 'descendant-or-self']", "element()*"));
    check("<x/>/../@*", null, type("IterStep[@axis = 'parent']", "element()?"));
  }

  /** Elvis Operator rewritings. */
  @Test public void gh1911() {
    check("head((<a/>[data()], 1))", 1, root(_UTIL_OR));
    check("head((<a/>[data()], <b/>[data()], <c/>[data()]))", null, count(_UTIL_OR, 2));

    check("head((1, <_/>))", 1, root(Int.class));
    check("head((<item/>, <default/>))", "<item/>", root(CElem.class));

    check("let $a := <a/>[data()] return if($a) then $a else ()", "", root(IterFilter.class));
    check("let $a := <a/>[data()] return if($a) then $a else 0", 0, root(_UTIL_OR));
    check("if(trace(<a/>)) then trace(<a/>) else 0", "<a/>", root(If.class));

    check("let $x := <x/> for $a in 1 to 2 for $b in $x return ($b, $b)[1]", "<x/>\n<x/>",
        root(_UTIL_REPLICATE)
    );
  }

  /** Discard redundant union tests. */
  @Test public void gh1914() {
    check("<a/>/(self::*|self::a)", "<a/>", root(CElem.class));

    check("<a/>/(* | a)", "", type(IterStep.class, "element()*"));
    check("<a/>/(a | *)", "", type(IterStep.class, "element()*"));
    check("<a/>/(a | * | b)", "", type(IterStep.class, "element()*"));

    check("(<a/> | <b/> | <a/>)/self::b", "<b/>", type(Union.class, "element(a)|element(b)+"));
    check("(<a/>, <b/>, <a/>)/self::b", "<b/>", type(Union.class, "element(a)|element(b)+"));
    check("count((<a/>, <b/> | <a/>))", 3, type(List.class, "element(a)|element(b)+"));
  }

  /** Push type checks into expressions. */
  @Test public void gh1915() {
    check("(switch(<_>a</_>) "
        + "  case 'a' return <a/> "
        + "  case 'b' return <b/> "
        + "  default  return error()"
        + ") treat as element(a)",
        "<a/>", exists("SwitchGroup/Treat"));
    check("for $e in (<a/>, <b/>) "
        + "return (typeswitch($e) "
        + "  case element(a) return <a/> "
        + "  case element(b) return <b/> "
        + "  default return error() "
        + ") treat as element()",
        "<a/>\n<b/>", empty(Treat.class));
  }

  /** Rewrite side-effecting let expressions. */
  @Test public void gh1917() {
    check("let $a := (# basex:non-deterministic #) { <a/> } return $a ! name()",
        "a", root(ItemMap.class));
  }

  /** Rewrite list to util:replicate. */
  @Test public void gh1918() {
    check("(1, 1)", "1\n1", root(SingletonSeq.class));
    check("let $a := (<a/>, <a/>) return $a[1] is $a[2]", false, exists(_UTIL_REPLICATE));
  }

  /** Switch expression: static and dynamic cases. */
  @Test public void gh1919() {
    query("switch('x') case <_>x</_>return 1 case 'x' return 2 default return 3", 1);
  }

  /** Switch expression, merge branches. */
  @Test public void gh1920() {
    check("switch(<_/>) case 'a' return 1 case 'b' return 1 case 'c' return 2 default return 3", 3,
        root(Switch.class), count(SwitchGroup.class, 3));
    check("switch(<_/>) case 'a' return 1 case 'b' return 2 case 'c' return 2 default return 3", 3,
        root(Switch.class), count(SwitchGroup.class, 3));
    check("switch(<_/>) case 'a' return 1 case 'b' return 2 case 'c' return 3 default return 3", 3,
        root(Switch.class), count(SwitchGroup.class, 3));

    check("switch(<_>a</_>) case 'a' return 1 case 'b' return 1 default return 2", 1,
        root(If.class));
    check("switch(<_>a</_>) case 'a' return 1 case 'b' return 2 default return 2", 1,
        root(If.class));
    check("switch(<_/>) case 'a' return 1 case 'b' return 1 case 'c' return 1 default return 2", 2,
        root(If.class));

    check("switch(<_>a</_>) case 'a' return 1 default return 1", 1,
        root(Int.class));
    check("switch(<_>a</_>) case 'a' return 1 case 'b' return 1 default return 1", 1,
        root(Int.class));
    check("switch(<_/>) case 'a' return 1 case 'b' return 1 case 'c' return 1 default return 1", 1,
        root(Int.class));
  }

  /** Rewrite integer lists to range sequences. */
  @Test public void gh1924() {
    check("1, 2", "1\n2", root(RangeSeq.class));
    check("-1, 0, 1", "-1\n0\n1", root(RangeSeq.class));
    check("1, 2 to 3", "1\n2\n3", root(RangeSeq.class));
    check("5, 6 to 7, 8", "5\n6\n7\n8", root(RangeSeq.class));
    check("5 to 6, 7 to 8", "5\n6\n7\n8", root(RangeSeq.class));
    check("5 to 6, 8", "5\n6\n8", root(IntSeq.class));
    check("1, 3 to 4", "1\n3\n4", root(IntSeq.class));
  }

  /** DDO property of recursive function. */
  @Test public void gh1927() {
    query("declare function local:a($a) as xs:boolean {"
        + "if ($a) then local:a(text { '' }) "
        + "else if ($a) then local:a($a) "
        + "else false() }; "
        + "local:a(()) ! text { . }", false);
    query("declare function local:a($a) as xs:boolean {"
        + "if ($a) then local:a(text { '' }) "
        + "else if ($a) then local:a($a) "
        + "else false() }; "
        + "let $c := local:a(()) return text { $c }", false);
  }

  /** Pre-evaluate lookup expressions. */
  @Test public void gh1928() {
    // pre-evaluate lookup with zero or single input items
    check("()?1", "", empty());
    check("()?*", "", empty());
    check("map { 1: 2 }?*", 2, root(Int.class));
    check("[ 1 ]?*", 1, root(Int.class));

    // do not pre-evaluate lookups with multiple input items
    check("([ 1 ], map { 2: 3 })?*", "1\n3", root(Lookup.class));
    check("((map { 1: 'A', 'x': 'B' }) treat as function(xs:anyAtomicType) as xs:string)?1",
        "A", root(Lookup.class));
  }

  /** Rewrite distinct sequence checks. */
  @Test public void gh1930() {
    check("(1 to 2)[. = (5, 4, 3, 1, 2, 5)]", "1\n2", exists(RangeSeq.class));
    check("distinct-values((5, 3, 4, 2, 1, 6, 1))", "1\n2\n3\n4\n5\n6", root(RangeSeq.class));
  }

  /** Ancestor steps on database and fragment nodes. */
  @Test public void gh1931() {
    query("<a>{ (<b><c/></b> update {})/c }</a>/c/ancestor::*", "<a>\n<c/>\n</a>");
  }

  /** Rewrite group by to distinct-values(). */
  @Test public void gh1932() {
    check("for $a in 1 to 2 group by $a return $a", "1\n2", root(RangeSeq.class));
    check("for $a in 1 to 2 group by $b := $a return $b", "1\n2", root(RangeSeq.class));
    check("for $a in 1 to 2 group by $b := data($a) return $b", "1\n2", root(RangeSeq.class));

    check("for $a in (1, 2) group by $a return $a", "1\n2", root(RangeSeq.class));
    check("for $a in (1, 3) group by $a return $a", "1\n3", root(SmallSeq.class));
    check("for $a in (1, 'a', 1) group by $a return $a", "1\na", root(SmallSeq.class));
    check("for $a allowing empty in () group by $a return $a", "", empty());

    check("for $p in (1 to 2)[. >= 0] group by $q := string($p) return $q",
        "1\n2", root(DISTINCT_VALUES), exists(DualMap.class));
    check("for $a in (1 to 2)[. >= 0] group by $a return $a promote to xs:double",
        "1\n2", root(DualMap.class));

    check("for $a in (1 to 2)[. >= 0] group by $b := string($a) return $b || 'x'",
        "1x\n2x", count(DualMap.class, 2));

    error("for $a as xs:byte in (1 to 2)[. >= 0] group by $a return $a", INVTREAT_X_X_X);
    error("for $a in (1 to 2)[. >= 0] group by $a as xs:byte := $a return $a", INVTREAT_X_X_X);
  }

  /** Rewrite expression range to util:replicate. */
  @Test public void gh1934() {
    check("for $i in (1 to 2)[. >= 0] return (1 to $i) ! $i", "1\n2\n2",
        root(IterMap.class), exists(_UTIL_REPLICATE));
    check("for $i in (1 to 2)[. >= 0] for $j in 1 to $i return $i", "1\n2\n2",
        root(IterMap.class), exists(_UTIL_REPLICATE));
  }

  /** Iterative path traversal, positional access. */
  @Test public void gh1935() {
    query("declare variable $x := <x><a><_/></a></x>; $x/a ! (_[$x/_ ! 1], .)", "<a>\n<_/>\n</a>");
  }

  /** Positional checks. */
  @Test public void gh1937() {
    check("1[position()]", 1, root(Int.class));
    check("(1, 3)[position()]", "1\n3", root(IntSeq.class));
    check("('a', 'b')[position()[position() ! .]]", "a\nb", root(StrSeq.class));

    check("(1, 3, 5)[last() - 2]", 1, root(Int.class));
    check("(1, 3, 5)[position() = last() - 1]", 3, root(Int.class));
    check("(1, 3, 5, 7)[position() = last() idiv 2]", 3, root(Int.class));
    check("(1, 3, 5, 7)[position() = last() div 2]", 3, root(Int.class));

    check("(1, 3, 5)[not(position() = 2)]", "1\n5", root(SmallSeq.class));
    check("(1, 3, 5)[not(position() > 2)]", "1\n3", root(SubSeq.class));
    check("(1, 3, 5)[not(position() < 2)]", "3\n5", root(SubSeq.class));
    check("(1, <_/>[data()])[not(position() = 2)]", 1, root(REMOVE));

    check("for $i in (1 to 2)[. >= 0] return (3 to 4)[not(position() = $i)]",
        "4\n3", exists(REMOVE));
    check("for $i in (1 to 2)[. >= 0] return (3 to 4)[not(position() > $i)]",
        "3\n3\n4", exists(_UTIL_RANGE));
    check("for $i in (1 to 2)[. >= 0] return (3 to 4)[not(position() < $i)]",
        "3\n4\n4", exists(_UTIL_RANGE));
  }

  /** Simple map, context value. */
  @Test public void gh1941() {
    query("<a/> ! <b>{ (<c>{ . }</c>, .) ! name()[. = 'a'] }</b>", "<b>a</b>");
  }

  /** If expression, boolean/not. */
  @Test public void gh1942() {
    check("for $a in (false(), true()) for $b in (false(), true()) return "
        + "if($a) then $b else not($b)", "true\nfalse\nfalse\ntrue", empty(If.class));
    check("for $a in (false(), true()) for $b in (false(), true()) return "
        + "if($a) then not($b) else $b", "false\ntrue\ntrue\nfalse", empty(If.class));

    check("for $a in (false(), true()) for $b in (0, 1) return "
        + "if($a) then not($b) else boolean($b)", "false\ntrue\ntrue\nfalse", empty(If.class));
    check("for $a in (false(), true()) for $b in (0, 1) return "
        + "if($a) then boolean($b) else not($b)", "true\nfalse\nfalse\ntrue", empty(If.class));

    check("for $a in (1, 2) for $b in (false(), true()) return "
        + "if($a) then $b else not($b)", "false\ntrue\nfalse\ntrue", empty(If.class));
    check("for $a in (1, 2) for $b in (false(), true()) return "
        + "if($a) then not($b) else $b", "true\nfalse\ntrue\nfalse", empty(If.class));

    check("let $a := <a/>[. = ''] for $b in (false(), true()) return "
        + "if($b) then exists($a) else empty($a)", "false\ntrue", empty(If.class));
    check("let $a := <a/>[. = ''] for $b in (false(), true()) return "
        + "if($b) then empty($a) else exists($a)", "true\nfalse", empty(If.class));

    check("let $a := <a/> for $b in (false(), true()) return "
        + "if($b) then $a = '' else $a != ''", "false\ntrue", empty(If.class));
    check("let $a := <a/> for $b in (false(), true()) return "
        + "if($b) then $a != '' else $a = ''", "true\nfalse", empty(If.class));
  }

  /** Inline arguments of replicated items and singleton sequences. */
  @Test public void gh1963() {
    check("util:replicate('x', 2) ! (. = 'x')", "true\ntrue", root(SingletonSeq.class));
    check("util:replicate(<a/>, 2) ! (. = 'x')", "false\nfalse", root(_UTIL_REPLICATE));

    check("(10, 10) ! .[. = 5]", "", root(IterFilter.class));
    check("util:replicate(<a/>, 2) ! .[data()]", "", root(IterFilter.class));
  }

  /** Inlining context item expression. */
  @Test public void gh1964() {
    query("let $a := <a/> return <b/>[. ! ./$a]", "<b/>");
    query("let $a := <a/> return <b/>[. ! data()[. = $a]]", "");
  }

  /** Faster fn:count operations. */
  @Test public void gh1965() {
    check("(1 to 2)[. = 1] => sort() => count()", 1, empty(SORT));
    check("(1 to 2)[. = 1] => reverse() => count()", 1, empty(REVERSE));
    check("(for $i in (1 to 2)[. = 1] order by $i return $i ) => count()", 1, empty(OrderBy.class));
  }

  /** Rewrite order by to fn:sort. */
  @Test public void gh1966() {
    check("for $i in (1 to 2)[. >= 0] order by 1 return $i", "1\n2",
        empty(OrderBy.class), root(IterFilter.class));
    check("for $i in (1 to 2)[. >= 0] order by 1 descending return $i", "1\n2",
        empty(OrderBy.class), root(IterFilter.class));

    check("for $i in (1 to 2)[. >= 0] order by $i return $i", "1\n2",
        empty(OrderBy.class), root(SORT));
    check("for $i in (1 to 2)[. >= 0] order by $i descending return $i", "2\n1",
        empty(OrderBy.class), root(REVERSE));
    check("for $i in (1 to 2)[. >= 0] order by $i return $i * 2", "2\n4",
        empty(OrderBy.class), root(DualMap.class));
    check("let $_ := <_>1</_> for $i in (1 to 2)[. >= 0] order by $i return $i + $_", "2\n3",
        empty(OrderBy.class), root(GFLWOR.class));

    check("for $i in (1 to 2)[. >= 0] order by $i empty greatest return $i", "1\n2",
        exists(OrderBy.class));
    check("for $i in (1 to 2)[. >= 0] order by $i collation '?lang=de' return $i", "1\n2",
        exists(OrderBy.class));
    check("for $i in ([ 1 ], [ 2 ]) order by $i return $i?*", "1\n2",
        exists(OrderBy.class));
    check("for $a in (1 to 2)[. >= 0] for $b in 3 to 4 order by $a return $a", "1\n1\n2\n2",
        exists(OrderBy.class));
    check("for $i in (1 to 2)[. >= 0] order by $i, 1 return $i", "1\n2",
        exists(OrderBy.class));
    check("for $i in (1 to 2)[. >= 0] order by -$i return $i", "2\n1",
        exists(OrderBy.class));
  }

  /** distinct-values: simplify arguments. */
  @Test public void gh1967() {
    check("count(distinct-values((1 to 100000000, 1 to 100000000)))", 100000000, root(Int.class));
    check("count(distinct-values((1 to 100000000, 1)))", 100000000, root(Int.class));
    check("count(distinct-values((1 to 100000000, 1, 0, 100000000, 100, 10 to 10000, 100000001)))",
        100000002, root(Int.class));
  }

  /** Existence checks, filter expressions. */
  @Test public void gh1971() {
    check("for $i in (1 to 2)[. >= 0] return exists($i[. = 1])", "true\nfalse",
        root(DualMap.class), exists(CmpSimpleG.class));
  }

  /** Parallel execution. */
  @Test public void gh1329() {
    query("xquery:fork-join(util:replicate(function() { <x/>[@a] }, 100000))", "");
    query("1!fn:last#0()", 1);
  }

  /** Range and arithmetic expression. */
  @Test public void gh1972() {
    check("(1 to 2) ! (. + 1)", "2\n3", root(RangeSeq.class));
    check("reverse(1 to 2) ! (. - 1)", "1\n0", root(RangeSeq.class));
    check("reverse((1 to 2) ! (. - 1))", "1\n0", root(RangeSeq.class));
  }

  /** EBV tests, count -> exists. */
  @Test public void gh1974() {
    check("boolean(count((1, 2)[. <= 2]))", true, root(EXISTS));
    check("boolean(count((1, 2)[. >= 3]))", false, root(EXISTS));

    check("boolean(string-length(<_>A</_>))", true, exists(STRING));
    check("boolean(string-length(<_/>))", false, exists(STRING));
  }

  /** Axis steps: Rewrites. */
  @Test public void gh1976() {
    check("document { }/parent::node()", "", root(Empty.class));

    check("attribute a {}/child::document-node()", "", root(Empty.class));
    check("attribute a {}/self::document-node()", "", root(Empty.class));
    check("attribute a {}/descendant-or-self::document-node()", "", root(Empty.class));
    check("attribute a {}/descendant-or-self::*", "", root(Empty.class));

    check("text { '' }/child::document-node()", "", root(Empty.class));
    check("text { '' }/self::document-node()", "", root(Empty.class));
    check("text { '' }/descendant-or-self::document-node()", "", root(Empty.class));
    check("text { '' }/descendant-or-self::*", "", root(Empty.class));

    check("document { }/ancestor-or-self::node()", "", root(CDoc.class));
    check("attribute a {}/descendant-or-self::attribute()", "a=\"\"", root(CAttr.class));
    check("text { '' }/descendant-or-self::text()", "", root(CTxt.class));
  }

  /** descendant-or-self -> descendant. */
  @Test public void gh1979() {
    check("document { <a/> }/descendant-or-self::a", "<a/>",
        exists("IterStep[@axis = 'descendant']"));
    check("<a/>/ancestor-or-self::document-node()", "",
        exists("IterStep[@axis = 'ancestor']"));
    check("document { <a b='c'/> }/descendant::a//@*", "b=\"c\"",
        exists("IterStep[@axis = 'descendant']"));
    check("<a b='c'/>/descendant-or-self::attribute()", "", root(Empty.class));
  }

  /** Lookup operator, iterative evaluation. */
  @Test public void gh1984() {
    query("head(([ 2 ], 3) ?1)", 2);
    query("[ map { }, map { } ] ?* [ ?* ]", "");
  }

  /** Simple maps, group by: Context check. */
  @Test public void gh1987() {
    query("function($m) { function() { $m?* } ! (" +
        "for $b in $m?0 let $c := .() group by $d := () return $c" +
        ")}(map { 0: 1 })", 1);
  }

  /** Casts, cardinality tests. */
  @Test public void gh1998() {
    check("zero-or-one((1 to 2)[. = 2]) cast as xs:decimal?", 2, empty(ZERO_OR_ONE));
    check("exactly-one((1 to 2)[. = 2]) cast as xs:decimal", 2, empty(EXACTLY_ONE));
    check("zero-or-one((1 to 2)[. = 2]) cast as xs:decimal", 2, empty(ZERO_OR_ONE));
    check("one-or-more((1 to 2)[. = 2]) cast as xs:decimal", 2, empty(ONE_OR_MORE));

    check("xs:decimal(exactly-one((1 to 2)[. = 2]))", 2, exists(EXACTLY_ONE));
    check("xs:decimal(one-or-more((1 to 2)[. = 2]))", 2, exists(ONE_OR_MORE));

    check("zero-or-one((1 to 2)[. = 3]) promote to empty-sequence()", "", empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) promote to xs:decimal" , 2, empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) promote to xs:decimal?", 2, empty(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) promote to xs:decimal+", 2, exists(ZERO_OR_ONE));
    check("zero-or-one((1 to 2)[. = 2]) promote to xs:decimal*", 2, exists(ZERO_OR_ONE));

    check("exactly-one((1 to 2)[. = 2]) promote to xs:decimal" , 2, empty(EXACTLY_ONE));
    check("exactly-one((1 to 2)[. = 2]) promote to xs:decimal?", 2, exists(EXACTLY_ONE));
    check("exactly-one((1 to 2)[. = 2]) promote to xs:decimal+", 2, exists(EXACTLY_ONE));
    check("exactly-one((1 to 2)[. = 2]) promote to xs:decimal*", 2, exists(EXACTLY_ONE));

    check("one-or-more((1 to 2)[. = 2]) promote to xs:decimal" , 2, empty(ONE_OR_MORE));
    check("one-or-more((1 to 2)[. = 2]) promote to xs:decimal?", 2, exists(ONE_OR_MORE));
    check("one-or-more((1 to 2)[. = 2]) promote to xs:decimal+", 2, empty(ONE_OR_MORE));
    check("one-or-more((1 to 2)[. = 2]) promote to xs:decimal*", 2, exists(ONE_OR_MORE));
  }

  /** Inline transform-with expression. */
  @Test public void gh2000() {
    inline(true);
    check("function() { <a/> update { delete node b } }()", "<a/>", root(TransformWith.class));
  }

  /** Dynamic unroll limit.. */
  @Test public void gh2001() {
    check("(1, 3) ! (. * 2)", "2\n6", exists(Arith.class));
    check("sum((1, 3) ! (. * 2))", 8, exists(Arith.class));
    check("sum((# db:unrolllimit 6 #) { (1 to 6) ! (. * 2) })", 42, empty(Arith.class));
    check("(1, 2)[. = 1]", 1, root(IterFilter.class));

    unroll(true);
    check("(1, 3) ! (. * 2)", "2\n6", empty(Arith.class));
    check("sum((# db:unrolllimit 0 #) { (1, 3) ! (. * 2) })", 8, exists(Arith.class));
    check("(1, 2)[. = 1]", 1, root(Int.class));
  }

  /** Identical nodes in mixed paths. */
  @Test public void gh2005() {
    check("declare context item := <a/>; (.|.)/self::a", "<a/>", empty(Union.class));

    check("<a/> ! (., .)/<b/>", "<b/>\n<b/>", exists(_UTIL_REPLICATE));
    check("util:replicate(<a/>, 2)/<b/>", "<b/>\n<b/>", exists(_UTIL_REPLICATE));
    check("declare context item := <a/>; (., .)/<b/>", "<b/>\n<b/>", exists(SingletonSeq.class));
  }
}
