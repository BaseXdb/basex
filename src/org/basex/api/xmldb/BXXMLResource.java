package org.basex.api.xmldb;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.data.SAXSerializer;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXXMLResource implements XMLResource {
  /** Current node context. */
  Nodes nodes;
  /** String content. */
  Object content;
  /** String id. */
  String id;
  /** Int pre. */
  int pre;

  /**
   * Standard constructor.
   * @param n nodes
   * @param iD String
   * @param preV int
   */
  public BXXMLResource(final Nodes n, final String iD, final int preV) {
    nodes = n;
    id = iD;
    pre = preV;
  }

  public Object getContent() {
    if(content == null) {
      try {
        final CachedOutput out = new CachedOutput();
        final boolean chop = nodes.data.meta.chop;
        nodes.serialize(new XMLSerializer(out, false, chop));
        content = out.toString();
      } catch(final Exception ex) {
        BaseX.debug(ex);
      }
    }
    return content;
  }

  public Node getContentAsDOM() {
    if(content == null) getContent();

    try {
      // Create a builder factory
      final DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      System.out.println(content);
      // Create the builder and parse the file
      final Document doc = factory.newDocumentBuilder().parse(
          new InputSource(new StringReader(content.toString())));
      return doc;
    } catch(final SAXException e) {
      // A parsing error occurred; the xml input is not valid
    } catch(final ParserConfigurationException e) {
    } catch(final IOException e) { }
    return null;
  }

  public void getContentAsSAX(final ContentHandler handler)
      throws XMLDBException {
    if(content == null) getContent();

    XMLReader reader = null;
    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware(true);
    saxFactory.setValidating(false);
    try {
      final SAXParser sax = saxFactory.newSAXParser();
      reader = sax.getXMLReader();
    } catch(final ParserConfigurationException pce) {
      throw new XMLDBException(1, pce.getMessage());
    } catch(final SAXException saxe) {
      saxe.printStackTrace();
      throw new XMLDBException(1, saxe.getMessage());
    }
    try {
      reader.setContentHandler(handler);
      reader.parse(new InputSource(new StringReader(content.toString())));
    } catch(final SAXException saxe) {
      saxe.printStackTrace();
      throw new XMLDBException(1, saxe.getMessage());
    } catch(final IOException ioe) {
      throw new XMLDBException(1, ioe.getMessage());
    }
  }

  public String getDocumentId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getId() {
    return id;
  }

  public Collection getParentCollection() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getResourceType() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setContent(final Object value) {
    content = value;
  }

  public void setContentAsDOM(final Node cont) {
    content = cont;
  }

  public ContentHandler setContentAsSAX() {
    SAXSerializer sax = new SAXSerializer(null);
    // A custom SAX Content Handler is required to handle the SAX events
    ContentHandler handler = sax.getContentHandler();
    return handler;
  }
  
  /**
   * Returns the pre value of the Doc
   * @return int Prevalue
   */
  public int getPre() {
    return pre;
  }
}
