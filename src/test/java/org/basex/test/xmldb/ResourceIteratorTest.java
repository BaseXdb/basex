package org.basex.test.xmldb;

import static org.junit.Assert.*;

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
    assertEquals("Result expected.", true, iter.hasMoreResources());

    // test empty result
    iter = serv.query("//Unknown").getIterator();
    assertEquals("Result expected.", false, iter.hasMoreResources());
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
