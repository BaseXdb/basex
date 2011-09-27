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
    check(FTSEARCH);

    // check index results
    query(FTSEARCH.args(" . ", "assignments"), "Assignments");
    query(FTSEARCH.args(" . ", "XXX"), "");

    // apply index options to query term
    new Set(Prop.STEMMING, true).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
    contains(FTSEARCH.args(" . ", "Exercises") + "/..",
        "<li>Exercise 1</li>");
    new Set(Prop.STEMMING, false).execute(CONTEXT);
    new CreateIndex("fulltext").execute(CONTEXT);
  }

  /**
   * Test method for the 'ft:count()' function.
   */
  @Test
  public void ftCount() {
    check(FTCOUNT);
    query(FTCOUNT.args("()"), "0");
    query(FTCOUNT.args(" //*[text() contains text '1']"), "1");
    query(FTCOUNT.args(" //li[text() contains text 'exercise']"), "2");
    query("for $i in //li[text() contains text 'exercise'] return " +
        FTCOUNT.args("$i[text() contains text 'exercise']"), "1 1");
  }

  /**
   * Test method for the 'ft:mark()' function.
   */
  @Test
  public void ftMark() {
    check(FTMARK);
    query(FTMARK.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(FTMARK.args(" //*[text() contains text '2'], 'b'"),
      "<li>Exercise <b>2</b></li>");
    contains(FTMARK.args(" //*[text() contains text 'Exercise']"),
      "<li><mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () return " +
        FTMARK.args("$a[. contains text 'a']", "b"), "<b>a</b> b");
    query("copy $a := text { 'ab' } modify () return " +
        FTMARK.args("$a[. contains text 'ab'], 'b'"), "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () return " +
        FTMARK.args("$a[. contains text 'a b'], 'b'"), "<b>a</b> <b>b</b>");
  }

  /**
   * Test method for the 'ft:extract()' function.
   */
  @Test
  public void ftExtract() {
    check(FTEXTRACT);
    query(FTEXTRACT.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(FTEXTRACT.args(" //*[text() contains text '2'], 'b', 20"),
      "<li>Exercise <b>2</b></li>");
    query(FTEXTRACT.args(" //*[text() contains text '2'], '_o_', 1"),
      "<li>...<_o_>2</_o_></li>");
    contains(FTEXTRACT.args(" //*[text() contains text 'Exercise'], 'b', 1"),
      "<li><b>Exercise</b>...</li>");
  }

  /**
   * Test method for the 'ft:score()' function.
   */
  @Test
  public void ftScore() {
    check(FTSCORE);
    query(FTSCORE.args(FTSEARCH.args(" . ", "2")), "1");
    query(FTSCORE.args(FTSEARCH.args(" . ", "XML")), "1 0.5");
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
