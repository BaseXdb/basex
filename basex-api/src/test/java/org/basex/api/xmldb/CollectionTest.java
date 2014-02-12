package org.basex.api.xmldb;

import static org.junit.Assert.*;

import java.io.*;

import javax.xml.parsers.*;

import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API Collection implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class CollectionTest extends XMLDBBaseTest {
  /** Collection. */
  Collection coll;

  @Before
  public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    final Database database = (Database) c.newInstance();
    coll = database.getCollection(PATH, LOGIN, PW);
  }

  @After
  public void tearDown() throws Exception {
    coll.close();
    dropDB();
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals(COLL, coll.getName());
  }

  @Test
  public void testGetServices() throws Exception {
    // get all services
    final Code code = new Code() {
      Object run() throws XMLDBException { return coll.getServices(); }
    };

    // find XPath implementation
    boolean xpath = false;
    for(final Service s : (Service[]) code.run()) {
      xpath |= s instanceof XPathQueryService;
    }
    assertTrue("XPath Implementation expected.", xpath);
    checkClosed(code);
  }

  @Test
  public void testGetService() throws Exception {
    // assume existence of XPath service
    assertNotNull("XPath Implementation expected.",
        coll.getService("XPathQueryService", "1.0"));

    // assume null reference for unknown version
    assertNull("Unknown version.", coll.getService("XPathQueryService", "3.8"));

    // get unknown service
    final Code code = new Code() {
      Object run() throws XMLDBException {
        return coll.getService("Unknown", "0.0");
      }
    };
    assertNull("No 'Unknown' service expected.", code.run());
    checkClosed(code);
  }

  @Test
  public void testGetParentCollection() throws Exception {
    // assume there's no parent collection
    final Code code = new Code() {
      Object run() throws XMLDBException { return coll.getParentCollection(); }
    };
    assertNull("No parent collection expected.", code.run());
    checkClosed(code);
  }

  @Test
  public void testGetChildCollectionCount() throws Exception {
    // assume there's no child collection
    final Code code = new Code() {
      Object run() throws XMLDBException {
        return coll.getChildCollectionCount();
      }
    };
    assertEquals("No child collection expected.", 0, code.num());
    checkClosed(code);
  }

  @Test
  public void testListChildCollections() throws Exception {
    // assume there's no child collection
    final Code code = new Code() {
      Object run() throws XMLDBException { return coll.listChildCollections(); }
    };
    assertEquals("No child collection expected.", 0, code.strings().length);
    checkClosed(code);
  }

  @Test
  public void testGetChildCollection() throws Exception {
    // assume there's no child collection
    final Code code = new Code() {
      Object run() throws XMLDBException {
        return coll.getChildCollection("X");
      }
    };
    assertNull("No child collection expected.", code.run());
    checkClosed(code);
  }

  @Test
  public void testGetResourceCount() throws Exception {
    // tests could be added for here multiple documents
    final Code code = new Code() {
      Object run() throws XMLDBException { return coll.getResourceCount(); }
    };
    assertEquals("One document expected.", 1, code.num());
    checkClosed(code);
  }

  @Test
  public void testListResources() throws Exception {
    final Code code = new Code() {
      Object run() throws XMLDBException { return coll.listResources(); }
    };
    final String[] res = code.strings();
    assertEquals("One document expected.", 1, res.length);
    assertEquals("Wrong document name.", DOC1, res[0]);
    checkClosed(code);
  }

  @Test
  public void testCreateResource() throws Exception {
    // test unknown resource type
    try {
      coll.createResource("hans", "UnknownResource");
      fail("Resource Type is expected to be unknown.");
    } catch(final XMLDBException ex) { }

    // test xml resource and ID creation
    Resource res = coll.createResource(null, XMLResource.RESOURCE_TYPE);
    assertTrue("XMLResource expected.", res instanceof XMLResource);
    assertNotNull("No ID was created.", res.getId());

    // test adoption of specified id
    final String id = DOC2;
    res = coll.createResource(id, XMLResource.RESOURCE_TYPE);
    assertEquals("Resource has wrong ID.", id, res.getId());

    // tests could be added for here multiple documents
    final Code code = new Code() {
      Object run() throws XMLDBException {
        return coll.createResource(null, BinaryResource.RESOURCE_TYPE);
      }
    };

    // allow vendor error for binary resources
    try {
      code.run();
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.VENDOR_ERROR, ex);
    }
    checkClosed(code);
  }

  @Test
  public void testStoreResource() throws Exception {
    Resource res = coll.createResource("NoContent", XMLResource.RESOURCE_TYPE);

    // try to store resource with missing content
    try {
      coll.storeResource(res);
      fail("Resource has no contents.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_RESOURCE, ex);
    }

    // try to store erroneous content
    res = coll.createResource("Faulty", XMLResource.RESOURCE_TYPE);
    res.setContent("<xml");
    try {
      coll.storeResource(res);
      fail("Resource are faulty.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_RESOURCE, ex);
    }

    // try to store resource with missing contents
    res = coll.createResource("Correct", XMLResource.RESOURCE_TYPE);
    res.setContent("<xml/>");
    coll.storeResource(res);

    // store DOM instance
    final XMLResource xml1 = (XMLResource) coll.createResource(
        DOC2, XMLResource.RESOURCE_TYPE);
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Node node = builder.parse(new File(DOCPATH + DOC2));
    xml1.setContentAsDOM(node);
    coll.storeResource(xml1);

    // store SAX stream
    final XMLResource xml2 = (XMLResource) coll.createResource(
        DOC3, XMLResource.RESOURCE_TYPE);
    final XMLReader reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(xml2.setContentAsSAX());
    reader.parse(new InputSource(DOCPATH + DOC3));
    coll.storeResource(xml2);
    // check number of documents
    assertEquals("Wrong number of documents.", 4, coll.getResourceCount());

    // update document with known id
    res = coll.createResource("Correct", XMLResource.RESOURCE_TYPE);
    res.setContent("<XML/>");
    coll.storeResource(res);
    // check number of documents
    assertEquals("Wrong number of documents.", 4, coll.getResourceCount());

    checkClosed(new Code() {
      Object run() throws XMLDBException {
        return coll.createResource("id", null);
      }
    });
  }

  @Test
  public void testRemoveResource() throws Exception {
    final Resource res =
        coll.createResource("Correct", XMLResource.RESOURCE_TYPE);
    res.setContent("<xml/>");
    coll.storeResource(res);

    coll.removeResource(coll.getResource("Correct"));
    // check number of documents
    assertEquals("Wrong number of documents.", 1, coll.getResourceCount());

    try {
      coll.removeResource(coll.getResource("test"));
      fail("Document does not exist.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.NO_SUCH_RESOURCE, ex);
    }

    try {
      coll.removeResource(coll.getResource(null));
      fail("Document does not exist.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.NO_SUCH_RESOURCE, ex);
    }

    checkClosed(new Code() {
      Object run() throws XMLDBException {
        coll.removeResource(null); return null;
      }
    });
  }

  @Test
  public void testCreateId() throws Exception {
    final Code code = new Code() {
      Object run() throws XMLDBException { return coll.createId(); }
    };

    // check some ids for their uniqueness
    for(int i = 0; i < 10; ++i) {
      if(coll.getResource(code.run().toString()) != null) {
        fail("Returned ID not unique.");
      }
    }
    checkClosed(code);
  }

  @Test
  public void testIsOpen() throws Exception {
    assertTrue(coll.isOpen());
    coll.close();
    assertFalse(coll.isOpen());
  }

  @Test
  public void testClose() throws Exception {
    coll.close();
    coll.close();
    assertFalse(coll.isOpen());
  }

  @Test
  public void testGetProperty() throws Exception {
    assertNull(coll.getProperty("ProbablyUnknown"));

    // the following tests are database specific...
    assertEquals(COLL, coll.getProperty("name"));
    assertEquals("true", coll.getProperty("chop"));
  }

  @Test
  public void testSetProperty() throws Exception {
    try {
      coll.setProperty("ProbablyUnknown", "on");
      fail("Invalid key was assigned.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.VENDOR_ERROR, ex);
    }

    // the following tests are database specific...
    coll.setProperty("name", "NewName");
    coll.setProperty("name", COLL);

    try {
      coll.setProperty("time", "ABC");
      fail("Invalid value was assigned.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.VENDOR_ERROR, ex);
    }
  }

  /**
   * Compares the expected error code with the specified exception.
   * @param exp expected error code
   * @param ex exception
   */
  private static void checkCode(final int exp, final XMLDBException ex) {
    assertEquals("Wrong error code.", exp, ex.errorCode);
  }

  /**
   * Runs the specified code in a closed collection state.
   * @param code code to be executed
   */
  private void checkClosed(final Code code) {
    try {
      coll.close();
      code.run();
      fail("Database was closed.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.COLLECTION_CLOSED, ex);
    }
  }

  /**
   * Abstract class for defining arbitrary methods.
   */
  abstract static class Code {
    /**
     * Method to be executed.
     * @return result
     * @throws XMLDBException exception
     */
    abstract Object run() throws XMLDBException;

    /**
     * Returns the result of the method as integer.
     * @return integer result
     * @throws XMLDBException exception
     */
    int num() throws XMLDBException {
      return (Integer) run();
    }

    /**
     * Returns the result of the method as string array.
     * @return string array
     * @throws XMLDBException exception
     */
    String[] strings() throws XMLDBException {
      return (String[]) run();
    }
  }
}
