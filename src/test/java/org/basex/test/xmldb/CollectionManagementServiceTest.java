package org.basex.test.xmldb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import junit.framework.TestCase;

/**
 * This class tests the XMLDB/API CollectionManagementService implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class CollectionManagementServiceTest extends TestCase {
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
  @Override
  protected void setUp() throws Exception {
    final Class<?> c = Class.forName(AllTests.DRIVER);
    db = (Database) c.newInstance();
    coll = db.getCollection(AllTests.PATH, AllTests.LOGIN, AllTests.PW);
    serv = (CollectionManagementService) coll.getService(
        "CollectionManagementService", "1.0");
  }

  @After
  @Override
  protected void tearDown() throws Exception {
    coll.close();
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
    assertNull("Collection was not removed.",
        db.getCollection(AllTests.URL + TEMP, null, null));
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals("CollectionManagementService", serv.getName());
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
