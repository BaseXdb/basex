package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.junit.jupiter.api.*;

/**
 * This class tests standard functions of XQuery 4.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class Fn4ModuleTest extends QueryPlanTest {
  /** Test method. */
  @Test public void identity() {
    final Function func = IDENTITY;
    query(func.args(" ()"), "");
    query(func.args(" <x/>"), "<x/>");
    query(func.args(" 1 to 10"), "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
    query("reverse(9 to 10000) => sort((), identity#1) => head()", 9);
  }
}
