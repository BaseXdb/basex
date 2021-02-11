package org.basex.api.xmldb;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API XMLResource implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XMLResourceTest extends XMLDBBaseTest {
  /** Collection. */
  private Collection collection;
  /** Resource. */
  private XMLResource resource;

  /**
   * Initializes a test.
   * @throws Exception any exception
   */
  @BeforeEach public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    final Database database = (Database) c.getDeclaredConstructor().newInstance();
    collection = database.getCollection(PATH, LOGIN, PW);
    resource = (XMLResource) collection.getResource(DOC1);
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
  @Test public void testParentCollection() throws Exception {
    assertEquals(resource.getParentCollection(), collection, "Wrong collection name.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetID() throws Exception {
    assertEquals(resource.getId(), DOC1, "Wrong ID.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetResourceType() throws Exception {
    assertEquals(resource.getResourceType(), XMLResource.RESOURCE_TYPE,
      "Wrong resource type.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetContent() throws Exception {
    compare(DOCPATH + DOC1, resource);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetDocumentID() throws Exception {
    // id and document id should be identical
    assertEquals(resource.getId(), resource.getDocumentId(), "ID and DocumentID differ.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetContentAsDOM() throws Exception {
    final Node node = resource.getContentAsDOM();
    final String root = node.getChildNodes().item(0).getNodeName();
    assertTrue(node instanceof Document, "Document instance expected.");
    assertEquals(root, "first", "Wrong root tag.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testSetContentAsDOM() throws Exception {
    // store small document
    final XMLResource xml = (XMLResource)
        collection.createResource(DOC2, XMLResource.RESOURCE_TYPE);
    xml.setContent("<xml/>");
    collection.storeResource(xml);
    assertEquals(2, collection.getResourceCount(), "Wrong number of documents.");

    // overwrite document with DOM contents
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document doc = builder.parse(new File(DOCPATH, DOC2));
    xml.setContentAsDOM(doc);
    collection.storeResource(xml);
    assertEquals(2, collection.getResourceCount(), "Wrong number of documents.");

    // compare content type
    assertTrue(xml.getContent() instanceof Document, "Document expected.");
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetContentAsSAX() throws Exception {
    final DefaultHandler ch = new DefaultHandler() {
      int count;
      @Override
      public void startElement(final String u, final String ln, final String qn,
          final Attributes a) {
        ++count;
      }
      @Override
      public void endDocument() {
        assertEquals(2, count, "Wrong number of elements.");
      }
    };
    ((XMLResource) collection.getResource(DOC1)).getContentAsSAX(ch);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testSetContentAsSAX() throws Exception {
    // store small document
    final XMLResource doc2 = (XMLResource)
        collection.createResource(DOC2, XMLResource.RESOURCE_TYPE);
    final XMLResource doc3 = (XMLResource)
        collection.createResource(DOC3, XMLResource.RESOURCE_TYPE);

    final XMLReader reader2 = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
    reader2.setContentHandler(doc2.setContentAsSAX());
    reader2.parse(new InputSource(DOCPATH + DOC2));

    final XMLReader reader3 = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
    reader3.setContentHandler(doc3.setContentAsSAX());
    reader3.parse(new InputSource(DOCPATH + DOC3));

    collection.storeResource(doc2);
    collection.storeResource(doc3);
    assertEquals(3, collection.getResourceCount(), "Wrong number of documents.");

    final Resource doc1 = collection.getResource(DOC1);
    compare(DOCPATH + DOC1, doc1);
    compare(DOCPATH + DOC2, doc2);
    compare(DOCPATH + DOC3, doc3);
    collection.removeResource(doc3);
    collection.removeResource(doc2);
    assertEquals(1, collection.getResourceCount(), "Wrong number of documents.");
  }

  /**
   * Compares an XML resource with a file on disk.
   * @param file file name
   * @param resource resource
   * @throws XMLDBException exception
   * @throws IOException I/O exception
   */
  private static void compare(final String file, final Resource resource)
      throws XMLDBException, IOException {

    // compare serialized node with input file
    final String result = resource.getContent().toString().replaceAll("\\r?\\n *", "");
    final String expected = Token.string(new IOFile(file).read()).trim();
    assertEquals(expected, result.trim(), "File content differs.");
  }
}
