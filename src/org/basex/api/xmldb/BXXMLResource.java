package org.basex.api.xmldb;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;

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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API.
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
      final DocumentBuilderFactory factory = DocumentBuilderFactory
          .newInstance();
      factory.setValidating(false);
      System.out.println(content);
      // Create the builder and parse the file
      final Document doc = factory.newDocumentBuilder().parse(
          new InputSource(new StringReader(content.toString())));
      return doc;
    } catch(final SAXException e) {
      // A parsing error occurred; the xml input is not valid
    } catch(final ParserConfigurationException e) {} catch(final IOException e) {}
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
    BaseX.notimplemented();
    return null;
  }

  public String getId() {
    return id;
  }

  public Collection getParentCollection() {
    BaseX.notimplemented();
    return null;
  }

  public String getResourceType() {
    return XMLResource.RESOURCE_TYPE;
  }

  public void setContent(final Object value) {
    content = value;
  }

  public void setContentAsDOM(final Node cont) {
    content = cont;
  }

  public ContentHandler setContentAsSAX() {
    // A custom SAX Content Handler is required to handle the SAX events
    return new SAXeParser(this);
  }

  /** SAX Parser. */
  class SAXeParser extends DefaultHandler {
    /**
     * Standard Constructor.
     * @param xmlresource XMLResource 
     */
    public SAXeParser(XMLResource xmlresource) {
        xmlContent = null;
        namespaces = null;
        resource = xmlresource;
        namespaces = new Hashtable<String, String>();
    }
    @Override
    public void characters(char ac[], int i, int j) {
      for(int k = 0; k < j; k++) {
        char c = ac[i + k];
        switch(c) {
          case 38: /* '&' */
            xmlContent.append("&amp;");
            break;

          case 60: /* '<' */
            xmlContent.append("&lt;");
            break;

          case 62: /* '>' */
            xmlContent.append("&gt;");
            break;

          case 34: /* '"' */
            xmlContent.append("&quot;");
            break;

          case 39: /* '\'' */
            xmlContent.append("&apos;");
            break;

          default:
            if(c > '\177') xmlContent.append("&#" + (int) c + ";");
            else xmlContent.append(c);
            break;
        }
      }
    }
    @Override
    public void endDocument() throws SAXException {
      try {
        resource.setContent(xmlContent);
      } catch(XMLDBException e) {
        throw new SAXException(e.getMessage());
      }
    }
    @Override
    public void endElement(String s, String s1, String s2) {
      xmlContent.append("</");
      xmlContent.append(s2);
      xmlContent.append(">");
    }
    @Override
    public void endPrefixMapping(String s) {
      namespaces.remove(s);
    }
    @Override
    public void ignorableWhitespace(char ac[], int i, int j) {
      for(int k = 0; k < j; k++)
        xmlContent.append(ac[i + k]);

    }
    @Override
    public void processingInstruction(String s, String s1) {
      xmlContent.append("<?");
      xmlContent.append(s);
      xmlContent.append(" ");
      if(s1 != null) xmlContent.append(s1);
      xmlContent.append("?>");
    }
    @Override
    public void skippedEntity(String s) {}
    @Override
    public void startDocument() {
      xmlContent = new StringBuffer();
      xmlContent.append("<?xml version=\"1.0\"?>");
    }
    @Override
    public void startElement(String s, String s1, String s2,
        Attributes attributes) {
      xmlContent.append("<");
      xmlContent.append(s2);
      for(int i = 0; i < attributes.getLength(); i++) {
        xmlContent.append(" ");
        xmlContent.append(attributes.getQName(i));
        xmlContent.append("=");
        xmlContent.append("\"");
        xmlContent.append(attributes.getValue(i));
        xmlContent.append("\"");
      }

      String s3;
      for(Enumeration enumeration = namespaces.keys(); enumeration
          .hasMoreElements(); namespaces.remove(s3)) {
        s3 = (String) enumeration.nextElement();
        xmlContent.append(" xmlns:");
        xmlContent.append(s3);
        xmlContent.append("=");
        xmlContent.append("\"");
        xmlContent.append(namespaces.get(s3));
        xmlContent.append("\"");
      }

      xmlContent.append(">");
    }
    
    @Override
    public void startPrefixMapping(String s, String s1) {
      namespaces.put(s, s1);
    }
    /** XMLResource. */
    protected XMLResource resource;
    /** StringBuffer */
    protected StringBuffer xmlContent;
    /** Hashtable */
    protected Hashtable<String, String> namespaces;
  }

  /**
   * Returns the pre value of the Doc
   * @return int Prevalue
   */
  public int getPre() {
    return pre;
  }
}
