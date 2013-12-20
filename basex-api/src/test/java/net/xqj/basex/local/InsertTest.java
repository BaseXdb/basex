package net.xqj.basex.local;

import com.xqj2.XQConnection2;
import static org.junit.Assert.*;
import static net.xqj.basex.BaseXXQInsertOptions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.xquery.*;

/**
 * Testing XQJ insert
 *
 * @author Charles Foster
 */
public class InsertTest extends XQJBaseTest {

  static final String DB = "xqj-test-database";
  static final String URI = "doc.xml";

  @Before
  public void setUp() throws XQException {
    super.setUp();

    XQExpression xqpe = xqc.createExpression();
    xqpe.executeCommand("CREATE DB "+DB);
    xqpe.executeCommand("SET DEFAULTDB true");
    xqpe.executeCommand("OPEN "+DB);
    xqpe.close();
  }

  @After
  public void tearDown() throws XQException {
    XQExpression xqpe = xqc.createExpression();
    xqpe.executeCommand("DROP DB "+DB);
    super.tearDown();
  }

  /**
   * Testing regular insert
  **/
  @Test
  public void testInsert() throws Throwable {
    XQConnection2 xqc2 = (XQConnection2)xqc;

    xqc2.insertItem(URI, createDocument("<e>hello</e>"), null);
    assertTrue(docAvailable(URI));
    assertTrue(dbExists(DB, URI));
  }

  /**
   * Testing insert via ADD strategy
   **/
  @Test
  public void testAdd() throws Throwable {
    XQConnection2 xqc2 = (XQConnection2)xqc;
    xqc2.insertItem(URI, createDocument("<e>a</e>"), options(ADD));

    assertTrue(docAvailable(URI));
    assertTrue(dbExists(DB, URI));

    xqc2.insertItem(URI, createDocument("<e>b</e>"), options(ADD));
    assertEquals(2, countUris(DB, URI));
  }

  /**
   * Testing insert via REPLACE strategy
   **/
  @Test
  public void testReplace() throws Throwable {
    XQConnection2 xqc2 = (XQConnection2)xqc;
    xqc2.insertItem(URI, createDocument("<e>a</e>"), options(REPLACE));

    assertTrue(docAvailable(URI));
    assertTrue(dbExists(DB, URI));

    xqc2.insertItem(URI, createDocument("<e>b</e>"), options(REPLACE));
    assertEquals(1, countUris(DB, URI));
  }

  /**
   * Testing insert via STORE strategy
   **/
  @Test
  public void testStore() throws Throwable {
    XQConnection2 xqc2 = (XQConnection2)xqc;
    xqc2.insertItem(URI, createDocument("<e>a</e>"), options(STORE));
    assertTrue(dbExists(DB, URI));

    xqc2.insertItem(URI, createDocument("<e>b</e>"), options(STORE));
    assertEquals(1, countUris(DB, URI));
  }

  // --------------------------------------------------------------------------
  // Helper methods
  // --------------------------------------------------------------------------

  private boolean dbExists(String db, String uri) throws XQException {
    XQResultSequence rs =
      xqc.createExpression().executeQuery(
        "db:exists('"+db+"', '"+uri+"')"
      );
    rs.next();
    return rs.getBoolean();
  }

  private int countUris(String db, String uri) throws XQException {
    XQResultSequence rs =
      xqc.createExpression().executeQuery(
        "xs:int(fn:count(db:list('"+db+"', '"+uri+"')))"
      );
    rs.next();
    return rs.getInt();
  }
  // --------------------------------------------------------------------------

}
