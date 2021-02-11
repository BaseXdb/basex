package org.basex.api.xmldb;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API ResourceIterator implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ResourceIteratorTest extends XMLDBBaseTest {
  /** Collection. */
  private Collection collection;
  /** Resource. */
  private XPathQueryService service;

  /**
   * Initializes a test.
   * @throws Exception any exception
   */
  @BeforeEach public void setUp() throws Exception {
    createDB();
    final Class<?> clzz = Class.forName(DRIVER);
    final Database database = (Database) clzz.getDeclaredConstructor().newInstance();
    collection = database.getCollection(PATH, LOGIN, PW);
    service = (XPathQueryService) collection.getService("XPathQueryService", "1.0");
  }

  /**
   * Finalizes a test.
   * @throws Exception any exception
   */
  @AfterEach public void tearDown() throws Exception {
    collection.close();
    dropDB();
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testHasMoreResources() throws Exception {
    // test result
    ResourceIterator iter = service.query("/").getIterator();
    assertTrue(iter.hasMoreResources(), "Result expected.");

    // test empty result
    iter = service.query("//Unknown").getIterator();
    assertFalse(iter.hasMoreResources(), "Result expected.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testNextResource() throws Exception {
    // count down number of results
    final ResourceSet set = service.query("//node()");
    final ResourceIterator iter = set.getIterator();
    long size = set.getSize();
    while(iter.hasMoreResources()) {
      iter.nextResource();
      --size;
    }
    assertEquals(0, size, "Wrong result size.");

    // test if iterator delivers more results
    try {
      iter.nextResource();
      fail("No resources left.");
    } catch(final XMLDBException ex) {
      assertEquals(ErrorCodes.NO_SUCH_RESOURCE, ex.errorCode, "Wrong error code.");
    }
  }
}
