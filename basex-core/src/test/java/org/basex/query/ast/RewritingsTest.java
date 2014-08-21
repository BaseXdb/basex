package org.basex.query.ast;

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
}
