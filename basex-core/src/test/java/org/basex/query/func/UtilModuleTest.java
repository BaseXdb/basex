package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Utility Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class UtilModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void itemAt() {
    query(_UTIL_ITEM_AT.args(" ()", 1), "");
    query(_UTIL_ITEM_AT.args(1, 1), 1);
    query(_UTIL_ITEM_AT.args(1, 0), "");
    query(_UTIL_ITEM_AT.args(1, 2), "");
    query(_UTIL_ITEM_AT.args(" 1 to 2", 2), 2);
    query(_UTIL_ITEM_AT.args(" 1 to 2", 3), "");
    query(_UTIL_ITEM_AT.args(" 1 to 2", 0), "");
    query(_UTIL_ITEM_AT.args(" 1 to 2", -1), "");
    query(_UTIL_ITEM_AT.args(" 1 to 2", 1.5), "");

    query("for $i in 1 to 2 return " + _UTIL_ITEM_AT.args(" $i", 1), "1\n2");
    query(_UTIL_ITEM_AT.args(" (<a/>,<b/>)", 0), "");
    query(_UTIL_ITEM_AT.args(" (<a/>,<b/>)", 1), "<a/>");
    query(_UTIL_ITEM_AT.args(" (<a/>,<b/>)", 2.5), "");
    query(_UTIL_ITEM_AT.args(" (<a/>,<b/>)", 3), "");
  }

  /** Test method. */
  @Test
  public void itemRange() {
    query(_UTIL_ITEM_RANGE.args(" ()", 1, 2), "");
    query(_UTIL_ITEM_RANGE.args(1, 1, 2), 1);
    query(_UTIL_ITEM_RANGE.args(1, 0, 2), 1);
    query(_UTIL_ITEM_RANGE.args(1, 2, 2), "");
    query(_UTIL_ITEM_RANGE.args(" 1 to 2", 2, 2), 2);
    query(_UTIL_ITEM_RANGE.args(" 1 to 2", 3, 2), "");
    query(_UTIL_ITEM_RANGE.args(" 1 to 2", 0, 2), "1\n2");
    query(_UTIL_ITEM_RANGE.args(" 1 to 2", -1, 2), "1\n2");
    query(_UTIL_ITEM_RANGE.args(" 1 to 2", 1.5, 2), 2);

    query("for $i in 1 to 2 return " + _UTIL_ITEM_RANGE.args(" $i", 1, 2), "1\n2");
    query(_UTIL_ITEM_RANGE.args(" (<a/>,<b/>)", 0, 2), "<a/>\n<b/>");
    query(_UTIL_ITEM_RANGE.args(" (<a/>,<b/>)", 1, 2), "<a/>\n<b/>");
    query(_UTIL_ITEM_RANGE.args(" (<a/>,<b/>)", 2.5, 3.5), "");
    query(_UTIL_ITEM_RANGE.args(" (<a/>,<b/>)", 3, 4), "");
  }

  /** Test method. */
  @Test
  public void lastFrom() {
    query(_UTIL_LAST_FROM.args(" ()"), "");
    query(_UTIL_LAST_FROM.args(1), 1);
    query(_UTIL_LAST_FROM.args(" 1 to 2"), 2);

    query("for $i in 1 to 2 return " + _UTIL_LAST_FROM.args(" $i"), "1\n2");
    query(_UTIL_LAST_FROM.args(" (<a/>,<b/>)"), "<b/>");
    query(_UTIL_LAST_FROM.args(" (<a/>,<b/>)[position() > 2]"), "");
  }

  /** Test method. */
  @Test
  public void deepEquals() {
    query(_UTIL_DEEP_EQUAL.args(1, 1), true);
    query(_UTIL_DEEP_EQUAL.args(1, 1, "ALLNODES"), true);
    error(_UTIL_DEEP_EQUAL.args("(1 to 2)", "(1 to 2)", "X"), QueryError.INVALIDOPTION_X);
  }
}
