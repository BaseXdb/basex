package org.basex.query.expr;

import org.basex.query.ast.*;
import org.junit.jupiter.api.*;

/**
 * XQuery 4.0 tests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class XQuery4Test extends QueryPlanTest {
  /** Lookup operator. */
  @Test public void lookup() {
    query("map { } ? ''", "");
    query("map { '': 1 } ? ''", 1);

    query("let $m := map { '': 1 } return $m?''", 1);
    query("let $_ := '' return map { '': 1 } ? $_", 1);
    query("let $_ := '' let $m := map { '': 1 } return $m?$_", 1);

    query("declare variable $_ := 1; [ 9 ]?$_", 9);
  }
}
