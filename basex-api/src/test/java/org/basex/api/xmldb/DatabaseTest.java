package org.basex.api.xmldb;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmldb.api.*;
import org.xmldb.api.base.*;

/**
 * This class tests the XMLDB/API Database implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public final class DatabaseTest extends XMLDBBaseTest {
  /** Database. */
  private Database database;

  @Before
  public void setUp() throws Exception {
    createDB();
    database = (Database) Class.forName(DRIVER).newInstance();
  }

  @After
  public void tearDown() throws Exception {
    dropDB();
  }

  @Test
  public void testAcceptsURI() throws Exception {
    database.acceptsURI(PATH);

    try {
      database.acceptsURI("bla");
      fail("URI was invalid.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_URI, ex);
    }
  }

  @Test
  public void testGetCollection() throws Exception {
    // directly call and close database instance
    database.getCollection(PATH, LOGIN, PW).close();

    try {
      database.getCollection("bla", LOGIN, PW);
      fail("URI was invalid.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_URI, ex);
    }
    // get database from database manager
    DatabaseManager.registerDatabase(database);
    final Collection coll = DatabaseManager.getCollection(PATH, LOGIN, PW);
    coll.close();
  }

  @Test
  public void testGetConformanceLevel() throws Exception {
    assertEquals(database.getConformanceLevel(), "0");
  }

  @Test
  public void testGetName() throws Exception {
    assertNotNull(database.getName());
  }

  @Test
  public void testGetProperty() throws Exception {
    assertNull(database.getProperty("ProbablyUnknown"));

    // the following tests are database specific...
    assertEquals("false", database.getProperty("queryinfo"));
    assertEquals("1", database.getProperty("runs"));
  }

  @Test
  public void testSetProperty() throws Exception {
    try {
      database.setProperty("ProbablyUnknown", "on");
      fail("Invalid key was assigned.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.VENDOR_ERROR, ex);
    }

    // the following tests are database specific...
    database.setProperty("queryinfo", "on");
    database.setProperty("queryinfo", "off");

    try {
      database.setProperty("runs", "ABC");
      fail("Invalid value was assigned.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.VENDOR_ERROR, ex);
    }
  }

  /**
   * Compares the expected error code with the specified exception.
   * @param exp expected error code
   * @param ex exception
   */
  static void checkCode(final int exp, final XMLDBException ex) {
    assertEquals("Wrong error code.", exp, ex.errorCode);
  }
}
