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
    final Function func = Function._UTIL_ITEM_AT;
    query(func.args(" ()", 1), "");
    query(func.args(1, 1), 1);
    query(func.args(1, 0), "");
    query(func.args(1, 2), "");
    query(func.args(" 1 to 2", 2), 2);
    query(func.args(" 1 to 2", 3), "");
    query(func.args(" 1 to 2", 0), "");
    query(func.args(" 1 to 2", -1), "");
    query(func.args(" 1 to 2", 1.5), "");

    query("for $i in 1 to 2 return " + func.args(" $i", 1), "1\n2");
    query(func.args(" (<a/>,<b/>)", 0), "");
    query(func.args(" (<a/>,<b/>)", 1), "<a/>");
    query(func.args(" (<a/>,<b/>)", 2.5), "");
    query(func.args(" (<a/>,<b/>)", 3), "");

    query(func.args(" (<a/>,<b/>)[name()]", 1), "<a/>");
    query(func.args(" (<a/>,<b/>)[name()]", 2), "<b/>");
    query(func.args(" (<a/>,<b/>)[name()]", 3), "");

    query(func.args(" prof:void(())", 0), "");
    query(func.args(" (<a/>,<b/>)", 1.5), "");
    query(func.args(" <a/>", 2), "");
    query(func.args(" (<a/>,<b/>)", " <_>1</_>"), "<a/>");
  }

  /** Test method. */
  @Test
  public void itemRange() {
    final Function func = Function._UTIL_ITEM_RANGE;
    query(func.args(" ()", 1, 2), "");
    query(func.args(1, 1, 2), 1);
    query(func.args(1, 0, 2), 1);
    query(func.args(1, 2, 2), "");
    query(func.args(" 1 to 2", 2, 2), 2);
    query(func.args(" 1 to 2", 3, 2), "");
    query(func.args(" 1 to 2", 0, 2), "1\n2");
    query(func.args(" 1 to 2", -1, 2), "1\n2");
    query(func.args(" 1 to 2", 1.5, 2), 2);

    query("for $i in 1 to 2 return " + func.args(" $i", 1, 2), "1\n2");
    query(func.args(" (<a/>,<b/>)", 0, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>,<b/>)", 1, 2), "<a/>\n<b/>");
    query(func.args(" (<a/>,<b/>)", 2.5, 3.5), "");
    query(func.args(" (<a/>,<b/>)", 3, 4), "");

    query(func.args(" (<a/>,<b/>)[name()]", 1, 9223372036854775807L), "<a/>\n<b/>");
  }

  /** Test method. */
  @Test
  public void lastFrom() {
    final Function func = _UTIL_LAST_FROM;
    query(func.args(" ()"), "");
    query(func.args(1), 1);
    query(func.args(" 1 to 2"), 2);

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" (<a/>,<b/>)"), "<b/>");
    query(func.args(" (<a/>,<b/>)[position() > 2]"), "");

    query("for $i in 1 to 2 return " + func.args(" $i"), "1\n2");
    query(func.args(" <a/>"), "<a/>");
    query(func.args(" (<a/>,<b/>)"), "<b/>");
    query(func.args(" (<a/>,<b/>)[name()]"), "<b/>");
    query(func.args(" prof:void(())"), "");
  }

  /** Test method. */
  @Test
  public void replicate() {
    final Function func = _UTIL_REPLICATE;
    query(func.args(" ()", 0), "");
    query(func.args(" ()", 1), "");
    query(func.args(1, 0), "");
    query(func.args("A", 1), "A");
    query(func.args("A", 2), "A\nA");
    query(func.args(" (0,'A')", 1), "0\nA");
    query(func.args(" (0,'A')", 2), "0\nA\n0\nA");
    query(func.args(" 1 to 10000", 10000) + "[last()]", "10000");
    query(func.args(" 1 to 10000", 10000) + "[1]", "1");
    query(func.args(" 1 to 10000", 10000) + "[10000]", "10000");
    query(func.args(" 1 to 10000", 10000) + "[10001]", "1");
    query("count(" + func.args(" 1 to 1000000", 1000000) + ")", 1000000000000L);
    query("count(" + func.args(func.args(" 1 to 3", 3), 3) + ")", 27);

    query("for $i in 1 to 2 return " + func.args(1, " $i"), "1\n1\n1");
    query(func.args(" <a/>", 0), "");
    query(func.args(" <a/>", 1), "<a/>");
    query(func.args(" <a/>", 2), "<a/>\n<a/>");
    query(func.args(" ()", " <_>2</_>"), "");
    query(func.args(" <a/>", " <_>2</_>"), "<a/>\n<a/>");

    error(func.args(1, -1), QueryError.UTIL_NEGATIVE_X);
  }

  /** Test method. */
  @Test
  public void deepEquals() {
    final Function func = _UTIL_DEEP_EQUAL;
    query(func.args(1, 1), true);
    query(func.args(1, 1, "ALLNODES"), true);
    error(func.args("(1 to 2)", "(1 to 2)", "X"), QueryError.INVALIDOPTION_X);
  }
}
