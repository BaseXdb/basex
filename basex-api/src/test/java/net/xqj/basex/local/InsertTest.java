package net.xqj.basex.local;

import static net.xqj.basex.BaseXXQInsertOptions.*;
import static org.junit.Assert.*;

import javax.xml.xquery.*;

import org.junit.*;

import com.xqj2.*;

/**
 * Testing XQJ insert.
 *
 * @author Charles Foster
 */
public final class InsertTest extends XQJBaseTest {
  /** Name of test database. */
  private static final String DB = "xqj-test-database";
  /** Name of test document. */
  private static final String URI = "doc.xml";

  @Before
  @Override
  public void setUp() throws XQException {
    super.setUp();

    final XQExpression xqpe = xqc.createExpression();
    xqpe.executeCommand("CREATE DB " + DB);
    xqpe.executeCommand("SET DEFAULTDB true");
    xqpe.executeCommand("OPEN " + DB);
    xqpe.close();
  }

  @After
  @Override
  public void tearDown() throws XQException {
    final XQExpression xqpe = xqc.createExpression();
    xqpe.executeCommand("DROP DB " + DB);
    super.tearDown();
  }

  /**
   * Testing regular insert.
   * @throws XQException query exception
   **/
  @Test
  public void testInsert() throws XQException {
    final XQConnection2 xqc2 = (XQConnection2) xqc;

    xqc2.insertItem(URI, createDocument("<e>hello</e>"), null);
    assertTrue(docAvailable(URI));
    assertTrue(dbExists(DB, URI));
  }

  /**
   * Testing insert via ADD strategy.
   * @throws XQException query exception
   **/
  @Test
  public void testAdd() throws XQException {
    final XQConnection2 xqc2 = (XQConnection2) xqc;
    xqc2.insertItem(URI, createDocument("<e>a</e>"), options(ADD));

    assertTrue(docAvailable(URI));
    assertTrue(dbExists(DB, URI));

    xqc2.insertItem(URI, createDocument("<e>b</e>"), options(ADD));
    assertEquals(2, countUris(DB, URI));
  }

  /**
   * Testing insert via REPLACE strategy.
   * @throws XQException query exception
   **/
  @Test
  public void testReplace() throws XQException {
    final XQConnection2 xqc2 = (XQConnection2) xqc;
    xqc2.insertItem(URI, createDocument("<e>a</e>"), options(REPLACE));

    assertTrue(docAvailable(URI));
    assertTrue(dbExists(DB, URI));

    xqc2.insertItem(URI, createDocument("<e>b</e>"), options(REPLACE));
    assertEquals(1, countUris(DB, URI));
  }

  /**
   * Testing insert via STORE strategy.
   * @throws XQException query exception
   **/
  @Test
  public void testStore() throws XQException {
    final XQConnection2 xqc2 = (XQConnection2) xqc;
    xqc2.insertItem(URI, createDocument("<e>a</e>"), options(STORE));
    assertTrue(dbExists(DB, URI));

    xqc2.insertItem(URI, createDocument("<e>b</e>"), options(STORE));
    assertEquals(1, countUris(DB, URI));
  }

  // --------------------------------------------------------------------------
  // Helper methods
  // --------------------------------------------------------------------------

  /**
   * Checks if a resource exists.
   * @param db database
   * @param uri URI
   * @return result of check
   * @throws XQException query exception
   */
  private boolean dbExists(final String db, final String uri) throws XQException {
    final XQResultSequence rs =
      xqc.createExpression().executeQuery(
        "db:exists('" + db + "', '" + uri + "')"
      );
    rs.next();
    return rs.getBoolean();
  }

  /**
   * Counts the resources in the specified database and URI.
   * @param db database
   * @param uri URI
   * @return number of resources
   * @throws XQException query exception
   */
  private int countUris(final String db, final String uri) throws XQException {
    final XQResultSequence rs =
      xqc.createExpression().executeQuery(
        "xs:int(fn:count(db:list('" + db + "', '" + uri + "')))"
      );
    rs.next();
    return rs.getInt();
  }
  // --------------------------------------------------------------------------

}
