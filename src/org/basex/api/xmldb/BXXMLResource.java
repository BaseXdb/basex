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
    // A custom SAX Content Handler is required to handle the SAX events
    return new SAXeParser(this);
  }

  /** SAX Parser. */
  class SAXeParser extends DefaultHandler {
    /**
     * Standard Constructor.
     * @param xmlresource
     */
    public SAXeParser(XMLResource xmlresource) {
        resource = null;
        newContent = null;
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
            newContent.append("&amp;");
            break;

          case 60: /* '<' */
            newContent.append("&lt;");
            break;

          case 62: /* '>' */
            newContent.append("&gt;");
            break;

          case 34: /* '"' */
            newContent.append("&quot;");
            break;

          case 39: /* '\'' */
            newContent.append("&apos;");
            break;

          default:
            if(c > '\177') newContent.append("&#" + (int) c + ";");
            else newContent.append(c);
            break;
        }
      }
    }
    @Override
    public void endDocument() throws SAXException {
      try {
        resource.setContent(newContent.toString());
      } catch(XMLDBException e) {
        throw new SAXException(e.getMessage());
      }
      //System.out.println(newContent.toString());
    }
    @Override
    public void endElement(String s, String s1, String s2) {
      newContent.append("</");
      newContent.append(s2);
      newContent.append(">");
    }
    @Override
    public void endPrefixMapping(String s) {
      namespaces.remove(s);
    }
    @Override
    public void ignorableWhitespace(char ac[], int i, int j) {
      for(int k = 0; k < j; k++)
        newContent.append(ac[i + k]);

    }
    @Override
    public void processingInstruction(String s, String s1) {
      newContent.append("<?");
      newContent.append(s);
      newContent.append(" ");
      if(s1 != null) newContent.append(s1);
      newContent.append("?>");
    }
    @Override
    public void skippedEntity(String s) {}
    @Override
    public void startDocument() {
      newContent = new StringBuffer();
      newContent.append("<?xml version=\"1.0\"?>");
    }
    @Override
    public void startElement(String s, String s1, String s2,
        Attributes attributes) {
      newContent.append("<");
      newContent.append(s2);
      for(int i = 0; i < attributes.getLength(); i++) {
        newContent.append(" ");
        newContent.append(attributes.getQName(i));
        newContent.append("=");
        newContent.append("\"");
        newContent.append(attributes.getValue(i));
        newContent.append("\"");
      }

      String s3;
      for(Enumeration enumeration = namespaces.keys(); enumeration
          .hasMoreElements(); namespaces.remove(s3)) {
        s3 = (String) enumeration.nextElement();
        newContent.append(" xmlns:");
        newContent.append(s3);
        newContent.append("=");
        newContent.append("\"");
        newContent.append(namespaces.get(s3));
        newContent.append("\"");
      }

      newContent.append(">");
    }
    
    @Override
    public void startPrefixMapping(String s, String s1) {
      namespaces.put(s, s1);
    }
    /** XMLResource. */
    protected XMLResource resource;
    /** StringBuffer */
    protected StringBuffer newContent;
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
