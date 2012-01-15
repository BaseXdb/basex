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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNMapTest extends AdvancedQueryTest {
  /**
   * Test method for the map:new() function.
   */
  @Test
  public void mapNew() {
    check(_MAP_NEW);
    query(EXISTS.args(_MAP_NEW.args("()")), true);
    query(_MAP_SIZE.args(_MAP_NEW.args("()")), 0);
    query(COUNT.args(_MAP_NEW.args("()")), 1);
    query(_MAP_SIZE.args(_MAP_NEW.args(_MAP_NEW.args("()"))), 0);
  }

  /**
   * Test method for the map:entry() function.
   */
  @Test
  public void mapEntry() {
    check(_MAP_ENTRY);
    query(EXISTS.args(_MAP_ENTRY.args("A", "B")), true);
    query(EXISTS.args(_MAP_ENTRY.args(1, 2)), true);
    query(EXISTS.args(_MAP_NEW.args(_MAP_ENTRY.args(1, 2))), "true");
    error(EXISTS.args(_MAP_ENTRY.args("()", 2)), Err.XPTYPE);
    error(EXISTS.args(_MAP_ENTRY.args("(1,2)", 2)), Err.XPTYPE);
  }

  /**
   * Test method for the map:get() function.
   */
  @Test
  public void mapGet() {
    check(_MAP_GET);
    query(_MAP_GET.args(_MAP_NEW.args("()"), 1), "");
    query(_MAP_GET.args(_MAP_ENTRY.args(1, 2), 1), 2);
  }

  /**
   * Test method for the map:contains() function.
   */
  @Test
  public void mapContains() {
    check(_MAP_CONTAINS);
    query(_MAP_CONTAINS.args(_MAP_NEW.args(), 1), false);
    query(_MAP_CONTAINS.args(_MAP_ENTRY.args(1, 2), 1), true);
  }

  /**
   * Test method for the map:remove() function.
   */
  @Test
  public void mapRemove() {
    check(_MAP_REMOVE);
    query(_MAP_SIZE.args(_MAP_REMOVE.args(_MAP_ENTRY.args(1, 2), 1)), 0);
  }

  /**
   * Test method for the map:size() function.
   */
  @Test
  public void mapSize() {
    check(_MAP_SIZE);
    query(_MAP_SIZE.args(_MAP_ENTRY.args(1, 2)), 1);
  }

  /**
   * Test method for the map:keys() function.
   */
  @Test
  public void mapKeys() {
    check(_MAP_KEYS);
    query("for $i in " + _MAP_KEYS.args(
        _MAP_NEW.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args("$i", "$i+1"))) + " order by $i return $i", "1 2 3");
    query("let $map := " + _MAP_NEW.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args("$i", "$i + 1")) +
        "for $k in " + _MAP_KEYS.args("$map") + " order by $k return " +
        _MAP_GET.args("$map", "$k"), "2 3 4");
  }

  /**
   * Test method for the map:collation() function.
   */
  @Test
  public void mapCollation() {
    check(_MAP_COLLATION);
    query(_MAP_COLLATION.args(_MAP_NEW.args()),
        Token.string(QueryText.URLCOLL));
  }
}
