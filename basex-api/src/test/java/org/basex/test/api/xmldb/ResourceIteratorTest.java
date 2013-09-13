package org.basex.test.api.xmldb;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API ResourceIterator implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class ResourceIteratorTest extends XMLDBBaseTest {
  /** Collection. */
  private Collection coll;
  /** Resource. */
  private XPathQueryService serv;

  @Before
  public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    final Database database = (Database) c.newInstance();
    coll = database.getCollection(PATH, LOGIN, PW);
    serv = (XPathQueryService) coll.getService("XPathQueryService", "1.0");
  }

  @After
  public void tearDown() throws Exception {
    coll.close();
    dropDB();
  }

  @Test
  public void testHasMoreResources() throws Exception {
    // test result
    ResourceIterator iter = serv.query("/").getIterator();
    assertTrue("Result expected.", iter.hasMoreResources());

    // test empty result
    iter = serv.query("//Unknown").getIterator();
    assertFalse("Result expected.", iter.hasMoreResources());
  }

  @Test
  public void testNextResource() throws Exception {
    // count down number of results
    final ResourceSet set = serv.query("//node()");
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
