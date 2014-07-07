package org.basex.query.ast;

import org.basex.core.cmd.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Checks for testing re-evaluation of query expressions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class PreEvalTest extends QueryPlanTest {
  /**
   * Checks if the count function is pre-compiled.
   * @throws Exception exception
   */
  @Test
  public void count() throws Exception {
    check("count(1)", "1", "exists(//" + Util.className(Int.class) + ')');

    new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a><a/></xml>").execute(context);
    check("count(//a)", "3", "exists(//" + Util.className(Int.class) + ')');
    check("count(/xml/a)", "3", "exists(//" + Util.className(Int.class) + ')');
    check("count(//text())", "2", "exists(//" + Util.className(Int.class) + ')');
    check("count(//*)", "4", "exists(//" + Util.className(Int.class) + ')');
    check("count(//node())", "6", "exists(//" + Util.className(Int.class) + ')');
    check("count(//comment())", "0", "exists(//" + Util.className(Int.class) + ')');
    check("count(//processing-instruction())", "0", "exists(//" + Util.className(Int.class) + ')');
    check("count(/self::document-node())", "1", "exists(//" + Util.className(Int.class) + ')');
    new DropDB(NAME).execute(context);
  }
}
