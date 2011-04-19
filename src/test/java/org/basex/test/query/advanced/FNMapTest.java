package org.basex.test.query.advanced;

import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.junit.Test;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNMapTest extends AdvancedQueryTest {
  /**
   * Test method for the map:new() functions.
   * @throws QueryException database exception
   */
  @Test
  public void testNew() throws QueryException {
    final String fun = check(FunDef.MAPNEW, Object.class, String.class);
    query("map:size(" + fun + "())", "0");
    query("count(" + fun + "())", "1");
    query("map:size(" + fun + "(" + fun + "()))", "0");
  }

  /**
   * Test method for the map:entry() functions.
   * @throws QueryException database exception
  @Test
  public void testEntry() throws QueryException {
    final String fun = check(FunDef.ENTRY, Object.class, Object.class);
    query("map:size(" + fun + "('a','b'))", "0");
  }
   */
}
