package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.QueryText;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Token;
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
    check(MAPNEW);
    query(EXISTS.args(MAPNEW.args("()")), true);
    query(MAPSIZE.args(MAPNEW.args("()")), 0);
    query(COUNT.args(MAPNEW.args("()")), 1);
    query(MAPSIZE.args(MAPNEW.args(MAPNEW.args("()"))), 0);
  }

  /**
   * Test method for the map:entry() function.
   */
  @Test
  public void mapEntry() {
    check(MAPENTRY);
    query(EXISTS.args(MAPENTRY.args("A", "B")), true);
    query(EXISTS.args(MAPENTRY.args(1, 2)), true);
    query(EXISTS.args(MAPNEW.args(MAPENTRY.args(1, 2))), "true");
    error(EXISTS.args(MAPENTRY.args("()", 2)), Err.XPTYPE);
    error(EXISTS.args(MAPENTRY.args("(1,2)", 2)), Err.XPTYPE);
  }

  /**
   * Test method for the map:get() function.
   */
  @Test
  public void mapGet() {
    check(MAPGET);
    query(MAPGET.args(MAPNEW.args("()"), 1), "");
    query(MAPGET.args(MAPENTRY.args(1, 2), 1), 2);
  }

  /**
   * Test method for the map:contains() function.
   */
  @Test
  public void mapContains() {
    check(MAPCONT);
    query(MAPCONT.args(MAPNEW.args(), 1), false);
    query(MAPCONT.args(MAPENTRY.args(1, 2), 1), true);
  }

  /**
   * Test method for the map:remove() function.
   */
  @Test
  public void mapRemove() {
    check(MAPREM);
    query(MAPSIZE.args(MAPREM.args(MAPENTRY.args(1, 2), 1)), 0);
  }

  /**
   * Test method for the map:size() function.
   */
  @Test
  public void mapSize() {
    check(MAPSIZE);
    query(MAPSIZE.args(MAPENTRY.args(1, 2)), 1);
  }

  /**
   * Test method for the map:keys() function.
   */
  @Test
  public void mapKeys() {
    check(MAPKEYS);
    query("for $i in " + MAPKEYS.args(
        MAPNEW.args(" for $i in 1 to 3 return " +
        MAPENTRY.args("$i", "$i+1"))) + " order by $i return $i", "1 2 3");
    query("let $map := " + MAPNEW.args(" for $i in 1 to 3 return " +
        MAPENTRY.args("$i", "$i + 1")) +
        "for $k in " + MAPKEYS.args("$map") + " order by $k return " +
        MAPGET.args("$map", "$k"), "2 3 4");
  }

  /**
   * Test method for the map:collation() function.
   */
  @Test
  public void mapCollation() {
    check(MAPCOLL);
    query(MAPCOLL.args(MAPNEW.args()), Token.string(QueryText.URLCOLL));
  }
}
