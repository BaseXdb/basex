package org.basex.api.xmldb;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import javax.xml.parsers.*;

import org.junit.jupiter.api.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API Collection implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CollectionTest extends XMLDBBaseTest {
  /** Collection. */
  Collection collection;

  /**
   * Initializes a test.
   * @throws Exception any exception
   */
  @BeforeEach public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    final Database database = (Database) c.getDeclaredConstructor().newInstance();
    collection = database.getCollection(PATH, LOGIN, PW);
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
  @Test public void testGetName() throws Exception {
    assertEquals(COLL, collection.getName());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetServices() throws Exception {
    // get all services
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException { return collection.getServices(); }
    };

    // find XPath implementation
    boolean xpath = false;
    for(final Service s : (Service[]) code.run()) {
      xpath |= s instanceof XPathQueryService;
    }
    assertTrue(xpath, "XPath Implementation expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetService() throws Exception {
    // assume existence of XPath service
    assertNotNull(collection.getService("XPathQueryService", "1.0"),
      "XPath Implementation expected.");

    // assume null reference for unknown version
    assertNull(collection.getService("XPathQueryService", "3.8"), "Unknown version.");

    // get unknown service
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException {
        return collection.getService("Unknown", "0.0");
      }
    };
    assertNull(code.run(), "No 'Unknown' service expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetParentCollection() throws Exception {
    // assume there's no parent collection
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException { return collection.getParentCollection(); }
    };
    assertNull(code.run(), "No parent collection expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetChildCollectionCount() throws Exception {
    // assume there's no child collection
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException {
        return collection.getChildCollectionCount();
      }
    };
    assertEquals(0, code.num(), "No child collection expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testListChildCollections() throws Exception {
    // assume there's no child collection
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException { return collection.listChildCollections(); }
    };
    assertEquals(0, code.strings().length, "No child collection expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetChildCollection() throws Exception {
    // assume there's no child collection
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException {
        return collection.getChildCollection("X");
      }
    };
    assertNull(code.run(), "No child collection expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetResourceCount() throws Exception {
    // tests could be added for here multiple documents
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException { return collection.getResourceCount(); }
    };
    assertEquals(1, code.num(), "One document expected.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testListResources() throws Exception {
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException { return collection.listResources(); }
    };
    final String[] resources = code.strings();
    assertEquals(1, resources.length, "One document expected.");
    assertEquals(DOC1, resources[0], "Wrong document name.");
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testCreateResource() throws Exception {
    // test unknown resource type
    try {
      collection.createResource("hans", "UnknownResource");
      fail("Resource Type is expected to be unknown.");
    } catch(final XMLDBException ignored) { }

    // test xml resource and ID creation
    Resource resource = collection.createResource(null, XMLResource.RESOURCE_TYPE);
    assertTrue(resource instanceof XMLResource, "XMLResource expected.");
    assertNotNull(resource.getId(), "No ID was created.");

    // test adoption of specified id
    final String id = DOC2;
    resource = collection.createResource(id, XMLResource.RESOURCE_TYPE);
    assertEquals(id, resource.getId(), "Resource has wrong ID.");

    // tests could be added for here multiple documents
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException {
        return collection.createResource(null, BinaryResource.RESOURCE_TYPE);
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

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testStoreResource() throws Exception {
    Resource resource = collection.createResource("NoContent", XMLResource.RESOURCE_TYPE);

    // try to store resource with missing content
    try {
      collection.storeResource(resource);
      fail("Resource has no contents.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_RESOURCE, ex);
    }

    // try to store erroneous content
    resource = collection.createResource("Faulty", XMLResource.RESOURCE_TYPE);
    resource.setContent("<xml");
    try {
      collection.storeResource(resource);
      fail("Resource are faulty.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.INVALID_RESOURCE, ex);
    }

    // try to store resource with missing contents
    resource = collection.createResource("Correct", XMLResource.RESOURCE_TYPE);
    resource.setContent("<xml/>");
    collection.storeResource(resource);

    // store DOM instance
    final XMLResource xml1 = (XMLResource) collection.createResource(
        DOC2, XMLResource.RESOURCE_TYPE);
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Node node = builder.parse(new File(DOCPATH, DOC2));
    xml1.setContentAsDOM(node);
    collection.storeResource(xml1);

    // store SAX stream
    final XMLResource xml2 = (XMLResource) collection.createResource(
        DOC3, XMLResource.RESOURCE_TYPE);
    final XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
    reader.setContentHandler(xml2.setContentAsSAX());
    reader.parse(new InputSource(DOCPATH + DOC3));
    collection.storeResource(xml2);
    // check number of documents
    assertEquals(4, collection.getResourceCount(), "Wrong number of documents.");

    // update document with known id
    resource = collection.createResource("Correct", XMLResource.RESOURCE_TYPE);
    resource.setContent("<XML/>");
    collection.storeResource(resource);
    // check number of documents
    assertEquals(4, collection.getResourceCount(), "Wrong number of documents.");

    checkClosed(new Code() {
      @Override
      Object run() throws XMLDBException {
        return collection.createResource("id", null);
      }
    });
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testRemoveResource() throws Exception {
    final Resource resource =
        collection.createResource("Correct", XMLResource.RESOURCE_TYPE);
    resource.setContent("<xml/>");
    collection.storeResource(resource);

    collection.removeResource(collection.getResource("Correct"));
    // check number of documents
    assertEquals(1, collection.getResourceCount(), "Wrong number of documents.");

    try {
      collection.removeResource(collection.getResource("test"));
      fail("Document does not exist.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.NO_SUCH_RESOURCE, ex);
    }

    try {
      collection.removeResource(collection.getResource(null));
      fail("Document does not exist.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.NO_SUCH_RESOURCE, ex);
    }

    checkClosed(new Code() {
      @Override
      Object run() throws XMLDBException {
        collection.removeResource(null); return null;
      }
    });
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testCreateId() throws Exception {
    final Code code = new Code() {
      @Override
      Object run() throws XMLDBException { return collection.createId(); }
    };

    // check some ids for their uniqueness
    for(int i = 0; i < 10; ++i) {
      if(collection.getResource(code.run().toString()) != null) {
        fail("Returned ID not unique.");
      }
    }
    checkClosed(code);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testIsOpen() throws Exception {
    assertTrue(collection.isOpen());
    collection.close();
    assertFalse(collection.isOpen());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testClose() throws Exception {
    collection.close();
    collection.close();
    assertFalse(collection.isOpen());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetProperty() throws Exception {
    assertNull(collection.getProperty("ProbablyUnknown"));

    // the following tests are database specific...
    assertEquals(COLL, collection.getProperty("name"));
    assertEquals("false", collection.getProperty("casesens"));
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testSetProperty() throws Exception {
    try {
      collection.setProperty("ProbablyUnknown", "on");
      fail("Invalid key was assigned.");
    } catch(final XMLDBException ex) {
      checkCode(ErrorCodes.VENDOR_ERROR, ex);
    }

    // the following tests are database specific...
    collection.setProperty("name", "NewName");
    collection.setProperty("name", COLL);

    try {
      collection.setProperty("time", "ABC");
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
    assertEquals(exp, ex.errorCode, "Wrong error code.");
  }

  /**
   * Runs the specified code in a closed collection state.
   * @param code code to be executed
   */
  private void checkClosed(final Code code) {
    try {
      collection.close();
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
