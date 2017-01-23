package org.basex.query.expr;

import static org.junit.Assert.*;

import org.basex.query.*;
import org.junit.*;

/**
 * Test cases for FLWOR expressions.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class GFLWORTest extends AdvancedQueryTest {
  /** Tests shadowing of outer variables. */
  @Test
  public void shadowTest() {
    assertEquals("<x>1</x>",
        query("for $a in for $a in <a>1</a> return $a/text() return <x>{ $a }</x>"));
  }

  /** Tests shadowing between grouping variables. */
  @Test
  public void groupShadowTest() {
    assertEquals("1", query("let $i := 1 group by $i, $i return $i"));
  }

  /** Positional optimization. */
  @Test
  public void posOptimizationTest() {
    assertEquals("<a/>", query("for $a at $p in (<a/>,<b/>)/. where $p < 2 return $a"));
  }
}
