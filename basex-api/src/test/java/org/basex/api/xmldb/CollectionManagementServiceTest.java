package org.basex.api.xmldb;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API CollectionManagementService implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
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
  private Collection coll;
  /** Resource. */
  private CollectionManagementService serv;

  @Before
  public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    db = (Database) c.newInstance();
    coll = db.getCollection(PATH, LOGIN, PW);
    serv = (CollectionManagementService) coll.getService(CMS, "1.0");
  }

  @After
  public void tearDown() throws Exception {
    coll.close();
    dropDB();
  }

  @Test
  public void testCreateCollection() throws Exception {
    // create a collection
    final Collection coll1 = serv.createCollection(TEMP);
    assertNotSame(coll, coll1);

    // add a document
    final Resource res1 = coll.createResource(TEST, XMLResource.RESOURCE_TYPE);
    res1.setContent("<xml/>");
    coll1.storeResource(res1);
    coll1.close();
  }

  @Test
  public void testRemoveCollection() throws Exception {
    serv.removeCollection(TEMP);
    assertNull("Collection was not removed.", db.getCollection(URL + TEMP, null, null));
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals(CMS, serv.getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals("1.0", serv.getVersion());
  }

  @Test
  public void testSetCollection() {
    // nothing serious to test...
  }
}
