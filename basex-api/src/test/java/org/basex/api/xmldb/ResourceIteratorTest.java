package org.basex.api.xmldb;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API ResourceIterator implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
  @Before public void setUp() throws Exception {
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
  @After public void tearDown() throws Exception {
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
    assertTrue("Result expected.", iter.hasMoreResources());

    // test empty result
    iter = service.query("//Unknown").getIterator();
    assertFalse("Result expected.", iter.hasMoreResources());
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
    assertEquals("Wrong result size.", 0, size);

    // test if iterator delivers more results
    try {
      iter.nextResource();
      fail("No resources left.");
    } catch(final XMLDBException ex) {
      assertEquals("Wrong error code.", ErrorCodes.NO_SUCH_RESOURCE,
          ex.errorCode);
    }
  }
}
