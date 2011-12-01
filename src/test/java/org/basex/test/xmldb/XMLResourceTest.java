package org.basex.test.xmldb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import junit.framework.TestCase;

/**
 * This class tests the XMLDB/API XMLResource implementation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class XMLResourceTest extends XMLDBBaseTest {
  /** Collection. */
  private Collection coll;
  /** Resource. */
  private XMLResource res;

  @Before
  public void setUp() throws Exception {
    createDB();
    final Class<?> c = Class.forName(DRIVER);
    final Database database = (Database) c.newInstance();
    coll = database.getCollection(PATH, LOGIN, PW);
    res = (XMLResource) coll.getResource(DOC1);
  }

  @After
  public void tearDown() throws Exception {
    coll.close();
    dropDB();
  }

  @Test
  public void testParentCollection() throws Exception {
    assertEquals("Wrong collection name.", res.getParentCollection(), coll);
  }

  @Test
  public void testGetID() throws Exception {
    assertEquals("Wrong ID.", res.getId(), DOC1);
  }

  @Test
  public void testGetResourceType() throws Exception {
    assertEquals("Wrong resource type.", res.getResourceType(),
        XMLResource.RESOURCE_TYPE);
  }

  @Test
  public void testGetContent() throws Exception {
    compare(DOCPATH + DOC1, res);
  }

  @Test
  public void testGetDocumentID() throws Exception {
    // id and document id should be identical
    assertEquals("ID and DocumentID differ.", res.getId(), res.getDocumentId());
  }

  @Test
  public void testGetContentAsDOM() throws Exception {
    final Node node = res.getContentAsDOM();
    final String root = node.getChildNodes().item(0).getNodeName();
    assertTrue("Document instance expected.", node instanceof Document);
    assertEquals("Wrong root tag.", root, "first");
  }

  @Test
  public void testSetContentAsDOM() throws Exception {
    // store small document
    final XMLResource xml = (XMLResource) coll.createResource(DOC2,
        XMLResource.RESOURCE_TYPE);
    xml.setContent("<xml/>");
    coll.storeResource(xml);
    assertEquals("Wrong number of documents.", 2, coll.getResourceCount());

    // overwrite document with DOM contents
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document doc = builder.parse(
        new File(DOCPATH + DOC2));
    xml.setContentAsDOM(doc);
    coll.storeResource(xml);
    assertEquals("Wrong number of documents.", 2, coll.getResourceCount());

    // compare content type
    assertTrue("Document expected.", xml.getContent() instanceof Document);
  }

  @Test
  public void testGetContentAsSAX() throws Exception {
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
    ((XMLResource) coll.getResource(DOC1)).getContentAsSAX(ch);
  }

  @Test
  public void testSetContentAsSAX() throws Exception {
    // store small document
    final XMLResource doc2 = (XMLResource) coll.createResource(DOC2,
        XMLResource.RESOURCE_TYPE);
    final XMLResource doc3 = (XMLResource) coll.createResource(DOC3,
        XMLResource.RESOURCE_TYPE);


    final XMLReader reader2 = XMLReaderFactory.createXMLReader();
    reader2.setContentHandler(doc2.setContentAsSAX());
    reader2.parse(new InputSource(DOCPATH + DOC2));

    final XMLReader reader3 = XMLReaderFactory.createXMLReader();
    reader3.setContentHandler(doc3.setContentAsSAX());
    reader3.parse(new InputSource(DOCPATH + DOC3));

    coll.storeResource(doc2);
    coll.storeResource(doc3);
    assertEquals("Wrong number of documents.", 3, coll.getResourceCount());

    final Resource doc1 = coll.getResource(DOC1);
    compare(DOCPATH + DOC1, doc1);
    compare(DOCPATH + DOC2, doc2);
    compare(DOCPATH + DOC3, doc3);
    coll.removeResource(doc3);
    coll.removeResource(doc2);
    assertEquals("Wrong number of documents.", 1, coll.getResourceCount());
  }

  /**
   * Compares an XML resource with a file on disk.
   * @param file file name
   * @param r resource
   * @throws XMLDBException exception
   * @throws IOException I/O exception
   */
  private void compare(final String file, final Resource r)
      throws XMLDBException, IOException {

    // compare serialized node with input file
    final String cont = r.getContent().toString().replaceAll("\\r?\\n *", "");
    final String buffer = new String(read(file)).trim();
    assertEquals("File content differs.", buffer, cont.trim());
  }
}
