package org.basex.test.query.advanced;

import static org.basex.core.Text.*;

import org.basex.core.BaseXException;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNDbTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "etc/test/input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/test/dir";
  /** Number of XML files for folder. */
  private static final int NFLDR;

  static {
    int fc = 0;
    for(final IOFile c : new IOFile(FLDR).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) ++fc;
    }
    NFLDR = fc;
  }

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB("db", FILE).execute(CONTEXT);
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB("db").execute(CONTEXT);
  }

  /**
   * Test method for the db:open() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testOpen() throws QueryException, BaseXException {
    final String fun = check(Function.DBOPEN);
    query("count(" + fun + "('db'))", "1");
    query("count(" + fun + "('db/'))", "1");

    // close database instance
    new Close().execute(CONTEXT);
    query("count(" + fun + "(<a>db</a>))", "1");
    query("count(" + fun + "('db/x'))", "0");
    query(fun + "('db')//title/text()", "XML");

    // run function on non-existing database
    new DropDB("db").execute(CONTEXT);
    error(fun + "('db')", Err.NODB);
  }

  /**
   * Test method for the db:open-pre() function.
   * @throws QueryException database exception
   */
  @Test
  public void testOpenPre() throws QueryException {
    final String fun = check(Function.DBOPENPRE);
    query(fun + "('db', 0)//title/text()", "XML");
    error(fun + "('db', -1)", Err.IDINVALID);
  }

  /**
   * Test method for the db:open-id() function.
   * @throws QueryException database exception
   */
  @Test
  public void testOpenId() throws QueryException {
    final String fun = check(Function.DBOPENID);
    query(fun + "('db', 0)//title/text()", "XML");
    error(fun + "('db', -1)", Err.IDINVALID);
  }

  /**
   * Test method for the db:text() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testText() throws QueryException, BaseXException {
    final String fun = check(Function.DBTEXT);

    // run function without and with index
    new DropIndex("text").execute(CONTEXT);
    query(fun + "('db', 'XML')", "XML");
    new CreateIndex("text").execute(CONTEXT);
    query(fun + "('db', 'XML')", "XML");
    query(fun + "('db', 'XXX')", "");
  }

  /**
   * Test method for the db:attribute() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testAttribute() throws QueryException, BaseXException {
    final String fun = check(Function.DBATTR);

    // run function without and with index
    new DropIndex("attribute").execute(CONTEXT);
    query("data(" + fun + "('db', '0'))", "0");
    new CreateIndex("attribute").execute(CONTEXT);
    query("data(" + fun + "('db', '0'))", "0");
    query("data(" + fun + "('db', '0', 'id'))", "0");
    query("data(" + fun + "('db', '0', 'XXX'))", "");
    query(fun + "('db', 'XXX')", "");
  }

  /**
   * Test method for the db:fulltext() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testIndex() throws QueryException, BaseXException {
    final String fun = check(Function.DBFULLTEXT);

    // run function without and with index
    new DropIndex("fulltext").execute(CONTEXT);
    error(fun + "('db', 'assignments')", Err.NOIDX);
    new CreateIndex("fulltext").execute(CONTEXT);
    query(fun + "('db', 'assignments')", "Assignments");
    query(fun + "('db', 'XXX')", "");
  }

  /**
   * Test method for the db:list() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testList() throws QueryException, BaseXException {
    final String fun = check(Function.DBLIST);

    // add documents
    new Add(FLDR, "docs", "test").execute(CONTEXT);
    contains(fun + "('db')", "test/");

    // create two other database and compare substring
    new CreateDB("daz").execute(CONTEXT);
    new CreateDB("dba").execute(CONTEXT);
    contains(fun + "()", "daz db dba");
    new DropDB("daz").execute(CONTEXT);
    new DropDB("dba").execute(CONTEXT);
  }

  /**
   * Test method for the db:add() function.
   * @throws QueryException query exception
   */
  @Test
  public void testAdd() throws QueryException {
    final String fun = check(Function.DBADD);

    query(fun + "('db', document { <root/> }, 'test1.xml')");
    query("count(collection('db/test1.xml')/root)", "1");

    query(fun + "('db', document { <root/> }, 'test2.xml', 'test')");
    query("count(collection('db/test/test2.xml')/root)", "1");

    query(fun + "('db', 'etc/test/input.xml', '', 'test')");
    query("count(collection('db/test/input.xml')/html)", "1");

    query(fun + "('db', 'etc/test/input.xml', 'test3.xml', 'test')");
    query("count(collection('db/test/test3.xml')/html)", "1");

    query(fun + "('db', '" + FLDR + "', '', 'test/dir')");
    query("count(collection('db/test/dir'))", NFLDR);
  }

  /**
   * Test method for the db:replace() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testReplace() throws QueryException, BaseXException {
    final String fun = check(Function.DBREPLACE);

    new Add("etc/test/input.xml", null, "test").execute(CONTEXT);

    query(fun + "('db', 'test/input.xml', document { <root/> })");
    query("count(collection('db/test/input.xml')/html) eq 0", "true");
    query("count(collection('db/test/input.xml')/root) eq 1", "true");

    query(fun + "('db', 'test/input.xml', 'etc/test/input.xml')");
    query("count(collection('db/test/input.xml')/html)", "1");
    query("count(collection('db/test/input.xml')/root)", "0");
  }

  /**
   * Test method for the db:delete() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testDelete() throws QueryException, BaseXException {
    final String fun = check(Function.DBDELETE);

    new Add(FLDR, "docs", "test").execute(CONTEXT);

    query(fun + "('db', 'test')", "");
    query("count(collection('db/test'))", "0");
  }

  /**
   * Test method for the db:rename() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testRename() throws QueryException, BaseXException {
    final String fun = check(Function.DBRENAME);

    new Add(FLDR, "docs", "test").execute(CONTEXT);
    query("count(collection('db/test'))", NFLDR);

    query(fun + "('db', 'test', 'newtest')", "");
    query("count(collection('db/test'))", "0");
    query("count(collection('db/newtest'))", NFLDR);
  }

  /**
   * Test method for the db:optimize() function.
   * @throws QueryException query exception
   */
  @Test
  public void testOptimize() throws QueryException {
    final String fun = check(Function.DBOPTIMIZE);

    query(fun + "('db')");
    query(fun + "('db', true())");
  }

  /**
   * Test method for db:node-pre() function.
   * @throws QueryException database exception
   */
  @Test
  public void testNodePre() throws QueryException {
    final String fun = check(Function.DBNODEPRE);
    query(fun + "(/html)", "1");
    query(fun + "(/ | /html)", "0 1");
  }

  /**
   * Test method for db:node-id() and db:node-pre() function.
   * @throws QueryException database exception
   */
  @Test
  public void testNode() throws QueryException {
    final String fun = check(Function.DBNODEID);
    query(fun + "(/html)", "1");
    query(fun + "(/ | /html)", "0 1");
  }

  /**
   * Test method for the db:system() function.
   * @throws QueryException database exception
   */
  @Test
  public void testSystem() throws QueryException {
    // wrong arguments
    final String fun = check(Function.DBSYSTEM);
    contains(fun + "()", INFOON);
  }

  /**
   * Test method for the db:info() function.
   * @throws QueryException query exception
   * @throws BaseXException database exception
   */
  @Test
  public void testInfo() throws QueryException, BaseXException {
    // wrong arguments
    final String fun = check(Function.DBINFO);

    // standard test
    contains(fun + "('db')", INFOON);

    // drop indexes and check index queries
    final String[] types = { "text", "attribute", "fulltext" };
    for(final String type : types) new DropIndex(type).execute(CONTEXT);
    for(final String type : types) query(fun + "('db', '" + type + "')");
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CONTEXT);
    for(final String type : types) query(fun + "('db', '" + type + "')");
    // check name indexes
    query(fun + "('db', 'tag')");
    query(fun + "('db', 'attname')");
  }
}
