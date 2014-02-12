package org.basex.query;

import static org.junit.Assert.*;

import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for the {@link QueryContext}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public class QueryContextTest extends SandboxTest {
  /**
   * Tests the {@link QueryContext#parseLibrary(String, String, StaticContext)}
   * method.
   */
  @Test
  public void module() {
    final QueryContext qc = new QueryContext(context);
    try {
      qc.parseLibrary(
          "module namespace m='foo'; declare function m:foo() { m:bar() }; ", "", null);
      fail("Unknown function 'm:bar()' was not detected.");
    } catch(final QueryException e) {
      assertSame(Err.FUNCUNKNOWN, e.err());
    } finally {
      qc.close();
    }
  }

  /**
   * Tests the {@link QueryContext#parseLibrary(String, String, StaticContext)} method.
   * @throws Exception exception
   */
  @Test
  public void module2() throws Exception {
    final QueryContext qc = new QueryContext(context);
    final IOFile a = new IOFile("src/test/resources/recmod/a.xqm");
    try {
      qc.parseLibrary(Token.string(a.read()), a.path(), null);
    } finally {
      qc.close();
    }
  }
}
