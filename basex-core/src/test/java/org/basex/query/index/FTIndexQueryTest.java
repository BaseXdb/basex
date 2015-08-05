package org.basex.query.index;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Test if index and non-index full-text queries behave the same way.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class FTIndexQueryTest extends FTData {
  static { create(DOC); }
  static { queries = QUERIES; }

  /**
   * Initializes the test with the given input.
   * @param input input
   * @throws BaseXException database exception
   */
  private static void init(final String input) throws BaseXException {
    new CreateDB(NAME, input).execute(context);
    context.options.set(MainOptions.FTINDEX, true);
    try {
      new CreateDB(NAME + "ix", input).execute(context);
    } finally {
      context.options.set(MainOptions.FTINDEX, false);
    }
  }

  /**
   * Runs all tests from {@link FTTest}.
   * @throws BaseXException database exception
   */
  @Test
  public void testFTTest() throws BaseXException {
    init(DOC);
    for(final Object[] q : QUERIES) {
      if(q.length == 3) assertQuery((String) q[0], (String) q[2]);
    }
  }

  /**
   * Tests extended full-text features.
   * @throws BaseXException database exception
   */
  @Test
  public void testExt() throws BaseXException {
    init("<x>A x B</x>");
    assertQuery("Ext 1", "//*[text() contains text 'A B' all words distance exactly 0 words]");
    assertQuery("Ext 2", _FT_MARK.args(" //*[text() contains text {'A B'} all words], 'b'"));
    assertQuery("Ext 3", _FT_MARK.args(" //*[text() contains text 'A' ftand 'B'], 'b'"));
  }


  /**
   * Tests mixed content.
   * @throws BaseXException database exception
   */
  @Test
  public void mixedContent() throws BaseXException {
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
    try {
      new Open(NAME).execute(context);
      final String result1 = new XQuery(query).execute(context);
      new Open(NAME + "ix").execute(context);
      final String resultIx = new XQuery(query).execute(context);
      assertEquals("Query \"" + name + "\" failed:\nQuery: " + query + '\n', result1, resultIx);
    } catch(final BaseXException ex) {
      final AssertionError err = new AssertionError(
          "Query \"" + name + "\" failed:\nQuery:" + query + "\nMessage: " + Util.message(ex));
      err.initCause(ex);
      throw err;
    }
  }

  /* TABLE REPRESENTATION
  PRE DIS SIZ ATS  NS  KIND  CONTENT
    0   1  46   1   0  DOC   tmp
    1   1  45   1   0  ELEM  fttest
    2   1  11   1   0  ELEM  co
    3   1   2   1   0  ELEM  w
    4   1   1   1   0  TEXT  xml in the first sentence. second sentence.
      third sentence. fourth sentence. fifth sentence.
    5   3   2   1   0  ELEM  w
    6   1   1   1   0  TEXT  XML xml XmL
    7   5   2   1   0  ELEM  w
    8   1   1   1   0  TEXT  we have xml databases
    9   7   2   1   0  ELEM  w
   10   1   1   1   0  TEXT  XML DATABASES
   11   9   2   1   0  ELEM  w
   12   1   1   1   0  TEXT  XML & Databases
   13  12   3   1   0  ELEM  wc
   14   1   2   1   0  ELEM  w
   15   1   1   1   0  TEXT  hello
   16  15   5   1   0  ELEM  sc
   17   1   2   1   0  ELEM  s
   18   1   1   1   0  TEXT  di\u00e4t-joghurt
   19   3   2   1   0  ELEM  s
   20   1   1   1   0  TEXT  diat-joghurt
   21  20   4   1   0  ELEM  at
   22   1   2   1   0  ELEM  b
   23   1   1   1   0  TEXT  B
   24   3   1   1   0  TEXT  ad one
   25  24   2   1   0  ELEM  fti
   26   1   1   1   0  TEXT  adfas wordt. ook wel eens
   27  26   2   1   0  ELEM  fti
   28   1   1   1   0  TEXT  wordt ook wel een s
   29  28   2   1   0  ELEM  fti
   30   1   1   1   0  TEXT  adfad. wordt
  ook wel.eens a
   31  30   2   1   0  ELEM  fti
   32   1   1   1   0  TEXT  adfad wordt. ook
  wel een s adf
   33  32   2   1   0  ELEM  fti
   34   1   1   1   0  TEXT  adfad wordt ook. wel een s
   35  34   2   2   0  ELEM  atr
   36   1   1   1   0  ATTR  key="value"
   37  36   2   1   0  ELEM  w
   38   1   1   1   0  TEXT  the fifth sentence. fourth sentence.
     third sentence. second sentence. first sentence.
   39   1   1   1   0  ELEM  wld
   40   1   2   1   0  ELEM  wld
   41   1   1   1   0  TEXT  yeah
   42  41   4   1   0  ELEM  mix
   43   1   1   1   0  TEXT  A
   44   2   1   1   0  ELEM  sub
   45   3   1   1   0  TEXT  B
   */
}
