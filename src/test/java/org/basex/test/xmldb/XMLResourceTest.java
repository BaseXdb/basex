package org.basex.test.xmldb;

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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public class XMLResourceTest extends TestCase {
  /** Collection. */
  private static Collection coll;
  /** Resource. */
  private static XMLResource res;

  @Before
  @Override
  protected void setUp() throws Exception {
    final Class<?> c = Class.forName(AllTests.DRIVER);
    final Database database = (Database) c.newInstance();
    coll = database.getCollection(AllTests.PATH, AllTests.LOGIN, AllTests.PW);
    res = (XMLResource) coll.getResource(AllTests.DOC1);
  }

  @After
  @Override
  protected void tearDown() throws Exception {
    coll.close();
  }

  @Test
  public void testParentCollection() throws Exception {
    assertEquals("Wrong collection name.", res.getParentCollection(), coll);
  }

  @Test
  public void testGetID() throws Exception {
    assertEquals("Wrong ID.", res.getId(), AllTests.DOC1);
  }

  @Test
  public void testGetResourceType() throws Exception {
    assertEquals("Wrong resource type.", res.getResourceType(),
        XMLResource.RESOURCE_TYPE);
  }

  @Test
  public void testGetContent() throws Exception {
    compare(AllTests.DOCPATH + AllTests.DOC1, res);
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
    final XMLResource xml = (XMLResource) coll.createResource(AllTests.DOC2,
        XMLResource.RESOURCE_TYPE);
    xml.setContent("<xml/>");
    coll.storeResource(xml);
    assertEquals("Wrong number of documents.", 2, coll.getResourceCount());

    // overwrite document with DOM contents
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document doc = builder.parse(
        new File(AllTests.DOCPATH + AllTests.DOC2));
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
      public void startElement(final String u, final String ln, final String qn,
          final Attributes a) {
        count++;
      }
      public void endDocument() {
        assertEquals("Wrong number of elements.", 2, count);
      }
    };
    ((XMLResource) coll.getResource(AllTests.DOC2)).getContentAsSAX(ch);
  }

  @Test
  public void testSetContentAsSAX() throws Exception {
    // store small document
    final XMLResource doc3 = (XMLResource) coll.createResource(AllTests.DOC3,
        XMLResource.RESOURCE_TYPE);

    final XMLReader reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(doc3.setContentAsSAX());
    reader.parse(new InputSource(AllTests.DOCPATH + AllTests.DOC3));

    coll.storeResource(doc3);
    assertEquals("Wrong number of documents.", 3, coll.getResourceCount());

    final Resource doc1 = coll.getResource(AllTests.DOC1);
    final Resource doc2 = coll.getResource(AllTests.DOC2);
    compare(AllTests.DOCPATH + AllTests.DOC1, doc1);
    compare(AllTests.DOCPATH + AllTests.DOC2, doc2);
    compare(AllTests.DOCPATH + AllTests.DOC3, doc3);
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
    final String buffer = new String(AllTests.read(file)).trim();
    assertEquals("File content differs.", buffer, cont.trim());
  }
}
