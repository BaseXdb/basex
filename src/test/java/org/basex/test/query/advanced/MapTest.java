package org.basex.test.query.advanced;

import org.basex.query.util.Err;
import org.junit.Test;

/**
 * Tests for XQuery Maps.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class MapTest extends AdvancedQueryTest {
  /** A map as key should lead to FOTY0013. */
  @Test public void mapAsKeyTest() {
    error("declare variable $m := map { 'a' := 'b' };" +
        "declare variable $q := map { $m := 'a' };" +
        "$q",
        Err.FNATM);
  }
}
