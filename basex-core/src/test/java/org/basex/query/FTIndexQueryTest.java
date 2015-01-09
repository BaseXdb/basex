package org.basex.query;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.simple.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Test if index and non-index full-text queries behave the same way.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class FTIndexQueryTest extends SandboxTest {
  /**
   * Initializes the test with the given input.
   * @param input input
   * @throws BaseXException database exception
   */
  private static void init(final String input) throws BaseXException {
    new CreateDB(NAME, input).execute(SandboxTest.context);
    new Set(MainOptions.FTINDEX, true).execute(SandboxTest.context);
    new CreateDB(NAME + "ix", input).execute(SandboxTest.context);
  }

  /**
   * Run all tests from {@link FTTest}.
   * @throws BaseXException database exception
   */
  @Test
  public void testFTTest() throws BaseXException {
    init(FTTest.DOC);
    for(final Object[] q : FTTest.QUERIES) {
      if(q.length == 3) assertQuery((String) q[2]);
    }
  }

  /**
   * Word distance test.
   * @throws BaseXException database exception
   */
  @Test
  public void testWordsDistance() throws BaseXException {
    init("<x>A x B</x>");
    assertQuery("//*[text() contains text 'A B' all words distance exactly 0 words]");
    assertQuery(_FT_MARK.args(" //*[text() contains text {'A B'} all words], 'b'"));
    assertQuery(_FT_MARK.args(" //*[text() contains text 'A' ftand 'B'], 'b'"));
  }

  /**
   * Assert that a query returns the same result with and without ft index.
   * @param q query
   */
  private static void assertQuery(final String q) {
    try {
      new Open(NAME).execute(context);
      final String result1 = new XQuery(q).execute(context);
      new Open(NAME + "ix").execute(context);
      final String result2 = new XQuery(q).execute(context);
      assertEquals("Query failed:\n" + q + '\n', result1, result2);
    } catch(final BaseXException ex) {
      final AssertionError err = new AssertionError(
          "Query failed:\n" + q + "\nMessage: " + Util.message(ex));
      err.initCause(ex);
      throw err;
    }
  }
}
