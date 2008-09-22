package org.basex.api.xmldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.basex.BaseX;
import org.basex.data.Nodes;
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

  /**
   * Standard constructor.
   * @param n nodes
   */
  public BXXMLResource(final Nodes n) {
    nodes = n;
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
    if(content != null) getContent();
    // <AW> ...process content (see eXist)

    try {
      // Create a builder factory
      final DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
      factory.setValidating(false);

      // Create the builder and parse the file
      final Document doc = factory.newDocumentBuilder().parse(
          new File(nodes.data.meta.file.toString()));
      return doc;
    } catch(final SAXException e) {
      // A parsing error occurred; the xml input is not valid
    } catch(final ParserConfigurationException e) {
    } catch(final IOException e) { }
    return null;
  }

  public void getContentAsSAX(final ContentHandler handler)
      throws XMLDBException {
    if(content != null) getContent();
    // <AW> ...process content (see eXist)

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
      reader.parse(new InputSource(new FileInputStream(new File(
          nodes.data.meta.file.toString()))));
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
    return nodes.data.meta.dbname;
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
    // TODO Auto-generated method stub
  }

  public ContentHandler setContentAsSAX() {
    // TODO Auto-generated method stub
    return null;
  }
}
