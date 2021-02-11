package org.basex.api.xmldb;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.xmldb.api.*;
import org.xmldb.api.base.*;

/**
 * This class tests the XMLDB/API Database implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DatabaseTest extends XMLDBBaseTest {
  /** Database. */
  private BXDatabase database;

  /**
   * Initializes a test.
   * @throws Exception any exception
   */
  @BeforeEach public void setUp() throws Exception {
    createDB();
    database = (BXDatabase) Class.forName(DRIVER).getDeclaredConstructor().newInstance();
  }

  /**
   * Finalizes a test.
   */
  @AfterEach public void tearDown() {
    dropDB();
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testAcceptsURI() throws Exception {
    database.acceptsURI(PATH);

    try {
      database.acceptsURI("bla");
      fail("URI was invalid.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_URI, ex);
    }
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetCollection() throws Exception {
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

  /**
   * Test.
   */
  @Test public void testGetConformanceLevel() {
    assertEquals(database.getConformanceLevel(), "0");
  }

  /**
   * Test.
   */
  @Test public void testGetName() {
    assertNotNull(database.getName());
  }

  /**
   * Test.
   */
  @Test public void testGetProperty() {
    assertNull(database.getProperty("ProbablyUnknown"));

    // the following tests are database specific...
    assertEquals("false", database.getProperty("queryinfo"));
    assertEquals("1", database.getProperty("runs"));
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testSetProperty() throws Exception {
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
  private static void checkCode(final int exp, final XMLDBException ex) {
    assertEquals(exp, ex.errorCode, "Wrong error code.");
  }
}
