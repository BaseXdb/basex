package org.basex.query.ast;

import static org.basex.query.func.Function.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Checks query rewritings.
 *
 * @author BaseX Team 2005-17, BSD License
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
    check("//(b,*)", null, exists(Union.class), "//@axis = 'descendant'");
    check("//(b|*)", null, exists(Union.class), "//@axis = 'descendant'");
    check("//(b|*)[text()]", null, exists(Union.class), "//@axis = 'descendant'");
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

  /** Checks if iterative evaluation of XPaths is used iff no duplicated occur (see GH-1001). */
  @Test public void iterPath() {
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

    // {@link Pos} rewritings
    final String uia = Util.className(_UTIL_ITEM_AT.clazz);
    final String uir = Util.className(_UTIL_ITEM_RANGE.clazz);
    final String ulf = Util.className(_UTIL_LAST_FROM.clazz);
    check("(<a/>,<b/>)[last()]", "<b/>", count(ulf , 1));
    check("(<a/>,<b/>)[position() > 1 and position() < 3]", "<b/>", count(uia , 1));
    check("(<a/>,<b/>)[position() > 1 and position() < 4]", "<b/>", count(uir , 1));
    check("(<a/>,<b/>)[position() > 1 and position() < 3 and <b/>]", "<b/>", count(uia , 1));

    // {@link CmpR} rewritings
    check("<a>5</a>[text() > 1 and text() < 9]", "<a>5</a>", count(CmpR.class, 1));
    check("<a>5</a>[text() > 1 and text() < 9 and <b/>]", "<a>5</a>", count(CmpR.class, 1));
    check("<a>5</a>[text() > 1 and . < 9]", "<a>5</a>", count(CmpR.class, 2));
    check("<a>5</a>[text() > 800000000]", "", exists(CmpR.class));
    check("<a>5</a>[text() < -800000000]", "", exists(CmpR.class));
    check("exists(<x>1234567890.12345678</x>[. = 1234567890.1234567])", true, exists(CmpR.class));

    // no {@link CmpR} rewritings
    check("exists(<x>123456789012345678</x> [. = 123456789012345679])", true, empty(CmpR.class));
    check("<a>5</a>[text() > 8000000000]", "", empty(CmpR.class));
    check("<a>5</a>[text() < -8000000000]", "", empty(CmpR.class));
    check("(1234567890.12345678)[. = 1234567890.1234567]", "", empty(CmpR.class));
    check("(123456789012345678 )[. = 123456789012345679]", "", empty(CmpR.class));

    // {@link CmpSR} rewritings
    check("<a>5</a>[text() > '1' and text() < '9']", "<a>5</a>", count(CmpSR.class, 1));
    check("<a>5</a>[text() > '1' and text() < '9' and <b/>]", "<a>5</a>", count(CmpSR.class, 1));
    check("<a>5</a>[text() > '1' and . < '9']", "<a>5</a>", count(CmpSR.class, 2));
  }

  /** Checks string-length optimizations. */
  @Test public void stringLength() {
    final String filter = Util.className(IterFilter.class);
    final String string = Util.className(FnString.class);
    final String stringLength = Util.className(FnStringLength.class);

    check("<a/>[string-length() >  -1]", "<a/>", empty(filter));
    check("<a/>[string-length() != -1]", "<a/>", empty(filter));
    check("<a/>[string-length() ge  0]", "<a/>", empty(filter));
    check("<a/>[string-length() ne 1.1]", "<a/>", empty(filter));

    check("<a/>[string-length() <   0]", "", empty(filter));
    check("<a/>[string-length() <= -1]", "", empty(filter));
    check("<a/>[string-length() eq -1]", "", empty(filter));
    check("<a/>[string-length() eq 1.1]", "", empty(filter));

    check("<a/>[string-length() >  0]", "", exists(string));
    check("<a/>[string-length() >= 0.5]", "", exists(string));
    check("<a/>[string-length() ne 0]", "", exists(string));

    check("<a/>[string-length() <  0.5]", "<a/>", exists(string));
    check("<a/>[string-length() <= 0.5]", "<a/>", exists(string));
    check("<a/>[string-length() eq 0]", "<a/>", exists(string));

    check("<a/>[string-length() gt 1]", "", exists(stringLength));

    check("<a/>[string-length() = <a>1</a>]", "", exists(stringLength));
  }

  /** Checks that empty sequences are eliminated and that singleton lists are flattened. */
  @Test
  public void list() {
    check("((), <x/>, ())", "<x/>", empty(List.class), empty(Empty.class), exists(CElem.class));
  }

  /** Checks that expressions marked as non-deterministic will not be rewritten. */
  @Test
  public void nonDeterministic() {
    check("count((# basex:non-deterministic #) { <x/> })", 1, exists(FnCount.class));
  }

  /** Ensures that fn:doc with URLs will not be rewritten. */
  @Test
  public void doc() {
    check("<a>{ doc('" + FILE + "') }</a>//x", "",
        exists(DBNode.class));
    check("if(<x>1</x> = 1) then 2 else doc('" + FILE + "')", 2,
        exists(DBNode.class));
    check("if(<x>1</x> = 1) then 2 else doc('http://abc.de/')", 2,
        exists(FnDoc.class));
    check("if(<x>1</x> = 1) then 2 else collection('http://abc.de/')", 2,
        exists(FnCollection.class));
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
    final String uia = Util.className(_UTIL_ITEM_AT.clazz);
    final String uir = Util.className(_UTIL_ITEM_RANGE.clazz);
    check("for $i in (1,2) return 'a'[$i]", "a", exists(uia));
    check("for $i in (1,2) return 'a'[position() = $i]", "a", exists(uia));
    check("for $i in (1,2) return 'a'[position() = $i to $i]", "a", exists(uia));
    check("for $i in (1,2) return 'a'[position() = $i to $i+1]", "a", exists(uir));
    check("for $i in (1,2) return 'a'[position() = $i to 1]", "a", exists(uir));
    check("for $i in (1,2) return 'a'[position() >= $i]", "a", exists(uir));
    check("for $i in (1,2) return 'a'[position() > $i]", "", exists(uir));
    check("for $i in (1,2) return 'a'[position() <= $i]", "a\na", exists(uir));
    check("for $i in (1,2) return 'a'[position() < $i]", "a", exists(uir));

    // check if positional predicates are rewritten to utility functions
    final String seq = " (1, 1.1, 1.9, 2) ";
    check("for $i in" + seq + "return ('a','b')[$i]", "a\nb", exists(uia));
    check("for $i in" + seq + "return ('a','b')[position() = $i]", "a\nb", exists(uia));
    check("for $i in" + seq + "return ('a','b')[position() >= $i]", "a\nb\nb\nb\nb", exists(uir));
    check("for $i in" + seq + "return ('a','b')[position() > $i]", "b\nb\nb", exists(uir));
    check("for $i in" + seq + "return ('a','b')[position() <= $i]", "a\na\na\na\nb", exists(uir));
    check("for $i in" + seq + "return ('a','b')[position() < $i]", "a\na\na", exists(uir));

    // check if multiple positional predicates are rewritten to utility functions
    check("for $i in" + seq + "return ('a','b')[$i][$i]", "a", count(UtilItemAt.class, 2));
    check("for $i in" + seq + "return ('a','b')[position() = $i][position() = $i]", "a",
        count(UtilItemAt.class, 2));
    check("for $i in" + seq + "return ('a','b')[position() < $i][position() < $i]", "a\na\na",
        count(uir , 2));

    // check if positional predicates are merged and rewritten to utility functions
    check("for $i in" + seq + "return ('a','b')[position() = $i and position() = $i]", "a\nb",
        exists(UtilItemAt.class));
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() <= $i]", "a\nb",
        exists(uir));
    check("for $i in" + seq + "return ('a','b')[position() <= $i and position() >= $i]", "a\nb",
        exists(uir));
    check("for $i in" + seq + "return ('a','b')[position() > $i and position() < $i]", "",
        exists(uir));
    check("for $i in" + seq + "return ('a','b')[position() < $i and position() > $i]", "",
        exists(uir));

    // no rewriting possible (conflicting positional predicates)
    check("for $i in" + seq + "return ('a','b')[position() = $i and position() = $i+1]", "",
        exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() > $i]", "b\nb\nb",
        exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() >= $i+1]", "b",
        exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a','b')[position() < $i and position() < $i+1]", "a\na\na",
        exists(CachedFilter.class));
  }

  /** Predicates. */
  @Test public void preds() {
    // context value: rewrite if root is of type string or node
    check("'s'[.]", "s", exists(ContextValue.class));
    check("<a/>[.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>[.][.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("('a','b')[position()[position() ! .]]", "a\nb", count(FnPosition.class, 2));
    check("('a','b')[. ! position()]", "a", exists("*[contains(name(), 'Map')]"));
    check("1[.]", 1, exists(ContextValue.class));
    check("let $x := (<a/>,<a/>) where $x[. eq ''] return $x", "<a/>\n<a/>", exists(CmpG.class));
    error("true#0[.]", QueryError.EBV_X_X);

    // map expression
    check("'s'['s' ! <a/>]", "s", empty("*[contains(name(), 'Map')]"));
    check("'s'['s' ! <a/>]", "s", "exists(QueryPlan/Str)");
    check("'s'['x' ! <a/> ! <b/>]", "s", "exists(QueryPlan/Str)");
    check("'s'['x' ! (<a/>,<b/>) ! <b/>]", "s", "exists(QueryPlan/Str)");
    check("'s'['x' ! <a>{ . }</a>[. = 'x']]", "s", "exists(QueryPlan//*[contains(name(), 'Map')])");

    // path expression
    check("let $a := <a/> return $a[$a/self::a]", "<a/>", count(VarRef.class, 1));
    check("let $a := <a/> return $a[$a]", "<a/>", count(VarRef.class, 1));
  }

  /** Comparison expressions. */
  @Test public void cmpG() {
    check("count(let $s := (0,1 to 99999) return $s[. = $s])", 100000, exists(CmpHashG.class));
  }

  /** Checks OR optimizations (GH-1519). */
  @Test public void optCount() {
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
}
