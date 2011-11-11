package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
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
  /** Name of test database. */
  private static final String DB = Util.name(FNFtTest.class);
  /** Test file. */
  private static final String FILE = "etc/test/input.xml";

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB(DB, FILE).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
  }

  /**
   * Test method for the 'ft:search()' function.
   * @throws BaseXException database exception
   */
  @Test
  public void ftSearch() throws BaseXException {
    check(_FT_SEARCH);

    // check index results
    query(_FT_SEARCH.args(" . ", "assignments"), "Assignments");
    query(_FT_SEARCH.args(" . ", "XXX"), "");

    // apply index options to query term
    new Set(Prop.STEMMING, true).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
    contains(_FT_SEARCH.args(" . ", "Exercises") + "/..",
        "<li>Exercise 1</li>");
    new Set(Prop.STEMMING, false).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
  }

  /**
   * Test method for the 'ft:count()' function.
   */
  @Test
  public void ftCount() {
    check(_FT_COUNT);
    query(_FT_COUNT.args("()"), "0");
    query(_FT_COUNT.args(" //*[text() contains text '1']"), "1");
    query(_FT_COUNT.args(" //li[text() contains text 'exercise']"), "2");
    query("for $i in //li[text() contains text 'exercise'] return " +
        _FT_COUNT.args("$i[text() contains text 'exercise']"), "1 1");
  }

  /**
   * Test method for the 'ft:mark()' function.
   */
  @Test
  public void ftMark() {
    check(_FT_MARK);
    query(_FT_MARK.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(_FT_MARK.args(" //*[text() contains text '2'], 'b'"),
      "<li>Exercise <b>2</b></li>");
    contains(_FT_MARK.args(" //*[text() contains text 'Exercise']"),
      "<li><mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () return " +
        _FT_MARK.args("$a[. contains text 'a']", "b"), "<b>a</b> b");
    query("copy $a := text { 'ab' } modify () return " +
        _FT_MARK.args("$a[. contains text 'ab'], 'b'"), "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () return " +
        _FT_MARK.args("$a[. contains text 'a b'], 'b'"), "<b>a</b> <b>b</b>");
  }

  /**
   * Test method for the 'ft:extract()' function.
   */
  @Test
  public void ftExtract() {
    check(_FT_EXTRACT);
    query(_FT_EXTRACT.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(_FT_EXTRACT.args(" //*[text() contains text '2'], 'b', 20"),
      "<li>Exercise <b>2</b></li>");
    query(_FT_EXTRACT.args(" //*[text() contains text '2'], '_o_', 1"),
      "<li>...<_o_>2</_o_></li>");
    contains(_FT_EXTRACT.args(" //*[text() contains text 'Exercise'], 'b', 1"),
      "<li><b>Exercise</b>...</li>");
  }

  /**
   * Test method for the 'ft:score()' function.
   */
  @Test
  public void ftScore() {
    check(_FT_SCORE);
    query(_FT_SCORE.args(_FT_SEARCH.args(" . ", "2")), "1");
    query(_FT_SCORE.args(_FT_SEARCH.args(" . ", "XML")), "1 0.5");
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }
}
