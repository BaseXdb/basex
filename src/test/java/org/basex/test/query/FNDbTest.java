package org.basex.test.query;

import static org.basex.core.Text.*;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.query.func.FunDef;
import org.basex.query.item.DBNode;
import org.basex.query.util.Err;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class FNDbTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";

  /** Constructor. */
  public FNDbTest() {
    super("db");
  }

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
    final String fun = check(FunDef.OPEN, String.class);
    query("count(" + fun + "('db'))", "1");
    query("count(" + fun + "('db/'))", "1");

    // close database instance
    new Close().execute(CTX);
    query("count(" + fun + "(<a>db</a>))", "1");
    query("count(" + fun + "('db/x'))", "0");
    query(fun + "('db')//title/text()", "XML");

    // run function on non-existing database
    new DropDB("db").execute(CTX);
    error(fun + "('db')", Err.NODB);
  }

  /**
   * Test method for the db:open-pre() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testOpenPre() throws BaseXException {
    final String fun = check(FunDef.OPENPRE, String.class, Integer.class);
    query(fun + "('db', 0)//title/text()", "XML");
    error(fun + "('db', -1)", Err.IDINVALID);
  }

  /**
   * Test method for the db:open-id() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testOpenId() throws BaseXException {
    final String fun = check(FunDef.OPENID, String.class, Integer.class);
    query(fun + "('db', 0)//title/text()", "XML");
    error(fun + "('db', -1)", Err.IDINVALID);
  }

  /**
   * Test method for the db:text() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testText() throws BaseXException {
    final String fun = check(FunDef.TEXT, (Class<?>) null);

    // run function without and with index
    new DropIndex("text").execute(CTX);
    query(fun + "('XML')", "XML");
    new CreateIndex("text").execute(CTX);
    query(fun + "('XML')", "XML");
    query(fun + "('XXX')", "");

    // run function on closed database
    new Close().execute(CTX);
    query("db:open('db')/" + fun + "('XML')", "XML");
    error(fun + "('XXX')", Err.NODBCTX);
  }

  /**
   * Test method for the db:attribute() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testAttribute() throws BaseXException {
    final String fun = check(FunDef.ATTR, (Class<?>) null, String.class);

    // run function without and with index
    new DropIndex("attribute").execute(CTX);
    query("data(" + fun + "('0'))", "0");
    new CreateIndex("attribute").execute(CTX);
    query("data(" + fun + "('0'))", "0");
    query("data(" + fun + "('0', 'id'))", "0");
    query("data(" + fun + "('0', 'XXX'))", "");
    query(fun + "('XXX')", "");

    // run function on closed database
    new Close().execute(CTX);
    query("data(db:open('db')/" + fun + "('0'))", "0");
    error("data(" + fun + "('XXX'))", Err.NODBCTX);
  }

  /**
   * Test method for the db:fulltext() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testIndex() throws BaseXException {
    final String fun = check(FunDef.FULLTEXT, String.class);

    // run function without and with index
    new DropIndex("fulltext").execute(CTX);
    error(fun + "('assignments')", Err.NOIDX);
    new CreateIndex("fulltext").execute(CTX);
    query(fun + "('assignments')", "Assignments");
    query(fun + "('XXX')", "");

    // run function on closed database
    new Close().execute(CTX);
    query("db:open('db')/" + fun + "('assignments')", "Assignments");
    error(fun + "('XXX')", Err.NODBCTX);
  }

  /**
   * Test method for the db:list() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testList() throws BaseXException {
    final String fun = check(FunDef.LIST);

    // create two other database and compare substring
    new CreateDB("daz").execute(CTX);
    new CreateDB("dba").execute(CTX);
    contains(fun + "()", "daz db dba");
    new DropDB("daz").execute(CTX);
    new DropDB("dba").execute(CTX);
  }

  /**
   * Test method for db:node-pre() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testNodePre() throws BaseXException {
    final String fun = check(FunDef.NODEPRE, DBNode.class);
    query(fun + "(/html)", "1");
    query(fun + "(/ | /html)", "0 1");
  }

  /**
   * Test method for db:node-id() and db:node-pre() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testNode() throws BaseXException {
    final String fun = check(FunDef.NODEID, DBNode.class);
    query(fun + "(/html)", "1");
    query(fun + "(/ | /html)", "0 1");
  }

  /**
   * Test method for the db:system() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testSystem() throws BaseXException {
    // wrong arguments
    final String fun = check(FunDef.SYSTEM);
    contains(fun + "()", INFOON);
  }

  /**
   * Test method for the db:info() function.
   * @throws BaseXException database exception
   */
  @Test
  public void testInfo() throws BaseXException {
    // wrong arguments
    final String fun = check(FunDef.INFO, String.class);

    // standard test
    contains(fun + "()", INFOON);

    // drop indexes and check index queries
    final String[] types = { "text", "attribute", "fulltext" };
    for(final String type : types) new DropIndex(type).execute(CTX);
    for(final String type : types) query(fun + "('" + type + "')");
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CTX);
    for(final String type : types) query(fun + "('" + type + "')");
    // check name indexes
    query(fun + "('tag')");
    query(fun + "('attname')");

    // run function on closed database
    new Close().execute(CTX);
    contains("db:open('db')/" + fun + "()", INFOON);
    contains("db:open('db')/" + fun + "('tag')", ":");
    error(fun + "('tag')", Err.NODBCTX);
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
