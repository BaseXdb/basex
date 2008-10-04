package org.basex.test;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.basex.data.SAXSerializer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import junit.framework.TestCase;

/**
 * This class tests the XMLDB features.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class XMLDBTest extends TestCase {
  /** XMLDB driver. */
  static String driver = "org.basex.api.xmldb.BXDatabaseImpl";
  /** Database/document path. */
  static String url = "xmldb:basex://localhost:8080/input";
  /** Collection. */
  Collection collection = null;

  @Before
  @Override
  protected void setUp() throws Exception {
    try {
      Class<?> c = Class.forName(driver);
      Database database = (Database) c.newInstance();
      DatabaseManager.registerDatabase(database);
      collection = DatabaseManager.getCollection(url);
    } catch(XMLDBException e) {
      System.err.println("XML:DB Exception occured " + e.errorCode);
      e.printStackTrace();
    }
  }

  /**
   * Test for XPath.
   * @throws Exception exception
   */
  @Test
  public void test1() throws Exception {
    XPathQueryService service = (XPathQueryService) collection.getService(
        "XPathQueryService", "1.0");
    ResourceSet resultSet = service.query("//li");
    ResourceIterator results = resultSet.getIterator();

    while(results.hasMoreResources()) {
      Resource res = results.nextResource();
      System.out.println(res.getContent());
    }
  }

  /**
   * Test XML Document Retrieval.
   * @throws Exception exception
   */
  @Test
  public void test2() throws Exception {
    String id = "input.xml";
    XMLResource resource = (XMLResource) collection.getResource(id);

    String cont = (String) resource.getContent();
    System.out.println("------XML Document Retrieval------");
    System.out.println(cont);
    System.out.println("------XML Document Retrieval END------");
  }

  /**
   * Test DOM Document Retrieval.
   * @throws Exception exception
   */
  @Test
  public void test3() throws Exception {
    String id = "input.xml";
    XMLResource resource = (XMLResource) collection.getResource(id);
    Document doc = (Document) resource.getContentAsDOM();
    System.out.println("------DOM Document Retrieval------");
    TransformerFactory.newInstance().newTransformer().transform(
        new DOMSource(doc), new StreamResult(System.out));
    System.out.println("------DOM Document Retrieval END------");
  }

  /**
   * Test SAX Document Retrieval.
   * @throws Exception exception
   */
  @Test
  public void test4() throws Exception {
    String id = "input.xml";
    XMLResource resource = (XMLResource) collection.getResource(id);
    SAXSerializer sax = new SAXSerializer(null);
    // A custom SAX Content Handler is required to handle the SAX events
    // For example with the ContentHandler from the SAXSerializer
    ContentHandler handler = sax.getContentHandler();
    resource.getContentAsSAX(handler);
  }

  /**
   * Test DOM Node Retrieval.
   * @throws Exception exception
   */
  @Test
  public void test5() throws Exception {
    String id = "input.xml";
    XMLResource resource = (XMLResource) collection.getResource(id);
    Node node = resource.getContentAsDOM();
    System.out.println("------DOM Node Retrieval------");
    TransformerFactory.newInstance().newTransformer().transform(
        new DOMSource(node), new StreamResult(System.out));
    System.out.println("------DOM Node Retrieval END------");
  }

  /**
   * Test Inserting a Text XML Document.
   * @throws Exception exception
   */
  @Test
  public void test6() throws Exception {
    String id = "test6";
    String document = "<xml>kjhjhjhj</xml>";

    XMLResource resource = (XMLResource) collection.createResource(id,
        XMLResource.RESOURCE_TYPE);
    resource.setContent(document);
    collection.storeResource(resource);
  }

  /**
   * Test Inserting a DOM Document.
   * @throws Exception exception
   */
  @Test
  public void test7() throws Exception {
    // Document is assumed to be a valid DOM document. Where this comes from is
    // outside the scope of the API
    Document document;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    document = builder.parse(new File("test7.xml"));
    String id = "test7";
    XMLResource resource = (XMLResource) collection.createResource(id,
        XMLResource.RESOURCE_TYPE);

    resource.setContentAsDOM(document);
    collection.storeResource(resource);
  }

  /**
   * Test Inserting a SAX Document.
   * @throws Exception exception
   */
  @Test
  public void test8() throws Exception {
    // File containing the XML to be inserted
    String fileName = "test8.xml";

    String id = "test8";
    XMLResource resource = (XMLResource) collection.createResource(id,
        XMLResource.RESOURCE_TYPE);

    ContentHandler handler = resource.setContentAsSAX();
    XMLReader reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(handler);
    reader.parse(new InputSource(fileName));

    collection.storeResource(resource);
  }

  /**
   * Test Deleting a Resource.
   * @throws Exception exception
   */
  @Test
  public void test9() throws Exception {
    String id = "tmp";
    collection.removeResource(collection.getResource(id));
  }
}
