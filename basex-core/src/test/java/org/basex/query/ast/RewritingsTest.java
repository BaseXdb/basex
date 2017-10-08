package org.basex.query.ast;

import static org.basex.query.func.Function.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
    check("count(1)", "1", "exists(//" + Util.className(Int.class) + ')');

    execute(new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a><a/></xml>"));
    check("count(//a)", "3", "exists(//" + Util.className(Int.class) + ')');
    check("count(/xml/a)", "3", "exists(//" + Util.className(Int.class) + ')');
    check("count(//text())", "2", "exists(//" + Util.className(Int.class) + ')');
    check("count(//*)", "4", "exists(//" + Util.className(Int.class) + ')');
    check("count(//node())", "6", "exists(//" + Util.className(Int.class) + ')');
    check("count(//comment())", "0", "exists(//" + Util.className(Int.class) + ')');
    check("count(/self::document-node())", "1", "exists(//" + Util.className(Int.class) + ')');
    execute(new DropDB(NAME));
  }

  /** Checks if descendant-or-self::node() steps are rewritten. */
  @Test public void mergeDesc() {
    execute(new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>"));

    check("//*", null, "//@axis = 'descendant'");
    check("//(b,*)", null, "exists(//Union) and //@axis = 'descendant'");
    check("//(b|*)", null, "exists(//Union) and //@axis = 'descendant'");
    check("//(b|*)[text()]", null, "exists(//Union) and //@axis = 'descendant'");
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
    query("not(<a/>[b])", "true");
    query("empty(<a/>[b])", "true");
    query("exists(<a/>[b])", "false");

    query("not(<a/>[b = 'c'])", "true");
    query("empty(<a/>[b = 'c'])", "true");
    query("exists(<a/>[b = 'c'])", "false");

    query("let $n := <n/> where $n[<a><b/><b/></a>/*] return $n", "<n/>");

    check("empty(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("exists(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("boolean(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("not(<a>X</a>[text()])", null, "//@axis = 'child'");

    check("if(<a>X</a>[text()]) then 1 else 2", null, "//@axis = 'child'");
    check("<a>X</a>[text()] and <a/>", null, "//@axis = 'child'");
    check("<a>X</a>[text()] or <a/>", null, "//Bln/@value = 'true'");
    check("<a>X</a>[text()] or <a/>[text()]", null, "//@axis = 'child'");
    check("for $a in <a>X</a> where $a[text()] return $a", null, "//@axis = 'child'");

    check("empty(<a>X</a>/.[text()])", null, "//@axis = 'child'");
  }

  /** Checks if iterative evaluation of XPaths is used iff no duplicated occur (see GH-1001). */
  @Test public void iterPath() {
    execute(new CreateDB(NAME, "<a id='0' x:id='' x='' xmlns:x='x'><b id='1'/><c id='2'/>"
        + "<d id='3'/><e id='4'/></a>"));
    check("(/a/*/../*) ! name()", "b\nc\nd\ne", "empty(//IterPath)");
    check("(exactly-one(/a/b)/../*) ! name()", "b\nc\nd\ne", "exists(//IterPath)");
    check("(/a/*/following::*) ! name()", "c\nd\ne", "empty(//IterPath)");
    check("(exactly-one(/a/b)/following::*) ! name()", "c\nd\ne", "exists(//IterPath)");
    check("(/a/*/following-sibling::*) ! name()", "c\nd\ne", "empty(//IterPath)");
    check("(exactly-one(/a/b)/following-sibling::*) ! name()", "c\nd\ne", "exists(//IterPath)");
    check("(/*/@id/../*) ! name()", "b\nc\nd\ne", "empty(//IterPath)");
    check("(exactly-one(/a)/@id/../*) ! name()", "b\nc\nd\ne", "exists(//IterPath)");
    execute(new DropDB(NAME));
  }

  /** Checks OR optimizations. */
  @Test public void or() {
    check("('' or '')", "false", "empty(//Or)");
    check("('x' or 'x' = 'x')", "true", "empty(//Or)");
    check("(false()   or <x/> = 'x')", "false", "empty(//Or)");
    check("(true()    or <x/> = 'x')", "true", "empty(//Or)");
    check("('x' = 'x' or <x/> = 'x')", "true", "empty(//Or)");

    // {@link CmpG} rewritings
    check("let $x := <x/>     return ($x = 'x' or $x = 'y')", "false", "empty(//Or)");
    check("let $x := <x>x</x> return ($x = 'x' or $x = 'y')", "true",  "empty(//Or)");
  }

  /** Checks AND optimizations. */
  @Test public void and() {
    check("('x' and 'y')", "true", "empty(//And)");
    check("('x' and 'x' = 'x')", "true", "empty(//And)");
    check("(true()    and <x>x</x> = 'x')", "true", "empty(//And)");
    check("(false()   and <x>x</x> = 'x')", "false", "empty(//And)");
    check("('x' = 'x' and <x>x</x> = 'x')", "true", "empty(//And)");

    // {@link Pos} rewritings
    check("(<a/>,<b/>)[last()]", "<b/>",
        "count(//" + Util.className(UtilLastFrom.class) + ") = 1");
    check("(<a/>,<b/>)[position() > 1 and position() < 3]", "<b/>",
        "count(//" + Util.className(UtilItemAt.class) + ") = 1");
    check("(<a/>,<b/>)[position() > 1 and position() < 4]", "<b/>",
        "count(//" + Util.className(UtilItemRange.class) + ") = 1");
    check("(<a/>,<b/>)[position() > 1 and position() < 3 and <b/>]", "<b/>",
        "count(//" + Util.className(UtilItemAt.class) + ") = 1");

    // {@link CmpR} rewritings
    check("<a>5</a>[text() > 1 and text() < 9]", "<a>5</a>", "count(//CmpR) = 1");
    check("<a>5</a>[text() > 1 and text() < 9 and <b/>]", "<a>5</a>", "count(//CmpR) = 1");
    check("<a>5</a>[text() > 1 and . < 9]", "<a>5</a>", "count(//CmpG) = 1 and count(//CmpR) = 1");

    // {@link CmpSR} rewritings
    check("<a>5</a>[text() > '1' and text() < '9']", "<a>5</a>", "count(//CmpSR) = 1");
    check("<a>5</a>[text() > '1' and text() < '9' and <b/>]", "<a>5</a>", "count(//CmpSR) = 1");
    check("<a>5</a>[text() > '1' and . < '9']", "<a>5</a>", "count(//CmpSR) = 2");
  }

  /** Checks string-length optimizations. */
  @Test public void stringLength() {
    final String filter = Util.className(IterFilter.class);
    final String string = Util.className(FnString.class);
    final String stringLength = Util.className(FnStringLength.class);

    check("<a/>[" + STRING_LENGTH.args() + " >  -1]", "<a/>", "empty(//" + filter + ')');
    check("<a/>[" + STRING_LENGTH.args() + " != -1]", "<a/>", "empty(//" + filter + ')');
    check("<a/>[" + STRING_LENGTH.args() + " ge  0]", "<a/>", "empty(//" + filter + ')');
    check("<a/>[" + STRING_LENGTH.args() + " ne 1.1]", "<a/>", "empty(//" + filter + ')');

    check("<a/>[" + STRING_LENGTH.args() + " <   0]", "", "empty(//" + filter + ')');
    check("<a/>[" + STRING_LENGTH.args() + " <= -1]", "", "empty(//" + filter + ')');
    check("<a/>[" + STRING_LENGTH.args() + " eq -1]", "", "empty(//" + filter + ')');
    check("<a/>[" + STRING_LENGTH.args() + " eq 1.1]", "", "empty(//" + filter + ')');

    check("<a/>[" + STRING_LENGTH.args() + " >  0]", "", "exists(//" + string + ')');
    check("<a/>[" + STRING_LENGTH.args() + " >= 0.5]", "", "exists(//" + string + ')');
    check("<a/>[" + STRING_LENGTH.args() + " ne 0]", "", "exists(//" + string + ')');

    check("<a/>[" + STRING_LENGTH.args() + " <  0.5]", "<a/>", "exists(//" + string + ')');
    check("<a/>[" + STRING_LENGTH.args() + " <= 0.5]", "<a/>", "exists(//" + string + ')');
    check("<a/>[" + STRING_LENGTH.args() + " eq 0]", "<a/>", "exists(//" + string + ')');

    check("<a/>[" + STRING_LENGTH.args() + " gt 1]", "", "exists(//" + stringLength + ')');

    check("<a/>[" + STRING_LENGTH.args() + " = <a>1</a>]", "", "exists(//" + stringLength + ')');
  }

  /** Checks that empty sequences are eliminated and that singleton lists are flattened. */
  @Test
  public void list() {
    check("((), <x/>, ())", "<x/>", "empty(//List)", "empty(//Empty)", "exists(/*/CElem)");
  }

  /** Checks that expressions marked as non-deterministic will not be rewritten. */
  @Test
  public void nonDeterministic() {
    check("count((# basex:non-deterministic #) { <x/> })", "1", "exists(//FnCount)");
  }

  /** Ensures that fn:doc with URLs will not be rewritten. */
  @Test
  public void doc() {
    check("<a>{ doc('" + FILE + "') }</a>//x", "",
        "exists(//" + Util.className(DBNode.class) + ')');
    check("if(<x>1</x> = 1) then 2 else doc('" + FILE + "')", "2",
        "exists(//" + Util.className(DBNode.class) + ')');
    check("if(<x>1</x> = 1) then 2 else doc('http://abc.de/')", "2",
        "exists(//" + Util.className(FnDoc.class) + ')');
    check("if(<x>1</x> = 1) then 2 else collection('http://abc.de/')", "2",
        "exists(//" + Util.className(FnCollection.class) + ')');
  }

  /** Positional predicates. */
  @Test public void pos() {
    // check if positional predicates are pre-evaluated
    check("'a'[1]", "a", "exists(//Str)");
    check("'a'[position() = 1]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() = 1 to 2]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() > 0]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() < 2]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() >= 1]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() <= 1]", "a", "exists(QueryPlan/Str)");

    // check if positional predicates are rewritten to utility functions
    check("for $i in (1,2) return ('a')[position() = $i]", "a", "exists(//UtilItemAt)");
    check("for $i in (1,2) return ('a')[position() = $i to $i]", "a", "exists(//UtilItemAt)");
    check("for $i in (1,2) return ('a')[position() = $i to $i+1]", "a", "exists(//UtilItemRange)");
    check("for $i in (1,2) return ('a')[position() = $i to 1]", "a", "exists(//UtilItemRange)");
    check("for $i in (1,2) return ('a')[position() >= $i]", "a", "exists(//UtilItemRange)");
    check("for $i in (1,2) return ('a')[position() > $i]", "", "exists(//UtilItemRange)");
    check("for $i in (1,2) return ('a')[position() <= $i]", "a\na", "exists(//UtilItemRange)");
    check("for $i in (1,2) return ('a')[position() < $i]", "a", "exists(//UtilItemRange)");

    // check if positional predicates are rewritten to utility functions
    final String seq = " (1, 1.1, 1.9, 2) ";
    check("for $i in" + seq + "return ('a','b')[position() = $i]", "a\nb", "exists(//UtilItemAt)");
    check("for $i in" + seq + "return ('a','b')[position() >= $i]", "a\nb\nb\nb\nb",
        "exists(//UtilItemRange)");
    check("for $i in" + seq + "return ('a','b')[position() > $i]", "b\nb\nb",
        "exists(//UtilItemRange)");
    check("for $i in" + seq + "return ('a','b')[position() <= $i]", "a\na\na\na\nb",
        "exists(//UtilItemRange)");
    check("for $i in" + seq + "return ('a','b')[position() < $i]", "a\na\na",
        "exists(//UtilItemRange)");

    // check if multiple positional predicates are rewritten to utility functions
    check("for $i in" + seq + "return ('a','b')[position() = $i][position() = $i]", "a",
        "count(//UtilItemAt) = 2");
    check("for $i in" + seq + "return ('a','b')[position() < $i][position() < $i]", "a\na\na",
        "count(//UtilItemRange) = 2");

    // check if positional predicates are merged and rewritten to utility functions
    check("for $i in" + seq + "return ('a','b')[position() = $i and position() = $i]", "a\nb",
        "exists(//UtilItemAt)");
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() <= $i]", "a\nb",
        "exists(//UtilItemRange)");
    check("for $i in" + seq + "return ('a','b')[position() <= $i and position() >= $i]", "a\nb",
        "exists(//UtilItemRange)");
    check("for $i in" + seq + "return ('a','b')[position() > $i and position() < $i]", "",
        "exists(//UtilItemRange)");
    check("for $i in" + seq + "return ('a','b')[position() < $i and position() > $i]", "",
        "exists(//UtilItemRange)");

    // no rewriting possible (conflicting positional predicates)
    check("for $i in" + seq + "return ('a','b')[position() = $i and position() = $i+1]", "",
        "exists(//CachedFilter)");
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() > $i]", "b\nb\nb",
        "exists(//CachedFilter)");
    check("for $i in" + seq + "return ('a','b')[position() >= $i and position() >= $i+1]", "b",
        "exists(//CachedFilter)");
    check("for $i in" + seq + "return ('a','b')[position() < $i and position() < $i+1]", "a\na\na",
        "exists(//CachedFilter)");
  }

  /** Predicates. */
  @Test public void preds() {
    // context value: rewrite if root is of type string or node
    check("'s'[.]", "s", "exists(//ContextValue)");
    check("<a/>[.]", "<a/>", "exists(QueryPlan/CElem) and empty(//ContextValue)");
    check("<a/>[.][.]", "<a/>", "exists(QueryPlan/CElem) and empty(//ContextValue)");
    check("<a/>/self::*[.][.]", "<a/>", "empty(//ContextValue)");
    check("<a/>/self::*[.][.]", "<a/>", "empty(//ContextValue)");
    check("('a','b')[position()[position() ! .]]", "a\nb", "count(.//FnPosition) = 2");
    check("('a','b')[. ! position()]", "a", "exists(.//*[contains(name(), 'Map')])");
    check("1[.]", "1", "exists(//ContextValue)");
    check("let $x := (<a/>,<a/>) where $x[. eq ''] return $x", "<a/>\n<a/>",
        "exists(.//ContextValue)");
    error("true#0[.]", QueryError.EBV_X_X);

    // map expression
    check("'s'['s' ! <a/>]", "s", "empty(QueryPlan//*[contains(name(), 'Map')])");
    check("'s'['s' ! <a/> ! <b/>]", "s", "exists(QueryPlan//*[contains(name(), 'Map')])");

    // path expression
    check("let $a := <a/> return $a[$a/self::a]", "<a/>", "count(//VarRef) = 1");
    check("let $a := <a/> return $a[$a]", "<a/>", "count(//VarRef) = 1");
  }
}
