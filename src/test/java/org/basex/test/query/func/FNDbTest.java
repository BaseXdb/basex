package org.basex.test.query.func;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;

import org.basex.core.BaseXException;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.MimeTypes;
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
  private static final String FILE = "src/test/resources/input.xml";
  /** Test folder. */
  private static final String FLDR = "src/test/resources/dir";
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
    check(_DB_OPEN);
    query(COUNT.args(_DB_OPEN.args(DB)), "1");
    query(COUNT.args(_DB_OPEN.args(DB, "")), "1");
    query(COUNT.args(_DB_OPEN.args(DB, "unknown")), "0");

    // close database instance
    new Close().execute(CONTEXT);
    query(COUNT.args(_DB_OPEN.args(DB, "unknown")), "0");
    query(_DB_OPEN.args(DB) + "//title/text()", "XML");

    // run function on non-existing database
    new DropDB(DB).execute(CONTEXT);
    error(_DB_OPEN.args(DB), Err.NODB);
  }

  /**
   * Test method for the db:open-pre() function.
   */
  @Test
  public void dbOpenPre() {
    check(_DB_OPEN_PRE);
    query(_DB_OPEN_PRE.args(DB, 0) + "//title/text()", "XML");
    error(_DB_OPEN_PRE.args(DB, -1), Err.IDINVALID);
  }

  /**
   * Test method for the db:open-id() function.
   */
  @Test
  public void dbOpenId() {
    check(_DB_OPEN_ID);
    query(_DB_OPEN_ID.args(DB, 0) + "//title/text()", "XML");
    error(_DB_OPEN_ID.args(DB, -1), Err.IDINVALID);
  }

  /**
   * Test method for the db:text() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbText() throws BaseXException {
    check(_DB_TEXT);
    // run function without and with index
    new DropIndex("text").execute(CONTEXT);
    query(_DB_TEXT.args(DB, "XML"), "XML");
    new CreateIndex("text").execute(CONTEXT);
    query(_DB_TEXT.args(DB, "XML"), "XML");
    query(_DB_TEXT.args(DB, "XXX"), "");
  }

  /**
   * Test method for the db:attribute() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbAttribute() throws BaseXException {
    check(_DB_ATTRIBUTE);
    // run function without and with index
    new DropIndex("attribute").execute(CONTEXT);
    query(DATA.args(_DB_ATTRIBUTE.args(DB, "0")), "0");
    new CreateIndex("attribute").execute(CONTEXT);
    query(DATA.args(_DB_ATTRIBUTE.args(DB, "0")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(DB, "0", "id")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(DB, "0", "XXX")), "");
    query(DATA.args(_DB_ATTRIBUTE.args(DB, "XXX")), "");
  }

  /**
   * Test method for the db:fulltext() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbFulltext() throws BaseXException {
    check(_DB_FULLTEXT);
    // run function without and with index
    new DropIndex("fulltext").execute(CONTEXT);
    error(_DB_FULLTEXT.args(DB, "assignments"), Err.NOIDX);
    new CreateIndex("fulltext").execute(CONTEXT);
    query(_DB_FULLTEXT.args(DB, "assignments"), "Assignments");
    query(_DB_FULLTEXT.args(DB, "XXX"), "");
  }

  /**
   * Test method for the db:list() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbList() throws BaseXException {
    check(_DB_LIST);
    // add documents
    new Add("test/docs", FLDR).execute(CONTEXT);
    contains(_DB_LIST.args(DB), "test/");
    // create two other database and compare substring
    new CreateDB(DB + 1).execute(CONTEXT);
    new CreateDB(DB + 2).execute(CONTEXT);
    contains(_DB_LIST.args(), DB + 1 + ' ' + DB + 2);
    new DropDB(DB + 1).execute(CONTEXT);
    new DropDB(DB + 2).execute(CONTEXT);
  }

  /**
   * Test method for the db:system() function.
   */
  @Test
  public void dbSystem() {
    check(_DB_SYSTEM);
    contains(_DB_SYSTEM.args(), INFOON);
  }

  /**
   * Test method for the db:info() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbInfo() throws BaseXException {
    check(_DB_INFO);
    // standard test
    contains(_DB_INFO.args(DB), INFOON);

    // drop indexes and check index queries
    final String[] types = { "text", "attribute", "fulltext" };
    for(final String type : types) new DropIndex(type).execute(CONTEXT);
    for(final String type : types) query(_DB_INFO.args(DB, type));
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CONTEXT);
    for(final String type : types) query(_DB_INFO.args(DB, type));
    // check name indexes
    query(_DB_INFO.args(DB, "tag"));
    query(_DB_INFO.args(DB, "attname"));
    error(_DB_INFO.args(DB, "XXX"), Err.NOIDX);
  }

  /**
   * Test method for db:node-id() function.
   */
  @Test
  public void dbNodeID() {
    check(_DB_NODE_ID);
    query(_DB_NODE_ID.args(" /html"), "1");
    query(_DB_NODE_ID.args(" / | /html"), "0 1");
  }

  /**
   * Test method for db:node-pre() function.
   */
  @Test
  public void dbNodePre() {
    check(_DB_NODE_PRE);
    query(_DB_NODE_PRE.args(" /html"), "1");
    query(_DB_NODE_PRE.args(" / | /html"), "0 1");
  }

  /**
   * Test method for db:event() function.
   */
  @Test
  public void dbEvent() {
    check(_DB_EVENT);
    error(_DB_EVENT.args("X", "Y"), Err.NOEVENT);
  }

  /**
   * Test method for the db:add() function.
   */
  @Test
  public void dbAdd() {
    check(_DB_ADD);
    query(_DB_ADD.args(DB, "\"<root/>\"", "t1.xml"));
    query(COUNT.args(COLLECTION.args(DB + "/t1.xml") + "/root"), "1");

    query(_DB_ADD.args(DB, " document { <root/> }", "t2.xml"));
    query(COUNT.args(COLLECTION.args(DB + "/t2.xml") + "/root"), "1");

    query(_DB_ADD.args(DB, " document { <root/> }", "test/t3.xml"));
    query(COUNT.args(COLLECTION.args(DB + "/test/t3.xml") + "/root"), "1");

    query(_DB_ADD.args(DB, FILE, "in/"));
    query(COUNT.args(COLLECTION.args(DB + "/in/input.xml") + "/html"), "1");

    query(_DB_ADD.args(DB, FILE, "test/t4.xml"));
    query(COUNT.args(COLLECTION.args(DB + "/test/t4.xml") + "/html"), "1");

    query(_DB_ADD.args(DB, FLDR, "test/dir"));
    query(COUNT.args(COLLECTION.args(DB + "/test/dir")), NFLDR);

    query("for $f in " + _FILE_LIST.args(FLDR) + " return " +
        _DB_ADD.args(DB, "$f", "dir"));
    query(COUNT.args(COLLECTION.args(DB + "/dir")), NFLDR);

    query("for $i in 1 to 3 return " +
        _DB_ADD.args(DB, "\"<root/>\"", "\"doc\" || $i"));
    query(COUNT.args(" for $i in 1 to 3 return " +
        COLLECTION.args("\"" + DB + "/doc\" || $i")), 3);
  }

  /**
   * Test method for the db:add() function with document with namespaces.
   */
  @Test
  public void dbAddWithNS() {
    query(_DB_ADD.args(DB, " document { <x xmlns:a='a' a:y='' /> }", "x"));
  }

  /**
   * Test method for the db:delete() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbDelete() throws BaseXException {
    check(_DB_DELETE);
    new Add("test/docs", FLDR).execute(CONTEXT);
    query(_DB_DELETE.args(DB, "test"));
    query(COUNT.args(COLLECTION.args(DB + "/test")), 0);
  }

  /**
   * Test method for the db:rename() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbRename() throws BaseXException {
    check(_DB_RENAME);

    new Add("test/docs", FLDR).execute(CONTEXT);
    query(COUNT.args(COLLECTION.args(DB + "/test")), NFLDR);

    // rename document
    query(_DB_RENAME.args(DB, "test", "newtest"));
    query(COUNT.args(COLLECTION.args(DB + "/test")), 0);
    query(COUNT.args(COLLECTION.args(DB + "/newtest")), NFLDR);

    // rename binary file
    query(_DB_STORE.args(DB, "one", ""));
    query(_DB_RENAME.args(DB, "one", "two"));
    query(_DB_RETRIEVE.args(DB, "two"));
    error(_DB_RETRIEVE.args(DB, "one"), Err.RESFNF);
  }

  /**
   * Test method for the db:replace() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbReplace() throws BaseXException {
    check(_DB_REPLACE);

    new Add("test", FILE).execute(CONTEXT);

    query(_DB_REPLACE.args(DB, FILE, "\"<R1/>\""));
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/R1"), 1);
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/R2"), 0);

    query(_DB_REPLACE.args(DB, FILE, " document { <R2/> }"));
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/R2"), 1);

    query(_DB_REPLACE.args(DB, FILE, FILE));
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/R2"), 0);
    query(COUNT.args(COLLECTION.args(DB + '/' + FILE) + "/html"), 1);
  }

  /**
   * Test method for the db:optimize() function.
   */
  @Test
  public void dbOptimize() {
    check(_DB_OPTIMIZE);
    query(_DB_OPTIMIZE.args(DB));
    query(_DB_OPTIMIZE.args(DB));
    query(_DB_OPTIMIZE.args(DB, "true()"));
  }

  /**
   * Test method for the db:retrieve() function.
   */
  @Test
  public void dbRetrieve() {
    check(_DB_RETRIEVE);
    error(_DB_RETRIEVE.args(DB, "raw"), Err.RESFNF);
    query(_DB_STORE.args(DB, "raw", "xs:hexBinary('41')"));
    query(_DB_RETRIEVE.args(DB, "raw"), "41");
    query(_DB_DELETE.args(DB, "raw"));
    error(_DB_RETRIEVE.args(DB, "raw"), Err.RESFNF);
  }

  /**
   * Test method for the db:store() function.
   */
  @Test
  public void dbStore() {
    check(_DB_STORE);
    query(_DB_STORE.args(DB, "raw1", "xs:hexBinary('41')"));
    query(_DB_STORE.args(DB, "raw2", "b"));
    query(_DB_RETRIEVE.args(DB, "raw2"), "62");
    query(_DB_STORE.args(DB, "raw3", 123));
    query(_DB_RETRIEVE.args(DB, "raw3"), "313233");
  }

  /**
   * Test method for the db:is-raw() function.
   */
  @Test
  public void dbIsRaw() {
    check(_DB_IS_RAW);
    query(_DB_ADD.args(DB, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(DB, "raw", "bla"));
    query(_DB_IS_RAW.args(DB, "xml"), "false");
    query(_DB_IS_RAW.args(DB, "raw"), "true");
    query(_DB_IS_RAW.args(DB, "xxx"), "false");
  }

  /**
   * Test method for the db:exists() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbExists() throws BaseXException {
    check(_DB_EXISTS);
    query(_DB_ADD.args(DB, "\"<a/>\"", "x/xml"));
    query(_DB_STORE.args(DB, "x/raw", "bla"));
    // checks if the specified resources exist (false expected for directories)
    query(_DB_EXISTS.args(DB), "true");
    query(_DB_EXISTS.args(DB, "x/xml"), "true");
    query(_DB_EXISTS.args(DB, "x/raw"), "true");
    query(_DB_EXISTS.args(DB, "xxx"), "false");
    query(_DB_EXISTS.args(DB, "x"), "false");
    query(_DB_EXISTS.args(DB, ""), "false");
    // false expected for missing database
    new DropDB(DB).execute(CONTEXT);
    query(_DB_EXISTS.args(DB), "false");
  }

  /**
   * Test method for the db:is-xml() function.
   */
  @Test
  public void dbIsXML() {
    check(_DB_IS_XML);
    query(_DB_ADD.args(DB, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(DB, "raw", "bla"));
    query(_DB_IS_XML.args(DB, "xml"), "true");
    query(_DB_IS_XML.args(DB, "raw"), "false");
    query(_DB_IS_XML.args(DB, "xxx"), "false");
  }

  /**
   * Test method for the db:content-type() function.
   */
  @Test
  public void dbContentType() {
    check(_DB_CONTENT_TYPE);
    query(_DB_ADD.args(DB, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(DB, "raw", "bla"));
    query(_DB_CONTENT_TYPE.args(DB, "xml"), MimeTypes.APP_XML);
    query(_DB_CONTENT_TYPE.args(DB, "raw"), MimeTypes.APP_OCTET);
    error(_DB_CONTENT_TYPE.args(DB, "test"), Err.RESFNF);
  }

  /**
   * Test method for the db:details() function.
   */
  @Test
  public void dbDetails() {
    check(_DB_DETAILS);
    query(_DB_ADD.args(DB, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(DB, "raw", "bla"));

    final String xmlCall = _DB_DETAILS.args(DB, "xml");
    query(xmlCall + "/@path/data()", "xml");
    query(xmlCall + "/@raw/data()", "false");
    query(xmlCall + "/@content-type/data()", MimeTypes.APP_XML);
    query(xmlCall + "/@modified-date/data()", CONTEXT.data().meta.time);
    query(xmlCall + "/@size/data()", "");

    final String rawCall = _DB_DETAILS.args(DB, "raw");
    query(rawCall + "/@path/data()", "raw");
    query(rawCall + "/@raw/data()", "true");
    query(rawCall + "/@content-type/data()", MimeTypes.APP_OCTET);
    query(rawCall + "/@modified-date/data() > 0", "true");
    query(rawCall + "/@size/data()", "3");

    error(_DB_DETAILS.args(DB, "test"), Err.RESFNF);
  }
}
