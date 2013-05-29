package org.basex.test.query;

import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.test.*;
import org.junit.*;

/**
 * Tests for the {@link QueryContext}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class QueryContextTest extends SandboxTest {
  /** Tests the {@link QueryContext#parseLibrary(String, String)} method. */
  @Test
  public void module() {
    final QueryContext qc = new QueryContext(context);
    try {
      qc.parseLibrary(
          "module namespace m='foo'; declare function m:foo() { m:bar() }; ", "");
      fail("Unknown function 'm:bar()' was not detected.");
    } catch(final QueryException e) {
      assertSame(Err.FUNCUNKNOWN, e.err());
    }
  }
}
