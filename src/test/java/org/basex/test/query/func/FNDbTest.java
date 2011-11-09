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
    check(DBOPEN);
    query(COUNT.args(DBOPEN.args(DB)), "1");
    query(COUNT.args(DBOPEN.args(DB, "")), "1");
    query(COUNT.args(DBOPEN.args(DB, "unknown")), "0");

    // close database instance
    new Close().execute(CONTEXT);
    query(COUNT.args(DBOPEN.args(DB, "unknown")), "0");
    query(DBOPEN.args(DB) + "//title/text()", "XML");

    // run function on non-existing database
    new DropDB(DB).execute(CONTEXT);
    error(DBOPEN.args(DB), Err.NODB);
  }

  /**
   * Test method for the db:open-pre() function.
   */
  @Test
  public void dbOpenPre() {
    check(DBOPENPRE);
    query(DBOPENPRE.args(DB, 0) + "//title/text()", "XML");
    error(DBOPENPRE.args(DB, -1), Err.IDINVALID);
  }

  /**
   * Test method for the db:open-id() function.
   */
  @Test
  public void dbOpenId() {
    check(DBOPENID);
    query(DBOPENID.args(DB, 0) + "//title/text()", "XML");
    error(DBOPENID.args(DB, -1), Err.IDINVALID);
  }

  /**
   * Test method for the db:text() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbText() throws BaseXException {
    check(DBTEXT);
    // run function without and with index
    new DropIndex("text").execute(CONTEXT);
    query(DBTEXT.args(DB, "XML"), "XML");
    new CreateIndex("text").execute(CONTEXT);
    query(DBTEXT.args(DB, "XML"), "XML");
    query(DBTEXT.args(DB, "XXX"), "");
  }

  /**
   * Test method for the db:attribute() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbAttribute() throws BaseXException {
    check(DBATTR);
    // run function without and with index
    new DropIndex("attribute").execute(CONTEXT);
    query(DATA.args(DBATTR.args(DB, "0")), "0");
    new CreateIndex("attribute").execute(CONTEXT);
    query(DATA.args(DBATTR.args(DB, "0")), "0");
    query(DATA.args(DBATTR.args(DB, "0", "id")), "0");
    query(DATA.args(DBATTR.args(DB, "0", "XXX")), "");
    query(DATA.args(DBATTR.args(DB, "XXX")), "");
  }

  /**
   * Test method for the db:fulltext() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbFulltext() throws BaseXException {
    check(DBFULLTEXT);
    // run function without and with index
    new DropIndex("fulltext").execute(CONTEXT);
    error(DBFULLTEXT.args(DB, "assignments"), Err.NOIDX);
    new CreateIndex("fulltext").execute(CONTEXT);
    query(DBFULLTEXT.args(DB, "assignments"), "Assignments");
    query(DBFULLTEXT.args(DB, "XXX"), "");
  }

  /**
   * Test method for the db:list() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbList() throws BaseXException {
    check(DBLIST);
    // add documents
    new Add("test/docs", FLDR).execute(CONTEXT);
    contains(DBLIST.args(DB), "test/");
    // create two other database and compare substring
    new CreateDB(DB + 1).execute(CONTEXT);
    new CreateDB(DB + 2).execute(CONTEXT);
    contains(DBLIST.args(), DB + 1 + ' ' + DB + 2);
    new DropDB(DB + 1).execute(CONTEXT);
    new DropDB(DB + 2).execute(CONTEXT);
  }

  /**
   * Test method for the db:system() function.
   */
  @Test
  public void dbSystem() {
    check(DBSYSTEM);
    contains(DBSYSTEM.args(), INFOON);
  }

  /**
   * Test method for the db:info() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbInfo() throws BaseXException {
    check(DBINFO);
    // standard test
    contains(DBINFO.args(DB), INFOON);

    // drop indexes and check index queries
    final String[] types = { "text", "attribute", "fulltext" };
    for(final String type : types) new DropIndex(type).execute(CONTEXT);
    for(final String type : types) query(DBINFO.args(DB, type));
    // create indexes and check index queries
    for(final String type : types) new CreateIndex(type).execute(CONTEXT);
    for(final String type : types) query(DBINFO.args(DB, type));
    // check name indexes
    query(DBINFO.args(DB, "tag"));
    query(DBINFO.args(DB, "attname"));
    error(DBINFO.args(DB, "XXX"), Err.NOIDX);
  }

  /**
   * Test method for db:node-id() function.
   */
  @Test
  public void dbNodeID() {
    check(DBNODEID);
    query(DBNODEID.args(" /html"), "1");
    query(DBNODEID.args(" / | /html"), "0 1");
  }

  /**
   * Test method for db:node-pre() function.
   */
  @Test
  public void dbNodePre() {
    check(DBNODEPRE);
    query(DBNODEPRE.args(" /html"), "1");
    query(DBNODEPRE.args(" / | /html"), "0 1");
  }

  /**
   * Test method for db:event() function.
   */
  @Test
  public void dbEvent() {
    check(DBEVENT);
    error(DBEVENT.args("X", "Y"), Err.NOEVENT);
  }

  /**
   * Test method for the db:add() function.
   */
  @Test
  public void dbAdd() {
    check(DBADD);
    query(DBADD.args(DB, "\"<root/>\"", "t1.xml"));
    query(COUNT.args(COLL.args(DB + "/t1.xml") + "/root"), "1");

    query(DBADD.args(DB, " document { <root/> }", "t2.xml"));
    query(COUNT.args(COLL.args(DB + "/t2.xml") + "/root"), "1");

    query(DBADD.args(DB, " document { <root/> }", "test/t3.xml"));
    query(COUNT.args(COLL.args(DB + "/test/t3.xml") + "/root"), "1");

    query(DBADD.args(DB, FILE, "in/"));
    query(COUNT.args(COLL.args(DB + "/in/input.xml") + "/html"), "1");

    query(DBADD.args(DB, FILE, "test/t4.xml"));
    query(COUNT.args(COLL.args(DB + "/test/t4.xml") + "/html"), "1");

    query(DBADD.args(DB, FLDR, "test/dir"));
    query(COUNT.args(COLL.args(DB + "/test/dir")), NFLDR);

    query("for $f in " + FLLIST.args(FLDR) + " return " +
        DBADD.args(DB, "$f", "dir"));
    query(COUNT.args(COLL.args(DB + "/dir")), NFLDR);

    query("for $i in 1 to 3 return " +
        DBADD.args(DB, "\"<root/>\"", "\"doc\" || $i"));
    query(COUNT.args(" for $i in 1 to 3 return " +
        COLL.args("\"" + DB + "/doc\" || $i")), 3);
  }

  /**
   * Test method for the db:add() function with document with namespaces.
   */
  @Test
  public void dbAddWithNS() {
    query(DBADD.args(DB, " document { <x xmlns:a='a' a:y='' /> }", "x"));
  }

  /**
   * Test method for the db:delete() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbDelete() throws BaseXException {
    check(DBDELETE);
    new Add("test/docs", FLDR).execute(CONTEXT);
    query(DBDELETE.args(DB, "test"));
    query(COUNT.args(COLL.args(DB + "/test")), 0);
  }

  /**
   * Test method for the db:rename() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbRename() throws BaseXException {
    check(DBRENAME);

    new Add("test/docs", FLDR).execute(CONTEXT);
    query(COUNT.args(COLL.args(DB + "/test")), NFLDR);

    // rename document
    query(DBRENAME.args(DB, "test", "newtest"));
    query(COUNT.args(COLL.args(DB + "/test")), 0);
    query(COUNT.args(COLL.args(DB + "/newtest")), NFLDR);

    // rename binary file
    query(DBSTORE.args(DB, "one", ""));
    query(DBRENAME.args(DB, "one", "two"));
    query(DBRETRIEVE.args(DB, "two"));
    error(DBRETRIEVE.args(DB, "one"), Err.RESFNF);
  }

  /**
   * Test method for the db:replace() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbReplace() throws BaseXException {
    check(DBREPLACE);

    new Add("test", FILE).execute(CONTEXT);

    query(DBREPLACE.args(DB, FILE, "\"<R1/>\""));
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/R1"), 1);
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/R2"), 0);

    query(DBREPLACE.args(DB, FILE, " document { <R2/> }"));
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/R2"), 1);

    query(DBREPLACE.args(DB, FILE, FILE));
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/R1"), 0);
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/R2"), 0);
    query(COUNT.args(COLL.args(DB + '/' + FILE) + "/html"), 1);
  }

  /**
   * Test method for the db:optimize() function.
   */
  @Test
  public void dbOptimize() {
    check(DBOPTIMIZE);
    query(DBOPTIMIZE.args(DB));
    query(DBOPTIMIZE.args(DB, "true()"));
  }

  /**
   * Test method for the db:retrieve() function.
   */
  @Test
  public void dbRetrieve() {
    check(DBRETRIEVE);
    error(DBRETRIEVE.args(DB, "raw"), Err.RESFNF);
    query(DBSTORE.args(DB, "raw", "xs:hexBinary('41')"));
    query(DBRETRIEVE.args(DB, "raw"), "41");
    query(DBDELETE.args(DB, "raw"));
    error(DBRETRIEVE.args(DB, "raw"), Err.RESFNF);
  }

  /**
   * Test method for the db:store() function.
   */
  @Test
  public void dbStore() {
    check(DBSTORE);
    query(DBSTORE.args(DB, "raw1", "xs:hexBinary('41')"));
    query(DBSTORE.args(DB, "raw2", "b"));
    query(DBRETRIEVE.args(DB, "raw2"), "62");
    query(DBSTORE.args(DB, "raw3", 123));
    query(DBRETRIEVE.args(DB, "raw3"), "313233");
  }

  /**
   * Test method for the db:is-raw() function.
   */
  @Test
  public void dbIsRaw() {
    check(DBISRAW);
    query(DBADD.args(DB, "\"<a/>\"", "xml"));
    query(DBSTORE.args(DB, "raw", "bla"));
    query(DBISRAW.args(DB, "xml"), "false");
    query(DBISRAW.args(DB, "raw"), "true");
    query(DBISRAW.args(DB, "xxx"), "false");
  }

  /**
   * Test method for the db:exists() function.
   * @throws BaseXException database exception
   */
  @Test
  public void dbExists() throws BaseXException {
    check(DBEXISTS);
    query(DBADD.args(DB, "\"<a/>\"", "x/xml"));
    query(DBSTORE.args(DB, "x/raw", "bla"));
    // checks if the specified resources exist (false expected for directories)
    query(DBEXISTS.args(DB), "true");
    query(DBEXISTS.args(DB, "x/xml"), "true");
    query(DBEXISTS.args(DB, "x/raw"), "true");
    query(DBEXISTS.args(DB, "xxx"), "false");
    query(DBEXISTS.args(DB, "x"), "false");
    query(DBEXISTS.args(DB, ""), "false");
    // false expected for missing database
    new DropDB(DB).execute(CONTEXT);
    query(DBEXISTS.args(DB), "false");
  }

  /**
   * Test method for the db:is-xml() function.
   */
  @Test
  public void dbIsXML() {
    check(DBISXML);
    query(DBADD.args(DB, "\"<a/>\"", "xml"));
    query(DBSTORE.args(DB, "raw", "bla"));
    query(DBISXML.args(DB, "xml"), "true");
    query(DBISXML.args(DB, "raw"), "false");
    query(DBISXML.args(DB, "xxx"), "false");
  }

  /**
   * Test method for the db:content-type() function.
   */
  @Test
  public void dbContentType() {
    check(DBCTYPE);
    query(DBADD.args(DB, "\"<a/>\"", "xml"));
    query(DBSTORE.args(DB, "raw", "bla"));
    query(DBCTYPE.args(DB, "xml"), MimeTypes.APP_XML);
    query(DBCTYPE.args(DB, "raw"), MimeTypes.APP_OCTET);
    error(DBCTYPE.args(DB, "test"), Err.RESFNF);
  }

  /**
   * Test method for the db:details() function.
   */
  @Test
  public void dbDetails() {
    check(DBDETAILS);
    query(DBADD.args(DB, "\"<a/>\"", "xml"));
    query(DBSTORE.args(DB, "raw", "bla"));

    final String xmlCall = DBDETAILS.args(DB, "xml");
    query(xmlCall + "/@path/data()", "xml");
    query(xmlCall + "/@raw/data()", "false");
    query(xmlCall + "/@content-type/data()", MimeTypes.APP_XML);
    query(xmlCall + "/@modified-date/data()", CONTEXT.data().meta.time);
    query(xmlCall + "/@size/data()", "");

    final String rawCall = DBDETAILS.args(DB, "raw");
    query(rawCall + "/@path/data()", "raw");
    query(rawCall + "/@raw/data()", "true");
    query(rawCall + "/@content-type/data()", MimeTypes.APP_OCTET);
    query(rawCall + "/@modified-date/data() > 0", "true");
    query(rawCall + "/@size/data()", "3");

    error(DBDETAILS.args(DB, "test"), Err.RESFNF);
  }
}
