package org.basex.test.query.expr;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * Tests for XQuery Maps.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class MapTest extends AdvancedQueryTest {
  /** A map as key should lead to FOTY0013. */
  @Test public void mapAsKeyTest() {
    error("declare variable $m := map { 'a' := 'b' };" +
        "declare variable $q := map { $m := 'a' };" +
        "$q",
        Err.FNATM);
  }
}
