package org.basex.api.xmldb;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API ResourceSet implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ResourceSetTest extends XMLDBBaseTest {
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
    final Class<?> c = Class.forName(DRIVER);
    final Database database = (Database) c.getDeclaredConstructor().newInstance();
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
  @Test public void testGetResource() throws Exception {
    // request resource
    final ResourceSet set = service.query("//node()");
    assertNotNull(set.getResource(0));

    // specify invalid position
    try {
      set.getResource(-1);
      fail("Invalid index access.");
    } catch(final XMLDBException ex) {
      assertEquals(ErrorCodes.NO_SUCH_RESOURCE, ex.errorCode, "Wrong error code.");
    }
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testAddResource() throws Exception {
    // perform two queries
    final ResourceSet set1 = service.query("1");
    final ResourceSet set2 = service.query("2");

    // add second to first result set
    final long size = set1.getSize();
    set1.addResource(set2.getResource(0));
    assertEquals(size + 1, set1.getSize(), "Wrong size of result set.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testRemoveResource() throws Exception {
    // perform query and remove result
    final ResourceSet set = service.query("1");
    set.removeResource(0);
    assertEquals(0, set.getSize(), "Wrong size of result set.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetIterator() throws Exception {
    // test if iterator yields results
    final ResourceSet set = service.query("1");
    set.removeResource(0);
    final ResourceIterator iter = set.getIterator();
    assertFalse(iter.hasMoreResources(), "No results expected.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetMembersAsResource() throws Exception {
    // test created resource
    final ResourceSet set = service.query("1");
    final Resource resource = set.getMembersAsResource();
    assertNull(resource.getId(), "No ID expected.");
    assertEquals("<xmldb>1</xmldb>", resource.getContent(), "Wrong result.");
    assertSame(resource.getParentCollection(), collection, "Wrong collection reference.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetSize() throws Exception {
    // test created resource
    final ResourceSet set = service.query("1");
    assertEquals(1, set.getSize(), "Wrong result size.");
    set.removeResource(0);
    assertEquals(0, set.getSize(), "Wrong result size.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testClear() throws Exception {
    // test created resource
    final ResourceSet set = service.query("1");
    set.clear();
    assertEquals(0, set.getSize(), "Results were not deleted.");
  }
}
