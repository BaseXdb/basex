package org.basex.api.xmldb;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API CollectionManagementService implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CollectionManagementServiceTest extends XMLDBBaseTest {
  /** CollectionManagementService string. */
  private static final String CMS = "CollectionManagementService";
  /** Temporary collection. */
  static final String TEMP = "XMLDBTemp";
  /** Test document. */
  static final String TEST = "test";
  /** Collection. */
  private Database db;
  /** Collection. */
  private Collection collection;
  /** Resource. */
  private CollectionManagementService service;

  /**
   * Initializes a test.
   * @throws Exception any exception
   */
  @BeforeEach public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    db = (Database) c.getDeclaredConstructor().newInstance();
    collection = db.getCollection(PATH, LOGIN, PW);
    service = (CollectionManagementService) collection.getService(CMS, "1.0");
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
  @Test public void testCreateCollection() throws Exception {
    // create a collection
    final Collection coll1 = service.createCollection(TEMP);
    assertNotSame(collection, coll1);

    // add a document
    final Resource res1 = collection.createResource(TEST, XMLResource.RESOURCE_TYPE);
    res1.setContent("<xml/>");
    coll1.storeResource(res1);
    coll1.close();
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testRemoveCollection() throws Exception {
    service.removeCollection(TEMP);
    assertNull(db.getCollection(URL + TEMP, null, null), "Collection was not removed.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetName() throws Exception {
    assertEquals(CMS, service.getName());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetVersion() throws Exception {
    assertEquals("1.0", service.getVersion());
  }
}
