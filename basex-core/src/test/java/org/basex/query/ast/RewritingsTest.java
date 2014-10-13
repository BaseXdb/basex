package org.basex.query.ast;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Checks query rewritings.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RewritingsTest extends QueryPlanTest {
  /**
   * Checks if the count function is pre-compiled.
   * @throws Exception exception
   */
  @Test
  public void preEval() throws Exception {
    check("count(1)", "1", "exists(//" + Util.className(Int.class) + ')');

    new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a><a/></xml>").execute(context);
    check("count(//a)", "3", "exists(//" + Util.className(Int.class) + ')');
    check("count(/xml/a)", "3", "exists(//" + Util.className(Int.class) + ')');
    check("count(//text())", "2", "exists(//" + Util.className(Int.class) + ')');
    check("count(//*)", "4", "exists(//" + Util.className(Int.class) + ')');
    check("count(//node())", "6", "exists(//" + Util.className(Int.class) + ')');
    check("count(//comment())", "0", "exists(//" + Util.className(Int.class) + ')');
    check("count(/self::document-node())", "1", "exists(//" + Util.className(Int.class) + ')');
    new DropDB(NAME).execute(context);
  }

  /**
   * Checks if descendant-or-self::node() steps are rewritten.
   * @throws Exception exception
   */
  @Test
  public void mergeDesc() throws Exception {
    new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>").execute(context);

    check("//*", null, "//@axis = 'descendant'");
    check("//(b,*)", null, "exists(//Union) and //@axis = 'descendant'");
    check("//(b|*)", null, "exists(//Union) and //@axis = 'descendant'");
    check("//(b|*)[text()]", null, "exists(//Union) and //@axis = 'descendant'");
    check("//(b,*)[1]", null, "not(//@axis = 'descendant')");
  }

  /**
   * Checks if descendant steps are rewritten to child steps.
   * @throws Exception exception
   */
  @Test
  public void descToChild() throws Exception {
    new CreateDB(NAME, "<a><b>B</b><b><c>C</c></b></a>").execute(context);

    check("descendant::a", null, "//@axis = 'child'");
    check("descendant::b", null, "//@axis = 'child'");
    check("descendant::c", null, "//@axis = 'child'");
    check("descendant::*", null, "not(//@axis = 'child')");
  }

  /**
   * Checks EBV optimizations.
   */
  @Test
  public void optimizeEbv() {
    query("not(<a/>[b])", "true");
    query("empty(<a/>[b])", "true");
    query("exists(<a/>[b])", "false");

    query("not(<a/>[b = 'c'])", "true");
    query("empty(<a/>[b = 'c'])", "true");
    query("exists(<a/>[b = 'c'])", "false");

    check("empty(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("exists(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("boolean(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("not(<a>X</a>[text()])", null, "//@axis = 'child'");

    check("if(<a>X</a>[text()]) then 1 else 2", null, "//@axis = 'child'");
    check("<a>X</a>[text()] and <a/>", null, "//@axis = 'child'");
    check("<a>X</a>[text()] or <a/>", null, "//@axis = 'child'");
    check("for $a in <a>X</a> where $a[text()] return $a", null, "//@axis = 'child'");

    check("empty(<a>X</a>/.[text()])", null, "//@axis = 'child'");
  }

  /**
   * Checks if iterative evaluation of XPaths is used iff no duplicated occur (see GH-1001).
   * @throws BaseXException if creating or dropping the database fails
   */
  @Test
  public void iterPath() throws BaseXException {
    new CreateDB(NAME, "<a id='0' x:id='' x='' xmlns:x='x'><b id='1'/><c id='2'/>"
        + "<d id='3'/><e id='4'/></a>").execute(context);
    check("(/a/*/../*) ! name()", "b c d e", "empty(//IterPath)");
    check("(exactly-one(/a/b)/../*) ! name()", "b c d e", "exists(//IterPath)");
    check("(/a/*/following::*) ! name()", "c d e", "empty(//IterPath)");
    check("(exactly-one(/a/b)/following::*) ! name()", "c d e", "exists(//IterPath)");
    check("(/a/*/following-sibling::*) ! name()", "c d e", "empty(//IterPath)");
    check("(exactly-one(/a/b)/following-sibling::*) ! name()", "c d e", "exists(//IterPath)");
    check("(/*/@id/../*) ! name()", "b c d e", "empty(//IterPath)");
    check("(exactly-one(/a)/@id/../*) ! name()", "b c d e", "exists(//IterPath)");
    new DropDB(NAME).execute(context);
  };

  /**
   * Checks OR optimizations.
   */
  @Test
  public void or() {
    check("('' or '')", "false", "empty(//Or)");
    check("('x' or 'x' = 'x')", "true", "empty(//Or)");
    check("(false()   or <x/> = 'x')", "false", "empty(//Or)");
    check("(true()    or <x/> = 'x')", "true", "empty(//Or)");
    check("('x' = 'x' or <x/> = 'x')", "true", "empty(//Or)");

    // {@link CmpG} rewritings
    check("let $x := <x/>     return ($x = 'x' or $x = 'y')", "false", "empty(//Or)");
    check("let $x := <x>x</x> return ($x = 'x' or $x = 'y')", "true",  "empty(//Or)");
  }

  /**
   * Checks AND optimizations.
   */
  @Test
  public void and() {
    check("('x' and 'y')", "true", "empty(//And)");
    check("('x' and 'x' = 'x')", "true", "empty(//And)");
    check("(true()    and <x>x</x> = 'x')", "true", "empty(//And)");
    check("(false()   and <x>x</x> = 'x')", "false", "empty(//And)");
    check("('x' = 'x' and <x>x</x> = 'x')", "true", "empty(//And)");

    // {@link Pos} rewritings
    check("(<a/>,<b/>)[position() > 1 and position() < 3]", "<b/>", "count(//Pos) = 1");
    check("(<a/>,<b/>)[position() > 1 and position() < 3 and <b/>]", "<b/>", "count(//Pos) = 1");
    // {@link CmpR} rewritings
    check("<a>5</a>[text() > 1 and text() < 9]", "<a>5</a>", "count(//CmpR) = 1");
    check("<a>5</a>[text() > 1 and text() < 9 and <b/>]", "<a>5</a>", "count(//CmpR) = 1");
    check("<a>5</a>[text() > 1 and . < 9]", "<a>5</a>", "count(//CmpG) = 1 and count(//CmpR) = 1");
    // {@link CmpSR} rewritings
    check("<a>5</a>[text() > '1' and text() < '9']", "<a>5</a>", "count(//CmpSR) = 1");
    check("<a>5</a>[text() > '1' and text() < '9' and <b/>]", "<a>5</a>", "count(//CmpSR) = 1");
    check("<a>5</a>[text() > '1' and . < '9']", "<a>5</a>", "count(//CmpSR) = 2");
  }
}
