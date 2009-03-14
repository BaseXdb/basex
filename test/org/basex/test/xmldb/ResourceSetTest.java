package org.basex.test.xmldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import junit.framework.TestCase;

/**
 * This class tests the XMLDB/API ResourceSet implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class ResourceSetTest extends TestCase {
  /** Collection. */
  private static Collection coll;
  /** Resource. */
  private static XPathQueryService serv;

  @Before
  @Override
  protected void setUp() throws Exception {
    final Class<?> c = Class.forName(AllTests.DRIVER);
    final Database database = (Database) c.newInstance();
    coll = database.getCollection(AllTests.PATH, AllTests.LOGIN, AllTests.PW);
    serv = (XPathQueryService) coll.getService("XPathQueryService", "1.0");
  }

  @After
  @Override
  protected void tearDown() throws Exception {
    coll.close();
  }

  @Test
  public void testGetResource() throws Exception {
    // request resource
    final ResourceSet set = serv.query("//node()");
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

  @Test
  public void testAddResource() throws Exception {
    // perform two queries
    final ResourceSet set1 = serv.query("1");
    final ResourceSet set2 = serv.query("2");

    // add second to first result set
    final long size = set1.getSize();
    set1.addResource(set2.getResource(0));
    assertEquals("Wrong size of result set.", size + 1, set1.getSize());
  }

  @Test
  public void testRemoveResource() throws Exception {
    // perform query and remove result
    final ResourceSet set = serv.query("1");
    set.removeResource(0);
    assertEquals("Wrong size of result set.", 0, set.getSize());
  }

  @Test
  public void testGetIterator() throws Exception {
    // test if iterator yields results
    final ResourceSet set = serv.query("1");
    set.removeResource(0);
    final ResourceIterator iter = set.getIterator();
    assertFalse("No results expected.", iter.hasMoreResources());
  }

  @Test
  public void testGetMembersAsResource() throws Exception {
    // test created resource
    final ResourceSet set = serv.query("1");
    final Resource res = set.getMembersAsResource();
    assertNull("No ID expected.", res.getId());
    assertSame("Wrong collection reference.", res.getParentCollection(), coll);
  }

  @Test
  public void testGetSize() throws Exception {
    // test created resource
    final ResourceSet set = serv.query("1");
    assertEquals("Wrong result size.", 1, set.getSize());
    set.removeResource(0);
    assertEquals("Wrong result size.", 0, set.getSize());
  }

  @Test
  public void testClear() throws Exception {
    // test created resource
    final ResourceSet set = serv.query("1");
    set.clear();
    assertEquals("Results were not deleted.", 0, set.getSize());
  }
}
