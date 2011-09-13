package org.basex.test.query.func;

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
import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
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
  /** Name of test database. */
  private static final String DB = Util.name(FNDbTest.class);
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
    new CreateDB(DB, FILE).execute(CONTEXT);
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Test method for the db:open() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbOpen() throws BaseXException {
    final String fun = check(Function.DBOPEN);
    query("count(" + fun + "('" + DB + "'))", "1");
    query("count(" + fun + "('" + DB + "', ''))", "1");
    query("count(" + fun + "('" + DB + "', 'unknown'))", "0");

    // close database instance
    new Close().execute(CONTEXT);
    query("count(" + fun + "(<a>" + DB + "</a>))", "1");
    query("count(" + fun + "('" + DB + "', 'unknown'))", "0");
    query(fun + "('" + DB + "')//title/text()", "XML");

    // run function on non-existing database
    new DropDB(DB).execute(CONTEXT);
    error(fun + "('" + DB + "')", Err.NODB);
  }

  /**
   * Test method for the db:open-pre() function.
   */
  @Test
  public void dbOpenPre() {
    final String fun = check(Function.DBOPENPRE);
    query(fun + "('" + DB + "', 0)//title/text()", "XML");
    error(fun + "('" + DB + "', -1)", Err.IDINVALID);
  }

  /**
   * Test method for the db:open-id() function.
   */
  @Test
  public void dbOpenId() {
    final String fun = check(Function.DBOPENID);
    query(fun + "('" + DB + "', 0)//title/text()", "XML");
    error(fun + "('" + DB + "', -1)", Err.IDINVALID);
  }

  /**
   * Test method for the db:text() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbText() throws BaseXException {
    final String fun = check(Function.DBTEXT);

    // run function without and with index
    new DropIndex("text").execute(CONTEXT);
    query(fun + "('" + DB + "', 'XML')", "XML");
    new CreateIndex("text").execute(CONTEXT);
    query(fun + "('" + DB + "', 'XML')", "XML");
    query(fun + "('" + DB + "', 'XXX')", "");
  }

  /**
   * Test method for the db:attribute() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbAttribute() throws BaseXException {
    final String fun = check(Function.DBATTR);

    // run function without and with index
    new DropIndex("attribute").execute(CONTEXT);
    query("data(" + fun + "('" + DB + "', '0'))", "0");
    new CreateIndex("attribute").execute(CONTEXT);
    query("data(" + fun + "('" + DB + "', '0'))", "0");
    query("data(" + fun + "('" + DB + "', '0', 'id'))", "0");
    query("data(" + fun + "('" + DB + "', '0', 'XXX'))", "");
    query(fun + "('" + DB + "', 'XXX')", "");
  }

  /**
   * Test method for the db:fulltext() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbFulltext() throws BaseXException {
    final String fun = check(Function.DBFULLTEXT);

    // run function without and with index
    new DropIndex("fulltext").execute(CONTEXT);
    error(fun + "('" + DB + "', 'assignments')", Err.NOIDX);
    new CreateIndex("fulltext").execute(CONTEXT);
    query(fun + "('" + DB + "', 'assignments')", "Assignments");
    query(fun + "('" + DB + "', 'XXX')", "");
  }

  /**
   * Test method for the db:list() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbList() throws BaseXException {
    final String fun = check(Function.DBLIST);

    // add documents
    new Add(FLDR, "docs", "test").execute(CONTEXT);
    contains(fun + "('" + DB + "')", "test/");

    // create two other database and compare substring
    new CreateDB(DB + 1).execute(CONTEXT);
    new CreateDB(DB + 2).execute(CONTEXT);
    contains(fun + "()", DB + 1 + ' ' + DB + 2);
    new DropDB(DB + 1).execute(CONTEXT);
    new DropDB(DB + 2).execute(CONTEXT);
  }

  /**
   * Test method for the db:system() function.
   */
  @Test
  public void dbSystem() {
    // wrong arguments
    final String fun = check(Function.DBSYSTEM);
    contains(fun + "()", INFOON);
  }

  /**
   * Test method for the db:info() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbInfo() throws BaseXException {
    // wrong arguments
    final String fun = check(Function.DBINFO);

    // standard test
    contains(fun + "('" + DB + "')", INFOON);

    // drop indexes and check index queries
    final String[] types = { "text", "attribute", "fulltext" };
    for(final String type : types) new DropIndex(type).execute(CONTEXT);
    for(final String type : types) {
      query(fun + "('" + DB + "', '" + type + "')");
    }
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CONTEXT);
    for(final String type : types) {
      query(fun + "('" + DB + "', '" + type + "')");
    }
    // check name indexes
    query(fun + "('" + DB + "', 'tag')");
    query(fun + "('" + DB + "', 'attname')");
  }

  /**
   * Test method for db:node-id() function.
   */
  @Test
  public void dbNodeID() {
    final String fun = check(Function.DBNODEID);
    query(fun + "(/html)", "1");
    query(fun + "(/ | /html)", "0 1");
  }

  /**
   * Test method for db:node-pre() function.
   */
  @Test
  public void dbNodePre() {
    final String fun = check(Function.DBNODEPRE);
    query(fun + "(/html)", "1");
    query(fun + "(/ | /html)", "0 1");
  }

  /**
   * Test method for db:event() function.
   */
  @Test
  public void dbEvent() {
    final String fun = check(Function.DBEVENT);
    error(fun + "('x', 'y')", Err.NOEVENT);
  }

  /**
   * Test method for the db:add() function.
   */
  @Test
  public void dbAdd() {
    final String fun = check(Function.DBADD);

    query(fun + "('" + DB + "', '<root/>', 'test1.xml')");
    query("count(collection('" + DB + "/test1.xml')/root)", "1");

    query(fun + "('" + DB + "', document { <root/> }, 'test2.xml')");
    query("count(collection('" + DB + "/test2.xml')/root)", "1");

    query(fun + "('" + DB + "', document { <root/> }, 'test3.xml', 'test')");
    query("count(collection('" + DB + "/test/test3.xml')/root)", "1");

    query(fun + "('" + DB + "', 'etc/test/input.xml', '', 'test')");
    query("count(collection('" + DB + "/test/input.xml')/html)", "1");

    query(fun + "('" + DB + "', 'etc/test/input.xml', 'test4.xml', 'test')");
    query("count(collection('" + DB + "/test/test4.xml')/html)", "1");

    query(fun + "('" + DB + "', '" + FLDR + "', '', 'test/dir')");
    query("count(collection('" + DB + "/test/dir'))", NFLDR);

    query("for $f in file:list('" + FLDR + "') " +
          "return " + fun + "('" + DB + "', $f, '', 'dir')");
    query("count(collection('" + DB + "/dir'))", NFLDR);

    query("for $i in 1 to 3 return " + fun + "('" + DB + "', '<root/>'," +
        "'doc' || $i)");
    query("count(for $i in 1 to 3 return collection('" + DB + "/doc' || $i))",
        "3");
  }

  /**
   * Test method for the db:delete() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbDelete() throws BaseXException {
    final String fun = check(Function.DBDELETE);

    new Add(FLDR, "docs", "test").execute(CONTEXT);

    query(fun + "('" + DB + "', 'test')", "");
    query("count(collection('" + DB + "/test'))", "0");
  }

  /**
   * Test method for the db:rename() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbRename() throws BaseXException {
    final String fun = check(Function.DBRENAME);

    new Add(FLDR, "docs", "test").execute(CONTEXT);
    query("count(collection('" + DB + "/test'))", NFLDR);

    // rename document
    query(fun + "('" + DB + "', 'test', 'newtest')");
    query("count(collection('" + DB + "/test'))", "0");
    query("count(collection('" + DB + "/newtest'))", NFLDR);

    // rename binary file
    query("db:store('" + DB + "', 'one', '')");
    query(fun + "('" + DB + "', 'one', 'two')");
    query("db:retrieve('" + DB + "', 'two')");
    error("db:retrieve('" + DB + "', 'one')", Err.RESFNF);
  }

  /**
   * Test method for the db:replace() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbReplace() throws BaseXException {
    final String fun = check(Function.DBREPLACE);

    new Add("etc/test/input.xml", null, "test").execute(CONTEXT);

    query(fun + "('" + DB + "', 'test/input.xml', '<root1/>')");
    query("count(collection('" + DB + "/test/input.xml')/html)", "0");
    query("count(collection('" + DB + "/test/input.xml')/root1)", "1");

    query(fun + "('" + DB + "', 'test/input.xml', document { <root2/> })");
    query("count(collection('" + DB + "/test/input.xml')/root1)", "0");
    query("count(collection('" + DB + "/test/input.xml')/root2)", "1");

    query(fun + "('" + DB + "', 'test/input.xml', 'etc/test/input.xml')");
    query("count(collection('" + DB + "/test/input.xml')/html)", "1");
    query("count(collection('" + DB + "/test/input.xml')/root2)", "0");
  }

  /**
   * Test method for the db:optimize() function.
   */
  @Test
  public void dbOptimize() {
    final String fun = check(Function.DBOPTIMIZE);
    query(fun + "('" + DB + "')");
    query(fun + "('" + DB + "', true())");
  }

  /**
   * Test method for the db:retrieve() function.
   */
  @Test
  public void dbRetrieve() {
    final String fun = check(Function.DBRETRIEVE);
    error(fun + "('" + DB + "', 'raw')", Err.RESFNF);
    query("db:store('" + DB + "', 'raw', xs:hexBinary('41'))");
    query(fun + "('" + DB + "', 'raw')");
    query("db:delete('" + DB + "', 'raw')");
    error(fun + "('" + DB + "', 'raw')", Err.RESFNF);
  }

  /**
   * Test method for the db:store() function.
   */
  @Test
  public void dbStore() {
    final String fun = check(Function.DBSTORE);
    query(fun + "('" + DB + "', 'raw1', xs:hexBinary('41'))");
    query(fun + "('" + DB + "', 'raw2', 'b')");
    query(fun + "('" + DB + "', 'raw3', 123)");
  }

  /**
   * Test method for the db:is-raw() function.
   */
  @Test
  public void dbIsRaw() {
    final String fun = check(Function.DBISRAW);
    query("db:add('" + DB + "', '<a/>', 'xml')");
    query("db:store('" + DB + "', 'raw', 'bla')");
    query(fun + "('" + DB + "', 'xml')", "false");
    query(fun + "('" + DB + "', 'raw')", "true");
    query(fun + "('" + DB + "', 'xxx')", "false");
    query(fun + "('" + DB + "', 'xxx')", "false");
  }

  /**
   * Test method for the db:is-xml() function.
   */
  @Test
  public void dbIsXML() {
    final String fun = check(Function.DBISXML);
    query("db:add('" + DB + "', '<a/>', 'xml')");
    query("db:store('" + DB + "', 'raw', 'bla')");
    query(fun + "('" + DB + "', 'xml')", "true");
    query(fun + "('" + DB + "', 'raw')", "false");
    query(fun + "('" + DB + "', 'xxx')", "false");
    query(fun + "('" + DB + "', 'xxx')", "false");
  }
}
