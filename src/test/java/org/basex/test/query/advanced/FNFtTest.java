package org.basex.test.query.advanced;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the XQuery full-text extensions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNFtTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB("db", FILE).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
  }

  /**
   * Test method for the ft:search() function.
   * @throws BaseXException database exception
   * @throws QueryException  query exception
   */
  @Test
  public void testSearch() throws BaseXException, QueryException {
    // test arguments
    final String fun = check(FunDef.SEARCH);

    // check index results
    query(fun + "(., 'assignments')", "Assignments");
    query(fun + "(., 'XXX')", "");

    // apply index options to query term
    new Set("stemming", true).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
    contains(fun + "(., 'Exercises')/..", "<li>Exercise 1</li>");
    new Set("stemming", false).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
  }

  /**
   * Test method for the 'ft:mark()' function.
   * @throws QueryException query exception
   */
  @Test
  public void testMark() throws QueryException {
    final String fun = check(FunDef.MARK);

    query(fun + "(//*[text() contains text '1'])",
      "<li>Exercise <mark>1</mark></li>");
    query(fun + "(//*[text() contains text '2'], 'b')",
      "<li>Exercise <b>2</b></li>");
    contains(fun + "(//*[text() contains text 'Exercise'])",
      "<li><mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () " +
      "return ft:mark($a[. contains text 'a'], 'b')", "<b>a</b> b");
    query("copy $a := text { 'ab' } modify () " +
      "return ft:mark($a[. contains text 'ab'], 'b')", "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () " +
      "return ft:mark($a[. contains text 'a b'], 'b')", "<b>a</b> <b>b</b>");
  }

  /**
   * Test method for the ft:extract() function.
   * @throws QueryException query exception
   */
  @Test
  public void testExtract() throws QueryException {
    final String fun = check(FunDef.EXTRACT);
    query(fun + "(//*[text() contains text '1'])",
      "<li>Exercise <mark>1</mark></li>");
    query(fun + "(//*[text() contains text '2'], 'b', 20)",
      "<li>Exercise <b>2</b></li>");
    query(fun + "(//*[text() contains text '2'], '_o_', 1)",
      "<li>...<_o_>2</_o_></li>");
    contains(fun + "(//*[text() contains text 'Exercise'], 'b', 1)",
      "<li><b>Exercise</b>...</li>");
  }

  /**
   * Test method for the 'ft:score()' function.
   * @throws QueryException query exception
   */
  @Test
  public void testScore() throws QueryException {
    // test arguments
    final String fun = check(FunDef.SCORE);
    query(fun + "(ft:search(., '2'))", "1");
    query(fun + "(ft:search(., 'XML'))", "1 0.5");
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB("db").execute(CONTEXT);
  }
}
