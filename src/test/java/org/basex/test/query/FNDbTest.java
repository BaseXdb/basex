package org.basex.test.query;

import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FNDbTest extends AdvancedQueryTest {
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
   * Test method for the db:open() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testOpen() throws BaseXException {
    // close database instance
    new Close().execute(CTX);

    // test wrong arguments
    args("db:open", String.class);

    // open database with(out) inner path
    query("count(db:open('db'))", "1");
    query("count(db:open('db/'))", "1");
    query("count(db:open(<a>db</a>))", "1");
    query("count(db:open('db/x'))", "0");
    query("db:open('db')//title/text()", "XML");
    // open database with pre and id values
    query("db:open-pre('db', 0)//title/text()", "XML");
    query("db:open-id('db', 0)//title/text()", "XML");
    // try invalid pre and id values
    error("db:open-pre('db', -1)", "BASX0004");
    error("db:open-id('db', -1)", "BASX0004");
    // run function on non-existing database
    new DropDB("db").execute(CTX);
    error("db:open('db')", "BASX0003");
  }

  /**
   * Test method for the db:...index() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testIndex() throws BaseXException {
    final String[] types = { "text", "attribute", "fulltext" };
    // drop indexes and check index queries
    for(final String type : types) new DropIndex(type).execute(CTX);
    for(final String type : types) {
      error("db:" + type +  "-index('xml')", "BASX0001");
    }
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CTX);
    for(final String type : types) query("db:" + type +  "-index('xml')");

    // test wrong arguments
    args("db:text-index", (Class<?>) null);
    args("db:fulltext-index", String.class);

    // check index results
    query("db:text-index('XML')", "XML");
    query("db:text-index('XML')", "XML");
    query("db:text-index('XXX')", "");
    query("data(" + "db:attribute-index('0'))", "0");
    query("data(" + "db:attribute-index('0', 'id'))", "0");
    query("data(" + "db:attribute-index('0', 'X'))", "");
    query("db:fulltext-index('assignments')", "Assignments");
    query("db:fulltext-index('XXX')", "");

    // run function on closed database
    new Close().execute(CTX);
    query("db:open('db')/db:text-index('XML')", "XML");
    error("db:text-index('x')", "BASX0002");
  }

  /**
   * Test method for the db:list() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testList() throws BaseXException {
    // wrong arguments
    args("db:list");

    // create two other database and compare substring
    new CreateDB("daz").execute(CTX);
    new CreateDB("dba").execute(CTX);
    contains("db:list()", "daz db dba");
    new DropDB("daz").execute(CTX);
    new DropDB("dba").execute(CTX);
  }

  /**
   * Test method for the db:fulltext-mark() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testFulltextMark() throws BaseXException {
    contains("db:fulltext-mark(//*[text() contains text '1'])",
      "<li>Exercise <mark>1</mark></li>");
    contains("db:fulltext-mark(//*[text() contains text '1'], 'b')",
      "<li>Exercise <b>1</b></li>");
    contains("db:fulltext-mark(//*[text() contains text 'Exercise'])",
      "<li><mark>Exercise</mark> 1</li>");
  }

  /**
   * Test method for db:node-id() and db:node-pre() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testNode() throws BaseXException {
    // wrong arguments
    args("db:node-id", (Class<?>) null);
    args("db:node-pre", (Class<?>) null);
    error("db:node-id(1)", "BASX0002");
    error("db:node-pre(1)", "BASX0002");

    // test results
    query("db:node-id(/html)", "1");
    query("db:node-pre(/html)", "1");
    query("db:node-pre(/ | /html)", "0 1");
  }

  /**
   * Test method for the db:system() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testSystem() throws BaseXException {
    // wrong arguments
    args("db:system");

    // standard test
    contains("db:system()", "ON");
  }

  /**
   * Test method for the db:info() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testInfo() throws BaseXException {
    // wrong arguments
    args("db:info");
    // standard test
    contains("db:info()", "ON");
    // run function on closed database
    new Close().execute(CTX);
    contains("db:open('db')/db:info()", "ON");
    error("db:info()", "BASX0002");
  }

  /**
   * Test method for the db:index-info() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testIndexInfo() throws BaseXException {
    // wrong arguments
    args("db:index-info", String.class);

    // drop indexes and check index queries
    final String[] types = { "text", "attribute", "fulltext" };
    for(final String type : types) new DropIndex(type).execute(CTX);
    for(final String type : types) {
      error("db:index-info('" + type + "')", "BASX0001");
    }
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CTX);
    for(final String type : types) query("db:index-info('" + type + "')");
    // check name indexes
    query("db:index-info('tag')");
    query("db:index-info('attname')");

    // run function on closed database
    new Close().execute(CTX);
    contains("db:open('db')/db:index-info('tag')", ":");
    error("db:index-info('tag')", "BASX0002");
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
