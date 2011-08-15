package org.basex.test.query.func;

import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the XQuery functions prefixed with "map".
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNMapTest extends AdvancedQueryTest {
  /**
   * Test method for the map:new() function.
   */
  @Test
  public void mapNew() {
    final String fun = check(Function.MAPNEW);
    query("exists(" + fun + "())", "true");
    query("map:size(" + fun + "())", "0");
    query("count(" + fun + "())", "1");
    query("map:size(" + fun + "(" + fun + "()))", "0");
  }

  /**
   * Test method for the map:entry() function.
   */
  @Test
  public void mapEntry() {
    final String fun = check(Function.MAPENTRY);
    query("exists(" + fun + "('a', 'b'))", "true");
    query("exists(" + fun + "(1, 2))", "true");
    query("exists(map:new(" + fun + "(1, 2)))", "true");
    error("exists(" + fun + "((), 2))", Err.XPTYPE);
    error("exists(" + fun + "((1,2), 2))", Err.XPTYPE);
  }

  /**
   * Test method for the map:get() function.
   */
  @Test
  public void mapGet() {
    final String fun = check(Function.MAPGET);
    query(fun + "(map:new(), 1)", "");
    query(fun + "(map:entry(1,2), 1)", "2");
  }

  /**
   * Test method for the map:contains() function.
   */
  @Test
  public void mapContains() {
    final String fun = check(Function.MAPCONT);
    query(fun + "(map:new(), 1)", "false");
    query(fun + "(map:entry(1,2), 1)", "true");
  }

  /**
   * Test method for the map:remove() function.
   */
  @Test
  public void mapRemove() {
    final String fun = check(Function.MAPREM);
    query("map:size(" + fun + "(map:entry(1,2),1))", "0");
  }

  /**
   * Test method for the map:size() function.
   */
  @Test
  public void mapSize() {
    final String fun = check(Function.MAPSIZE);
    query(fun + "(map:entry(1,2))", "1");
  }

  /**
   * Test method for the map:keys() function.
   */
  @Test
  public void mapKeys() {
    final String fun = check(Function.MAPKEYS);
    query("for $i in " + fun + "(map:new(" +
        "for $i in 1 to 3 return map:entry($i, $i+1))) order by $i return $i",
      "1 2 3");
    query("let $map := map:new(for $i in 1 to 3 return map:entry($i, $i + 1))" +
      "for $k in " + fun + "($map) order by $k return map:get($map, $k)",
      "2 3 4");
  }

  /**
   * Test method for the map:collation() function.
   */
  @Test
  public void mapCollation() {
    final String fun = check(Function.MAPCOLL);
    query(fun + "(map:new())",
        "http://www.w3.org/2005/xpath-functions/collation/codepoint");
  }
}
