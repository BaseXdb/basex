package org.basex.api.xmldb;

import static org.junit.Assert.*;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class tests the XMLDB/API XMLResource implementation.
 *
 * @author BaseX Team 2005-20, BSD License
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
  @Before public void setUp() throws Exception {
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
  @After public void tearDown() throws Exception {
    collection.close();
    dropDB();
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testParentCollection() throws Exception {
    assertEquals("Wrong collection name.", resource.getParentCollection(), collection);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetID() throws Exception {
    assertEquals("Wrong ID.", resource.getId(), DOC1);
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetResourceType() throws Exception {
    assertEquals("Wrong resource type.", resource.getResourceType(),
        XMLResource.RESOURCE_TYPE);
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
    assertEquals("ID and DocumentID differ.", resource.getId(), resource.getDocumentId());
  }

  /**
   * Test.
   * @throws Exception any exception
   */
  @Test public void testGetContentAsDOM() throws Exception {
    final Node node = resource.getContentAsDOM();
    final String root = node.getChildNodes().item(0).getNodeName();
    assertTrue("Document instance expected.", node instanceof Document);
    assertEquals("Wrong root tag.", root, "first");
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
    assertEquals("Wrong number of documents.", 2, collection.getResourceCount());

    // overwrite document with DOM contents
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document doc = builder.parse(new File(DOCPATH, DOC2));
    xml.setContentAsDOM(doc);
    collection.storeResource(xml);
    assertEquals("Wrong number of documents.", 2, collection.getResourceCount());

    // compare content type
    assertTrue("Document expected.", xml.getContent() instanceof Document);
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
        assertEquals("Wrong number of elements.", 2, count);
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
    assertEquals("Wrong number of documents.", 3, collection.getResourceCount());

    final Resource doc1 = collection.getResource(DOC1);
    compare(DOCPATH + DOC1, doc1);
    compare(DOCPATH + DOC2, doc2);
    compare(DOCPATH + DOC3, doc3);
    collection.removeResource(doc3);
    collection.removeResource(doc2);
    assertEquals("Wrong number of documents.", 1, collection.getResourceCount());
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
    assertEquals("File content differs.", expected, result.trim());
  }
}
