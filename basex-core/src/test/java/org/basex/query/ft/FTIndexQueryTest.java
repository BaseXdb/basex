package org.basex.query.ft;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * Test if index and non-index full-text queries behave the same way.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class FTIndexQueryTest extends FTData {
  static { create(DOC); }
  static { queries = QUERIES; }

  /**
   * Initializes the test with the given input.
   * @param input input
   */
  private static void init(final String input) {
    execute(new CreateDB(NAME, input));
    set(MainOptions.FTINDEX, true);
    try {
      execute(new CreateDB(NAME + "ix", input));
    } finally {
      set(MainOptions.FTINDEX, false);
    }
  }

  /**
   * Runs all tests from {@link FTTest}.
   */
  @Test public void testFTTest() {
    init(DOC);
    for(final Object[] q : QUERIES) {
      if(q.length == 3) assertQuery((String) q[0], (String) q[2]);
    }
  }

  /**
   * Tests extended full-text features.
   */
  @Test public void testExt() {
    init("<x>A x B</x>");
    assertQuery("Ext 1", "//*[text() contains text 'A B' all words distance exactly 0 words]");
    assertQuery("Ext 2", _FT_MARK.args(" //*[text() contains text {'A B'} all words], 'b'"));
    assertQuery("Ext 3", _FT_MARK.args(" //*[text() contains text 'A' ftand 'B'], 'b'"));
  }

  /**
   * Tests mixed content.
   */
  @Test public void mixedContent() {
    init("<mix>A<sub/>B</mix>");
    assertQuery("Mix", "//mix[text()[1] contains text 'B']");

    init("<xml><mix>B<sub/>A</mix><mix>A<sub/>B</mix></xml>");
    assertQuery("Mix", "//mix[text()[1] contains text 'B']");
    assertQuery("Mix", "//mix[text() contains text 'A'][1]");
  }

  /**
   * Asserts that a query returns the same result with and without ft index.
   * @param name name of query
   * @param query query
   */
  private static void assertQuery(final String name, final String query) {
    execute(new Open(NAME));
    final String result1 = query(query);
    execute(new Open(NAME + "ix"));
    final String resultIx = query(query);
    assertEquals(result1, resultIx, "Query \"" + name + "\" failed:\nQuery: " + query + '\n');
  }
}
