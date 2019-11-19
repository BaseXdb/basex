package org.basex.api.xmldb;

import static org.junit.Assert.*;

import org.basex.io.*;
import org.junit.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API XPathQueryService implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class XPathQueryServiceTest extends XMLDBBaseTest {
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
  @Test public void testSetNamespace() throws Exception {
    // overwriting namespaces
    service.setNamespace("hell", "a");
    service.setNamespace("hell", "o");
    assertEquals("Namespace not found.", "o", service.getNamespace("hell"));

    // testing invalid URIs
    try {
      service.setNamespace("hell", null);
      fail("Null URIs are not allowed.");
    } catch(final XMLDBException ignored) { }

    try {
      collection.createResource("hans", "");
      fail("Empty URIs are not allowed.");
    } catch(final XMLDBException ignored) { }
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetNamespace() throws Exception {
    // testing former namespace
    assertNull("Namespaces shouldn't be global.", service.getNamespace("hell"));

    // setting and requesting default namespace
    service.setNamespace(null, "def");
    assertEquals("No default Namespace.", "def", service.getNamespace(null));
    assertEquals("No default Namespace.", "def", service.getNamespace(""));
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testRemoveNamespace() throws Exception {
    // set and remove namespace
    service.setNamespace("hell", "a");
    service.removeNamespace("hell");
    assertNull("Namespace was not removed.", service.getNamespace("hell"));

    // set and remove default namespace
    service.setNamespace(null, "def");
    service.removeNamespace("");
    assertNull("Namespace was not removed.", service.getNamespace(null));
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testClearNamespace() throws Exception {
    // set and clear namespace
    service.setNamespace("hell", "a");
    service.clearNamespaces();
    assertNull("Namespace was not removed.", service.getNamespace("hell"));
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testQuery() throws Exception {
    // catch query errors
    try {
      service.query("1+");
      fail("Buggy query was accepted.");
    } catch(final XMLDBException ignored) { }

    assertEquals("Wrong result size.", 1, service.query("/").getSize());

    // add second document
    final Resource resource = collection.createResource(DOC2, XMLResource.RESOURCE_TYPE);
    resource.setContent(new IOFile(DOCPATH, DOC2).read());
    collection.storeResource(resource);
    assertEquals("Wrong result size", 6, service.query("//node()").getSize());

    // remove second document
    collection.removeResource(resource);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testQueryResource() throws Exception {
     assertEquals("Wrong result size", 3, service.queryResource(DOC1, "//node()").getSize());

    // catch query errors
    try {
      service.queryResource("UnknownDoc", "/");
      fail("Query was executed on unknown document.");
    } catch(final XMLDBException ignore) { }
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetName() throws Exception {
    assertEquals("XPathQueryService", service.getName());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetVersion() throws Exception {
    assertEquals("1.0", service.getVersion());
  }
}

