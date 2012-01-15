package org.basex.test.query.expr;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;
import org.junit.Test;

/**
 * Test cases for FLWOR expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FLWORTest {
  /** Database context. */
  private static Context context = new Context();

  /** Tests shadowing of outer variables. */
  @Test
  public void shadowTest() {
    query("for $a in for $a in <a>1</a> return $a/text() return <x>{ $a }</x>",
        "<x>1</x>");
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param query query
   * @param expected expected output
   */
  private void query(final String query, final String expected) {
    try {
      final String result = new XQuery(query).execute(context);
      // quotes are replaced by apostrophes to simplify comparison
      assertEquals(expected.replaceAll("\\\"", "'"),
          result.replaceAll("\\\"", "'"));
    } catch(final BaseXException ex) {
      fail(Util.message(ex));
    }
  }
}
