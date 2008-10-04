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
    return new BXSAXContentHandler(this);
  }

  /** SAX Parser. */
  class BXSAXContentHandler extends DefaultHandler {
    
    /** XMLResource. */
    protected XMLResource res;
    /** StringBuffer */
    protected StringBuffer cont;
    /** Hashtable */
    protected Hashtable<String, String> ns;
    
    /**
     * Standard Constructor.
     * @param xmlresource XMLResource 
     */
    public BXSAXContentHandler(XMLResource xmlresource) {
        res = xmlresource;
        ns = new Hashtable<String, String>();
    }
    @Override
    public void characters(char ac[], int i, int j) {
      for(int k = 0; k < j; k++) {
        char c = ac[i + k];
        switch(c) {
          case 38: /* '&' */
            cont.append("&amp;");
            break;

          case 60: /* '<' */
            cont.append("&lt;");
            break;

          case 62: /* '>' */
            cont.append("&gt;");
            break;

          case 34: /* '"' */
            cont.append("&quot;");
            break;

          case 39: /* '\'' */
            cont.append("&apos;");
            break;

          default:
            if(c > '\177') cont.append("&#" + (int) c + ";");
            else cont.append(c);
            break;
        }
      }
    }
    @Override
    public void endDocument() throws SAXException {
      try {
        res.setContent(cont);
      } catch(XMLDBException e) {
        throw new SAXException(e.getMessage());
      }
    }
    @Override
    public void endElement(String s, String s1, String s2) {
      cont.append("</");
      cont.append(s2);
      cont.append(">");
    }
    @Override
    public void endPrefixMapping(String s) {
      ns.remove(s);
    }
    @Override
    public void ignorableWhitespace(char ac[], int i, int j) {
      for(int k = 0; k < j; k++)
        cont.append(ac[i + k]);

    }
    @Override
    public void processingInstruction(String s, String s1) {
      cont.append("<?");
      cont.append(s);
      cont.append(" ");
      if(s1 != null) cont.append(s1);
      cont.append("?>");
    }
    @Override
    public void skippedEntity(String s) {}
    @Override
    public void startDocument() {
      cont = new StringBuffer();
      cont.append("<?xml version=\"1.0\"?>");
    }
    @Override
    public void startElement(String s, String s1, String s2,
        Attributes attributes) {
      cont.append("<");
      cont.append(s2);
      for(int i = 0; i < attributes.getLength(); i++) {
        cont.append(" ");
        cont.append(attributes.getQName(i));
        cont.append("=");
        cont.append("\"");
        cont.append(attributes.getValue(i));
        cont.append("\"");
      }

      String s3;
      for(Enumeration enumeration = ns.keys(); enumeration
          .hasMoreElements(); ns.remove(s3)) {
        s3 = (String) enumeration.nextElement();
        cont.append(" xmlns:");
        cont.append(s3);
        cont.append("=");
        cont.append("\"");
        cont.append(ns.get(s3));
        cont.append("\"");
      }

      cont.append(">");
    }
    
    @Override
    public void startPrefixMapping(String s, String s1) {
      ns.put(s, s1);
    }
  }

  /**
   * Returns the pre value of the Doc
   * @return int Prevalue
   */
  public int getPre() {
    return pre;
  }
}
