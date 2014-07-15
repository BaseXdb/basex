package org.basex.api.xmldb;

import static org.junit.Assert.*;

import org.basex.io.*;
import org.junit.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API XPathQueryService implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class XPathQueryServiceTest extends XMLDBBaseTest {
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
    final Resource res = coll.createResource(DOC2, XMLResource.RESOURCE_TYPE);
    res.setContent(new IOFile(DOCPATH, DOC2).read());
    coll.storeResource(res);
    assertEquals("Wrong result size", 6, serv.query("//node()").getSize());

    // remove second document
    coll.removeResource(res);
  }

  @Test
  public void testQueryResource() throws Exception {
     assertEquals("Wrong result size", 3, serv.queryResource(DOC1, "//node()").getSize());

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

