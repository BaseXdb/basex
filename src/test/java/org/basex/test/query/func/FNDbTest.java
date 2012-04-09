package org.basex.test.query.func;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNDbTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";
  /** Test folder. */
  private static final String FLDR = "src/test/resources/dir/";
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
    new CreateDB(NAME, FILE).execute(context);
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Test method for the db:open() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbOpen() throws BaseXException {
    check(_DB_OPEN);
    query(COUNT.args(_DB_OPEN.args(NAME)), "1");
    query(COUNT.args(_DB_OPEN.args(NAME, "")), "1");
    query(COUNT.args(_DB_OPEN.args(NAME, "unknown")), "0");

    // close database instance
    new Close().execute(context);
    query(COUNT.args(_DB_OPEN.args(NAME, "unknown")), "0");
    query(_DB_OPEN.args(NAME) + "//title/text()", "XML");

    // run function on non-existing database
    new DropDB(NAME).execute(context);
    error(_DB_OPEN.args(NAME), Err.NODB);
  }

  /**
   * Test method for the db:open-pre() function.
   */
  @Test
  public void dbOpenPre() {
    check(_DB_OPEN_PRE);
    query(_DB_OPEN_PRE.args(NAME, 0) + "//title/text()", "XML");
    error(_DB_OPEN_PRE.args(NAME, -1), Err.IDINVALID);
  }

  /**
   * Test method for the db:open-id() function.
   */
  @Test
  public void dbOpenId() {
    check(_DB_OPEN_ID);
    query(_DB_OPEN_ID.args(NAME, 0) + "//title/text()", "XML");
    error(_DB_OPEN_ID.args(NAME, -1), Err.IDINVALID);
  }

  /**
   * Test method for the db:text() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbText() throws BaseXException {
    check(_DB_TEXT);
    // run function without and with index
    new DropIndex("text").execute(context);
    query(_DB_TEXT.args(NAME, "XML"), "XML");
    new CreateIndex("text").execute(context);
    query(_DB_TEXT.args(NAME, "XML"), "XML");
    query(_DB_TEXT.args(NAME, "XXX"), "");
  }

  /**
   * Test method for the db:attribute() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbAttribute() throws BaseXException {
    check(_DB_ATTRIBUTE);
    // run function without and with index
    new DropIndex("attribute").execute(context);
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0")), "0");
    new CreateIndex("attribute").execute(context);
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0", "id")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0", "XXX")), "");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "XXX")), "");
  }

  /**
   * Test method for the db:fulltext() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbFulltext() throws BaseXException {
    check(_DB_FULLTEXT);
    // run function without and with index
    new DropIndex("fulltext").execute(context);
    error(_DB_FULLTEXT.args(NAME, "assignments"), Err.NOINDEX);
    new CreateIndex("fulltext").execute(context);
    query(_DB_FULLTEXT.args(NAME, "assignments"), "Assignments");
    query(_DB_FULLTEXT.args(NAME, "XXX"), "");
  }

  /**
   * Test method for the db:list() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbList() throws BaseXException {
    check(_DB_LIST);
    // add documents
    new Add("test/docs", FLDR).execute(context);
    contains(_DB_LIST.args(NAME), "test/");
    // create two other database and compare substring
    new CreateDB(NAME + 1).execute(context);
    new CreateDB(NAME + 2).execute(context);
    contains(_DB_LIST.args(), NAME + 1 + ' ' + NAME + 2);
    new DropDB(NAME + 1).execute(context);
    new DropDB(NAME + 2).execute(context);
  }

  /**
   * Test method for the db:list-details() function.
   */
  @Test
  public void dbListDetails() {
    check(_DB_LIST_DETAILS);
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));

    final String xmlCall = _DB_LIST_DETAILS.args(NAME, "xml");
    query(xmlCall + "/@raw/data()", "false");
    query(xmlCall + "/@content-type/data()", MimeTypes.APP_XML);
    query(xmlCall + "/@modified-date/xs:dateTime(.)");
    query(xmlCall + "/@size/data()", "");
    query(xmlCall + "/text()", "xml");

    final String rawCall = _DB_LIST_DETAILS.args(NAME, "raw");
    query(rawCall + "/@raw/data()", "true");
    query(rawCall + "/@content-type/data()", MimeTypes.APP_OCTET);
    query(rawCall + "/@modified-date/xs:dateTime(.) > " +
        "xs:dateTime('1971-01-01T00:00:01')", "true");
    query(rawCall + "/@size/data()", "3");
    query(rawCall + "/text()", "raw");

    query(_DB_LIST_DETAILS.args(NAME, "test"), "");
    error(_DB_LIST_DETAILS.args("mostProbablyNotAvailable"), Err.NODB);
  }

  /**
   * Test method for the db:system() function.
   */
  @Test
  public void dbSystem() {
    check(_DB_SYSTEM);
    contains(_DB_SYSTEM.args(), Prop.VERSION);
  }

  /**
   * Test method for the db:info() function.
   */
  @Test
  public void dbInfo() {
    check(_DB_INFO);
    query("count(" + _DB_INFO.args(NAME) + "//" +
        SIZE.replaceAll(" |-", "").toLowerCase(Locale.ENGLISH) + ')', 1);
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
    query(_DB_ADD.args(NAME, "\"<root/>\"", "t1.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/t1.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, " document { <root/> }", "t2.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/t2.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, " document { <root/> }", "test/t3.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/t3.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, FILE, "in/"));
    query(COUNT.args(COLLECTION.args(NAME + "/in/input.xml") + "/html"), "1");

    query(_DB_ADD.args(NAME, FILE, "test/t4.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/t4.xml") + "/html"), "1");

    query(_DB_ADD.args(NAME, FLDR, "test/dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/dir")), NFLDR);

    query("for $f in " + _FILE_LIST.args(FLDR, "true()", "*.xml") +
        " return " + _DB_ADD.args(NAME, " '" + FLDR + "' || $f", "dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/dir")), NFLDR);

    query("for $i in 1 to 3 return " +
        _DB_ADD.args(NAME, "\"<root/>\"", "\"doc\" || $i"));
    query(COUNT.args(" for $i in 1 to 3 return " +
        COLLECTION.args('"' + NAME + "/doc\" || $i")), 3);
  }

  /**
   * Test method for the db:add() function with document with namespaces.
   */
  @Test
  public void dbAddWithNS() {
    query(_DB_ADD.args(NAME, " document { <x xmlns:a='a' a:y='' /> }", "x"));
  }

  /**
   * Test method for the db:delete() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbDelete() throws BaseXException {
    check(_DB_DELETE);
    new Add("test/docs", FLDR).execute(context);
    query(_DB_DELETE.args(NAME, "test"));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), 0);
  }

  /**
   * Test method for the db:rename() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbRename() throws BaseXException {
    check(_DB_RENAME);

    new Add("test/docs", FLDR).execute(context);
    query(COUNT.args(COLLECTION.args(NAME + "/test")), NFLDR);

    // rename document
    query(_DB_RENAME.args(NAME, "test", "newtest"));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), 0);
    query(COUNT.args(COLLECTION.args(NAME + "/newtest")), NFLDR);

    // rename binary file
    query(_DB_STORE.args(NAME, "one", ""));
    query(_DB_RENAME.args(NAME, "one", "two"));
    query(_DB_RETRIEVE.args(NAME, "two"));
    error(_DB_RETRIEVE.args(NAME, "one"), Err.RESFNF);
  }

  /**
   * Test method for the db:replace() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbReplace() throws BaseXException {
    check(_DB_REPLACE);

    new Add("test", FILE).execute(context);

    query(_DB_REPLACE.args(NAME, FILE, "\"<R1/>\""));
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R1"), 1);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R2"), 0);

    query(_DB_REPLACE.args(NAME, FILE, " document { <R2/> }"));
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R2"), 1);

    query(_DB_REPLACE.args(NAME, FILE, FILE));
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/R2"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + FILE) + "/html"), 1);
  }

  /**
   * Test method for the db:optimize() function.
   */
  @Test
  public void dbOptimize() {
    check(_DB_OPTIMIZE);
    query(_DB_OPTIMIZE.args(NAME));
    query(_DB_OPTIMIZE.args(NAME));
    query(_DB_OPTIMIZE.args(NAME, "true()"));
  }

  /**
   * Test method for the db:retrieve() function.
   */
  @Test
  public void dbRetrieve() {
    check(_DB_RETRIEVE);
    error(_DB_RETRIEVE.args(NAME, "raw"), Err.RESFNF);
    query(_DB_STORE.args(NAME, "raw", "xs:hexBinary('41')"));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw") + ')', "41");
    query(_DB_DELETE.args(NAME, "raw"));
    error(_DB_RETRIEVE.args(NAME, "raw"), Err.RESFNF);
  }

  /**
   * Test method for the db:store() function.
   */
  @Test
  public void dbStore() {
    check(_DB_STORE);
    query(_DB_STORE.args(NAME, "raw1", "xs:hexBinary('41')"));
    query(_DB_STORE.args(NAME, "raw2", "b"));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw2") + ')', "62");
    query(_DB_STORE.args(NAME, "raw3", 123));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw3") + ')', "313233");
  }

  /**
   * Test method for the db:is-raw() function.
   */
  @Test
  public void dbIsRaw() {
    check(_DB_IS_RAW);
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_IS_RAW.args(NAME, "xml"), "false");
    query(_DB_IS_RAW.args(NAME, "raw"), "true");
    query(_DB_IS_RAW.args(NAME, "xxx"), "false");
  }

  /**
   * Test method for the db:exists() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbExists() throws BaseXException {
    check(_DB_EXISTS);
    query(_DB_ADD.args(NAME, "\"<a/>\"", "x/xml"));
    query(_DB_STORE.args(NAME, "x/raw", "bla"));
    // checks if the specified resources exist (false expected for directories)
    query(_DB_EXISTS.args(NAME), "true");
    query(_DB_EXISTS.args(NAME, "x/xml"), "true");
    query(_DB_EXISTS.args(NAME, "x/raw"), "true");
    query(_DB_EXISTS.args(NAME, "xxx"), "false");
    query(_DB_EXISTS.args(NAME, "x"), "false");
    query(_DB_EXISTS.args(NAME, ""), "false");
    // false expected for missing database
    new DropDB(NAME).execute(context);
    query(_DB_EXISTS.args(NAME), "false");
  }

  /**
   * Test method for the db:is-xml() function.
   */
  @Test
  public void dbIsXML() {
    check(_DB_IS_XML);
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_IS_XML.args(NAME, "xml"), "true");
    query(_DB_IS_XML.args(NAME, "raw"), "false");
    query(_DB_IS_XML.args(NAME, "xxx"), "false");
  }

  /**
   * Test method for the db:content-type() function.
   */
  @Test
  public void dbContentType() {
    check(_DB_CONTENT_TYPE);
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_CONTENT_TYPE.args(NAME, "xml"), MimeTypes.APP_XML);
    query(_DB_CONTENT_TYPE.args(NAME, "raw"), MimeTypes.APP_OCTET);
    error(_DB_CONTENT_TYPE.args(NAME, "test"), Err.RESFNF);
  }
}
