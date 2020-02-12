package org.basex.api.xmldb;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API ResourceSet implementation.
 *
 * @author BaseX Team 2005-20, BSD License
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
  @Before public void setUp() throws Exception {
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
  @After public void tearDown() throws Exception {
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
      assertEquals("Wrong error code.", ErrorCodes.NO_SUCH_RESOURCE,
          ex.errorCode);
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
    assertEquals("Wrong size of result set.", size + 1, set1.getSize());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testRemoveResource() throws Exception {
    // perform query and remove result
    final ResourceSet set = service.query("1");
    set.removeResource(0);
    assertEquals("Wrong size of result set.", 0, set.getSize());
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
    assertFalse("No results expected.", iter.hasMoreResources());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetMembersAsResource() throws Exception {
    // test created resource
    final ResourceSet set = service.query("1");
    final Resource resource = set.getMembersAsResource();
    assertNull("No ID expected.", resource.getId());
    assertEquals("Wrong result.", "<xmldb>1</xmldb>", resource.getContent());
    assertSame("Wrong collection reference.", resource.getParentCollection(), collection);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetSize() throws Exception {
    // test created resource
    final ResourceSet set = service.query("1");
    assertEquals("Wrong result size.", 1, set.getSize());
    set.removeResource(0);
    assertEquals("Wrong result size.", 0, set.getSize());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testClear() throws Exception {
    // test created resource
    final ResourceSet set = service.query("1");
    set.clear();
    assertEquals("Results were not deleted.", 0, set.getSize());
  }
}
