package org.basex.test.query;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the XQuery full-text extensions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTExtTest extends FNTest {
  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";
  
  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB("db", FILE).execute(CTX);
  }

  /**
   * Test method for the 'mark' modifier in the FOR clause.
   * @throws BaseXException database exception
   */
  @Test
  public void testFor() throws BaseXException {
    // open database with(out) inner path
    query("for $i mark $m in //*[text() contains text '1'] return $m",
        "<li>Exercise <mark>1</mark></li>");
  }

  /**
   * Test method for the 'mark' modifier in the FOR clause.
   * @throws BaseXException database exception
   */
  @Test
  public void testFor2() throws BaseXException {
    // open database with(out) inner path
    query("for $i mark $m in //*[text() contains text '1' using marker 'b'] " +
        "return $m",
        "<li>Exercise <b>1</b></li>");
  }

  /**
   * Test method for the 'mark' modifier in the LET clause.
   * @throws BaseXException database exception
   */
  @Test
  public void testLet() throws BaseXException {
    // open database with(out) inner path
    query("let mark $m := //*[text() contains text '1'] return $m",
        "<li>Exercise <mark>1</mark></li>");
  }

  /**
   * Test method for the 'mark' modifier in the LET clause.
   * @throws BaseXException database exception
   */
  @Test
  public void testLet2() throws BaseXException {
    // open database with(out) inner path
    query("let mark $m := //*[text() contains text '1' using marker 'b'] " +
        "return $m",
        "<li>Exercise <b>1</b></li>");
  }

  /**
   * Test method for the 'db:fulltext-mark()' function.
   * @throws BaseXException database exception
   */
  @Test
  public void testFTMark() throws BaseXException {
    // open database with(out) inner path
    query("db:fulltext-mark(//*[text() contains text '1'])",
        "<li>Exercise <mark>1</mark></li>");
  }

  /**
   * Test method for the 'db:fulltext-mark()' function.
   * @throws BaseXException database exception
   */
  @Test
  public void testFTMark2() throws BaseXException {
    // open database with(out) inner path
    query("db:fulltext-mark(//*[text() contains text '2'], 'b')",
        "<li>Exercise <b>2</b></li>");
  }

  /**
   * Test method for the 'db:fulltext-mark()' function.
   * @throws BaseXException database exception
   */
  @Test
  public void testFTMark3() throws BaseXException {
    // open database with(out) inner path
    query("db:fulltext-mark(//*[text() contains text '1' using marker 'b'])",
        "<li>Exercise <b>1</b></li>");
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB("db").execute(CTX);
  }
}
