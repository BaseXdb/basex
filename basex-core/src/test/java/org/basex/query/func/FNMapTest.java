package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the functions of the Map Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNMapTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void newTest() {
    query(EXISTS.args(_MAP_NEW.args("()")), true);
    query(_MAP_SIZE.args(_MAP_NEW.args("()")), 0);
    query(COUNT.args(_MAP_NEW.args("()")), 1);
    query(_MAP_SIZE.args(_MAP_NEW.args(_MAP_NEW.args("()"))), 0);
  }

  /** Test method. */
  @Test
  public void entry() {
    query(EXISTS.args(_MAP_ENTRY.args("A", "B")), true);
    query(EXISTS.args(_MAP_ENTRY.args(1, 2)), true);
    query(EXISTS.args(_MAP_NEW.args(_MAP_ENTRY.args(1, 2))), "true");
    error(EXISTS.args(_MAP_ENTRY.args("()", 2)), Err.INVEMPTY);
    error(EXISTS.args(_MAP_ENTRY.args("(1,2)", 2)), Err.SEQCAST);
  }

  /** Test method. */
  @Test
  public void get() {
    query(_MAP_GET.args(_MAP_NEW.args("()"), 1), "");
    query(_MAP_GET.args(_MAP_ENTRY.args(1, 2), 1), 2);
  }

  /** Test method. */
  @Test
  public void contains() {
    query(_MAP_CONTAINS.args(_MAP_NEW.args(), 1), false);
    query(_MAP_CONTAINS.args(_MAP_ENTRY.args(1, 2), 1), true);
  }

  /** Test method. */
  @Test
  public void remove() {
    query(_MAP_SIZE.args(_MAP_REMOVE.args(_MAP_ENTRY.args(1, 2), 1)), 0);
  }

  /** Test method. */
  @Test
  public void size() {
    query(_MAP_SIZE.args(_MAP_ENTRY.args(1, 2)), 1);
  }

  /** Test method. */
  @Test
  public void keys() {
    query("for $i in " + _MAP_KEYS.args(
        _MAP_NEW.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args("$i", "$i+1"))) + " order by $i return $i", "1 2 3");
    query("let $map := " + _MAP_NEW.args(" for $i in 1 to 3 return " +
        _MAP_ENTRY.args("$i", "$i + 1")) +
        "for $k in " + _MAP_KEYS.args("$map") + " order by $k return " +
        _MAP_GET.args("$map", "$k"), "2 3 4");
  }

  /** Test method. */
  @Test
  public void collation() {
    query(_MAP_COLLATION.args(_MAP_NEW.args()), Token.string(QueryText.URLCOLL));
  }
}
