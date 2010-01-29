package org.basex.test.xmldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import junit.framework.TestCase;

/**
 * This class tests the XMLDB/API XPathQueryService implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class XPathQueryServiceTest extends TestCase {
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
  public void testSetNamespace() throws Exception {
    // overwriting namespaces
    serv.setNamespace("hell", "a");
    serv.setNamespace("hell", "o");
    assertEquals("Namespace not found.", "o", serv.getNamespace("hell"));

    // testing invalid URIs
    try {
      serv.setNamespace("hell", null);
      fail("Null URIs are not allowed.");
    } catch(final XMLDBException ex) { }

    try {
      coll.createResource("hans", "");
      fail("Empty URIs are not allowed.");
    } catch(final XMLDBException ex) { }
  }

  @Test
  public void testGetNamespace() throws Exception {
    // testing former namespace
    assertNull("Namespaces shouldn't be global.", serv.getNamespace("hell"));

    // setting and requesting default namespace
    serv.setNamespace(null, "def");
    assertEquals("No default Namespace.", "def", serv.getNamespace(null));
    assertEquals("No default Namespace.", "def", serv.getNamespace(""));
  }

  @Test
  public void testRemoveNamespace() throws Exception {
    // set and remove namespace
    serv.setNamespace("hell", "a");
    serv.removeNamespace("hell");
    assertNull("Namespace was not removed.", serv.getNamespace("hell"));

    // set and remove default namespace
    serv.setNamespace(null, "def");
    serv.removeNamespace("");
    assertNull("Namespace was not removed.", serv.getNamespace(null));
  }

  @Test
  public void testClearNamespace() throws Exception {
    // set and clear namespace
    serv.setNamespace("hell", "a");
    serv.clearNamespaces();
    assertNull("Namespace was not removed.", serv.getNamespace("hell"));
  }

  @Test
  public void testQuery() throws Exception {
    // catch query errors
    try {
      serv.query("1+");
      fail("Buggy query was accepted.");
    } catch(final XMLDBException ex) { }

    assertEquals("Wrong result size.", 1, serv.query("/").getSize());

    // add second document
    final Resource res = coll.createResource(AllTests.DOC2,
        XMLResource.RESOURCE_TYPE);
    res.setContent(AllTests.read(AllTests.DOCPATH + AllTests.DOC2));
    coll.storeResource(res);
    assertEquals("Wrong result size", 6, serv.query("//node()").getSize());

    // remove second document
    coll.removeResource(res);
  }

  @Test
  public void testQueryResource() throws Exception {
     assertEquals("Wrong result size", 3, serv.queryResource(AllTests.DOC1,
      "//node()").getSize());

    // catch query errors
    try {
      serv.queryResource("UnknownDoc", "/");
      fail("Query was executed on unknown document.");
    } catch(final XMLDBException ex) { /* ignored. */ }
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals("XPathQueryService", serv.getName());
  }

  @Test
  public void testGetVersion() throws Exception {
    assertEquals("1.0", serv.getVersion());
  }

  @Test
  public void testSetCollection() throws Exception {
    // nothing serious to test...
  }
}

