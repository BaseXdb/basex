package org.basex.query.ast;

import static org.basex.query.func.Function.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.junit.Test;

/**
 * Checks query rewritings.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class RewritingsTest extends QueryPlanTest {
  /** Input file. */
  private static final String FILE = "src/test/resources/input.xml";

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
    execute(new DropDB(NAME));
  }

  /** Checks if descendant-or-self::node() steps are rewritten. */
  @Test public void mergeDesc() {
    execute(new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>"));

    check("//*", null, "//@axis = 'descendant'");
    check("//(b,*)", null, exists(IterPath.class), "//@axis = 'descendant'");
    check("//(b|*)", null, exists(IterPath.class), "//@axis = 'descendant'");
    check("//(b|*)[text()]", null, exists(IterPath.class), empty(Union.class),
        "//@axis = 'descendant'");
    check("//(b,*)[1]", null, "not(//@axis = 'descendant')");
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
    execute(new DropDB(NAME));
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
    check("1[. = 1] = 1 to 2", true, exists(cmpir));
    check("1[. = 2] = 1 to 2", false, exists(cmpir));
    check("1[. = 2] = 1 to 2", false, exists(cmpir));

    // do not rewrite equality comparisons against single integers
    check("1[. = 1] = 1", true, empty(cmpir));

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
    check("(1234567890.12345678)[. = 1234567890.1234567]", "", empty(cmpr));
    check("(123456789012345678 )[. = 123456789012345679]", "", empty(cmpr));

    // rewrite equality comparisons
    check("1[. = 1] >= 1.0", true, exists(cmpr));
    check("1[. = 1] >= 1e0", true, exists(cmpr));
    check("1e0[. = 1] >= 1", true, exists(cmpr));
    check("1e0[. = 1] >= 1e0", true, exists(cmpr));
    check("<_>1.1</_> >= 1.1", true, exists(cmpr));

    // do not rewrite decimal/double comparisons
    check("1e0[. = 1] >= 1.0", true, empty(cmpr));
    check("1.0[. = 1] >= 1e0", true, empty(cmpr));

    // do not rewrite equality comparisons
    check("1[. = 1] = 1.0", true, empty(cmpr));
    check("1[. = 1] = 1e0", true, empty(cmpr));
    check("1e0[. = 1] = 1", true, empty(cmpr));
    check("1e0[. = 1] = 1.0", true, empty(cmpr));
    check("1e0[. = 1] = 1e0", true, empty(cmpr));
    check("<_>1.1</_> = 1.1", true, empty(cmpr));

    // suppressed rewritings
    check("random:double() = 2", false, empty(cmpr));
    check("1.1[. != 0] = 1.3", false, empty(cmpr));
    check("'x'[. = 'x'] = 'x'", true, empty(cmpr));
    check("'x'[. != 'x'] = 1.3", false, empty(cmpr));

    check("1.1[. = 1.1] = 1.1", true, empty(cmpr));

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

    check("<a/>[string-length() >  0]", "", exists(STRING));
    check("<a/>[string-length() >= 0.5]", "", exists(STRING));
    check("<a/>[string-length() ne 0]", "", exists(STRING));

    check("<a/>[string-length() <  0.5]", "<a/>", exists(STRING));
    check("<a/>[string-length() <= 0.5]", "<a/>", exists(STRING));
    check("<a/>[string-length() eq 0]", "<a/>", exists(STRING));

    check("<a/>[string-length() gt 1]", "", exists(STRING_LENGTH));
    check("<a/>[string-length() = <a>1</a>]", "", exists(STRING_LENGTH));
  }

  /** Checks count optimizations. */
  @Test public void count() {
    // static occurrence: zero-or-one
    String count = "count(1[. = 1])";

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
    check(count + " >  0", true, root(EXISTS));
    check(count + " >= 1", true, root(EXISTS));
    check(count + " != 0", true, root(EXISTS));
    check(count + " <  1", false, root(EMPTY));
    check(count + " <= 0", false, root(EMPTY));
    check(count + " =  0", false, root(EMPTY));

    // zero-or-one result: no need to evaluate count
    check(count + " <  2", true, root(Bln.class));
    check(count + " <= 1", true, root(Bln.class));
    check(count + " != 2", true, root(Bln.class));
    check(count + " >  1", false, root(Bln.class));
    check(count + " >= 2", false, root(Bln.class));
    check(count + " =  2", false, root(Bln.class));

    // no rewritings possible
    check(count + " != 1", false, exists(COUNT));
    check(count + " =  1", true, exists(COUNT));

    // one-or-more results: no need to evaluate count
    count = "count((1, 1[. = 1]))";
    check(count + " >  0", true, root(Bln.class));
    check(count + " >= 1", true, root(Bln.class));
    check(count + " != 0", true, root(Bln.class));
    check(count + " <  1", false, root(Bln.class));
    check(count + " <= 0", false, root(Bln.class));
    check(count + " =  0", false, root(Bln.class));

    // no rewritings possible
    check(count + " =  1", false, exists(COUNT));
    check(count + " =  2", true, exists(COUNT));
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
    check("for $i in (1,2) return 'a'[$i]", "a", exists(_UTIL_ITEM));
    check("for $i in (1,2) return 'a'[position() = $i]", "a", exists(_UTIL_ITEM));
    check("for $i in (1,2) return 'a'[position() = $i to $i]", "a", exists(_UTIL_ITEM));
    check("for $i in (1,2) return 'a'[position() = $i to $i+1]", "a", exists(_UTIL_RANGE));
    check("for $i in (1,2) return 'a'[position() = $i to 1]", "a", exists(_UTIL_RANGE));
    check("for $i in (1,2) return 'a'[position() >= $i]", "a", exists(_UTIL_RANGE));
    check("for $i in (1,2) return 'a'[position() > $i]", "", exists(_UTIL_RANGE));
    check("for $i in (1,2) return 'a'[position() <= $i]", "a\na", exists(_UTIL_RANGE));
    check("for $i in (1,2) return 'a'[position() < $i]", "a", exists(_UTIL_RANGE));

    // check if positional predicates are rewritten to utility functions
    final String seq = " (1, 1.1, 1.9, 2) ";
    check("for $i in" + seq + "return ('a','b')[$i]", "a\nb",
        exists(_UTIL_ITEM));
    check("for $i in" + seq + "return ('a','b')[position() = $i]", "a\nb",
        exists(_UTIL_ITEM));
    check("for $i in" + seq + "return ('a','b')[position() >= $i]", "a\nb\nb\nb\nb",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a','b')[position() > $i]", "b\nb\nb",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a','b')[position() <= $i]", "a\na\na\na\nb",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a','b')[position() < $i]", "a\na\na",
        exists(_UTIL_RANGE));

    // check if multiple positional predicates are rewritten to utility functions
    check("for $i in" + seq + "return ('a','b')[$i][$i]", "a",
        count(_UTIL_ITEM, 2));
    check("for $i in" + seq + "return ('a','b')[position() = $i][position() = $i]", "a",
        count(_UTIL_ITEM, 2));
    check("for $i in" + seq + "return ('a','b')[position() < $i][position() < $i]", "a\na\na",
        count(_UTIL_RANGE, 2));

    // check if positional predicates are merged and rewritten to utility functions
    check("for $i in" + seq + "return ('a','b')[position() = $i and position() = $i]", "a\nb",
        exists(_UTIL_ITEM));
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() <= $i]", "a\nb",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a','b')[position() <= $i and position() >= $i]", "a\nb",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a','b')[position() > $i and position() < $i]", "",
        exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a','b')[position() < $i and position() > $i]", "",
        exists(_UTIL_RANGE));

    // no rewriting possible (conflicting positional predicates)
    check("for $i in" + seq + "return ('a','b')[position() = $i and position() = $i+1]", "",
        exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() > $i]", "b\nb\nb",
        exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() >= $i+1]", "b",
        exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a','b')[position() < $i and position() < $i+1]", "a\na\na",
        exists(CachedFilter.class));

    check("(<a/>,<b/>)[last()]", "<b/>", count(_UTIL_LAST, 1));
    check("(<a/>,<b/>)[position() > 1 and position() < 3]", "<b/>", count(_UTIL_LAST, 1));
    check("(<a/>,<b/>)[position() > 1 and position() < 3 and <b/>]", "<b/>", count(_UTIL_LAST, 1));
    check("(<a/>,<b/>)[position() > 1 and position() < 4]", "<b/>", count(_UTIL_LAST, 1));
  }

  /** Predicates. */
  @Test public void preds() {
    // context value: rewrite if root is of type string or node
    check("'s'[.]", "s", exists(ContextValue.class));
    check("<a/>[.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>[.][.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("('a','b')[position()[position() ! .]]", "a\nb", count(POSITION, 2));
    check("('a','b')[. ! position()]", "a", exists("*[contains(name(), 'Map')]"));
    check("1[.]", 1, exists(ContextValue.class));
    check("let $x := (<a/>,<a/>) where $x[. eq ''] return $x", "<a/>\n<a/>", exists(CmpG.class));
    error("true#0[.]", QueryError.EBV_X_X);

    // map expression
    check("'s'['s' ! <a/>]", "s", empty(IterMap.class));
    check("'s'['s' ! <a/>]", "s", root(Str.class));
    check("'s'['x' ! <a/> ! <b/>]", "s", root(Str.class));
    check("'s'['x' ! (<a/>,<b/>) ! <b/>]", "s", root(Str.class));
    check("'s'['x' ! <a>{ . }</a>[. = 'x']]", "s", empty(IterMap.class), root(If.class));

    // path expression
    check("let $a := <a/> return $a[$a/self::a]", "<a/>", count(VarRef.class, 1));
    check("let $a := <a/> return $a[$a]", "<a/>", empty(VarRef.class));

    // if expression
    check("for $t in ('a','b') return $t[$t]", "a\nb", exists(If.class));
  }

  /** Comparison expressions. */
  @Test public void cmpG() {
    check("count(let $s := (0,1 to 99999) return $s[. = $s])", 100000, exists(CmpHashG.class));
  }

  /** Checks OR optimizations. */
  @Test public void gh1519() {
    query("declare function local:replicate($seq, $n, $out) {"
        + "  if($n eq 0) then $out "
        + "  else ( "
        + "    let $out2 := if($n mod 2 eq 0) then $out else ($out, $seq) "
        + "    return local:replicate(($seq, $seq), $n idiv 2, $out2) "
        + "  )"
        + "};"
        + "let $n := 1000000000 "
        + "return ( "
        + "  count(local:replicate((1,2,3), $n, ())) eq 3 * $n, "
        + "  count(local:replicate((1,2,3), $n, ())) = 3 * $n "
        + ")", "true\ntrue");
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
    check("(<a/>, 1[.=0])/node()", "", exists(IterStep.class));

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
    check("(<a/>,comment{})/child::namespace-node()", "", empty());
    check("(<a/>,comment{})/descendant::namespace-node()", "", empty());
    check("(<a/>,comment{})/attribute::namespace-node()", "", empty());
    check("(<a/>,comment{})/self::namespace-node()", "", exists(IterStep.class));
    check("(<a/>,comment{})/descendant-or-self::namespace-node()", "", exists(IterStep.class));
  }

  /** Casts. */
  @Test public void cast() {
    check("for $n in 1 to 3 return xs:integer($n)[. = 1]", 1, empty(Cast.class));
    check("('a','b') ! xs:string(.)", "a\nb", empty(Cast.class), root(StrSeq.class));
  }

  /** Casts. */
  @Test public void typeCheck() {
    check("declare function local:a($e) as xs:string? { local:b($e) }; " +
        "declare function local:b($e) as xs:string? { $e }; local:a(<_>X</_>)", "X",
        count(TypeCheck.class, 1));
    check("declare function local:a($e) as xs:string? { local:b($e) }; " +
        "declare function local:b($e) as xs:string* { $e }; local:a(<_>X</_>)", "X",
        count(TypeCheck.class, 1));
    check("declare function local:a($e) as xs:string* { local:b($e) }; " +
        "declare function local:b($e) as xs:string? { $e }; local:a(<_>X</_>)", "X",
        count(TypeCheck.class, 1));
  }

  /** GH1694. */
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
    execute(new DropDB(NAME));
  }

  /** GH1726. */
  @Test public void gh1726() {
    final String query =
      "let $xml := if(1[.]) then ( " +
      "  element a { element b { } update { } } " +
      ") else ( " +
      "  error() " +
      ") " +
      "let $b := $xml/* " +
      "return ($b, $b/..)";

    query(query, "<b/>\n<a>\n<b/>\n</a>");
  }

  /** GH1723. */
  @Test public void gh1723() {
    check("count(<a/>)", 1, root(Int.class));
    check("count(<a/>/<b/>)", 1, root(Int.class));
    check("count(<a/>/<b/>/<c/>/<d/>)", 1, root(Int.class));
  }

  /** GH1733. */
  @Test public void gh1733() {
    check("(<a/>, <b/>)/1", "1\n1", root(SingletonSeq.class));
    check("<a/>/1", 1, root(Int.class));
    check("<a/>/<a/>", "<a/>", root(CElem.class));
    check("(<a/>/map { 1:2 })?1", 2, root(Int.class));
    check("<a/>/self::a/count(.)", 1, root("ItemMap"));

    // no rewriting possible
    check("(<a/>, <b/>)/<c/>", "<c/>\n<c/>", root(MixedPath.class));
  }

  /** GH1741. */
  @Test public void gh1741() {
    check("<a/>/<b/>[1]", "<b/>", root(CElem.class));
    check("<a/>/.[1]", "<a/>", root(CElem.class));
    check("<doc><x/><y/></doc>/*/..[1] ! name()", "doc", empty(ItrPos.class));

    check("<a/>/<b/>[2]", "", root(Empty.class));
    check("<a/>/.[2]", "", root(Empty.class));
    check("<doc><x/><y/></doc>/*/..[2] ! name()", "", root(Empty.class));
  }

  /** GH1737: combined kind tests. */
  @Test public void gh1737() {
    // merge identical steps, rewrite to iterative path
    check("<a/>/(*|*)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(*,*)", "", root(IterPath.class), empty(List.class));

    // rewrite to single union node test, rewrite to iterative path
    check("<a/>/(a|b)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a,b)", "", root(IterPath.class), empty(List.class));

    // merge descendant-or-self step, rewrite to iterative path
    check("<a/>//(a|b)", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a,b)", "", root(IterPath.class), empty(List.class));

    // rewrite to single union node test, rewrite to iterative path
    check("<a/>/(a|b)[text()]", "", root(IterPath.class), empty(Union.class));
    check("<a/>/(a,b)[text()]", "", root(IterPath.class), empty(List.class));
    check("<_><a>x</a><b/></_>/(a,b)[text()]", "<a>x</a>", root(IterPath.class), empty(List.class));

    // rewrite to union expression
    check("<a/>/(*,@*)", "", root(MixedPath.class), exists(Union.class));
  }

  /** GH1761: merge adjacent steps in path expressions. */
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
    check("document { <a/>,<b/> }/(a,b)/self::a", "<a/>", count(IterStep.class, 1));
    check("document { <a/>,<b/> }/a/(self::a, self::b)", "<a/>", count(IterStep.class, 1));
    check("document { <a/>,<b/> }/(a,b)/(self::b, self::a)", "<a/>\n<b/>",
        count(IterStep.class, 1));
  }

  /** GH1762: merge steps and predicates with self steps. */
  @Test public void gh1762() {
    // merge self steps
    check("<a/>/self::*[self::a]", "<a/>", count(IterStep.class, 1));
    check("<a/>/self::*[self::b]", "", count(IterStep.class, 1));
    check("<a/>/self::a[self::*]", "<a/>", count(IterStep.class, 1));
    check("<a/>/self::a[self::node()]", "<a/>", count(IterStep.class, 1));

    // nested predicates
    check("<a/>/self::a[self::a[self::a[self::a]]]", "<a/>", count(IterStep.class, 1));

    // combined kind test
    check("document { <a/>,<b/> }/a[self::a|self::b]", "<a/>", count(IterStep.class, 1));
  }

  /** Path tests. */
  @Test public void gh1729() {
    check("let $x := 'g' return <g/> ! self::g[name() = $x]", "<g/>", empty(CachedPath.class));
  }

  /** Path tests. */
  @Test public void gh1752() {
    check("'1' ! json:parse(.)/descendant::*[text()] = 1", true, empty(IterMap.class));
  }

  /** Static typing of computed maps. */
  @Test public void gh1766() {
    check("let $map := map { 'c': 'x' } return count($map?(('a','b')))", 0, root(Int.class));
  }

  /** Rewrite boolean comparisons. */
  @Test public void gh1775() {
    check("(false(), true()) ! (.  = true())", "false\ntrue", root(BlnSeq.class));
    check("(false(), true()) ! (. != false())", "false\ntrue", root(BlnSeq.class));

    check("(false(), true()) ! (.  = false())", "true\nfalse", exists(NOT));
    check("(false(), true()) ! (. != true())", "true\nfalse", exists(NOT));
  }

  /** Merge predicates. */
  @Test public void gh1777() {
    check("6[. > 5][. < 7]", 6, count(CmpIR.class, 1));
    check("('a', 'b')[2][2]", "", empty());
  }

  /** Merge conjunctions. */
  @Test public void gh1776() {
    check("for $n in (1,2,3) where $n != 2 and $n != 3 return $n", 1,
        exists(NOT), exists(IntSeq.class));

    check("'A'[. = <a>A</a> and . = 'A']", "A", exists(NOT), exists(List.class));
    check("(<a/>, <b/>, <c/>)[name() = 'a' and name() = 'b']", "",
        exists(NOT), exists(StrSeq.class));
  }
}
