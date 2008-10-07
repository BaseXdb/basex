package org.basex.api.xmldb;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.basex.BaseX;
import org.basex.api.dom.BXDoc;
import org.basex.data.Data;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.xquery.item.DNode;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXXMLResource implements XMLResource {
  /** Current data reference. */
  private final Data data;
  /** String id. */
  private final String id;
  /** Int pre. */
  private final int pre;
  /** Collection reference. */
  private Collection coll;
  /** String content. */
  Object content;

  /**
   * Standard constructor.
   * @param d data reference
   * @param iD String
   * @param preV int
   * @param col Collection
   */
  public BXXMLResource(final Data d, final String iD, final int preV, final Collection col) {
    data = d;
    id = iD;
    pre = preV;
    coll = col;
  }

  public Object getContent() throws XMLDBException {
    if(content == null) {
      try {
        final CachedOutput out = new CachedOutput();
        new XMLSerializer(out).xml(data, pre);
        content = out.toString();
      } catch(final IOException ex) {
        throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
      }
    }
    return content;
  }

  public Node getContentAsDOM() {
    return content instanceof Node ? (Node) content :
      new BXDoc(new DNode(data, pre));
  }

  public void getContentAsSAX(final ContentHandler handler)
      throws XMLDBException {

    if(handler == null) throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    
    XMLReader reader = null;
    final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware(true);
    saxFactory.setValidating(false);
    try {
      final SAXParser sax = saxFactory.newSAXParser();
      reader = sax.getXMLReader();
      reader.setContentHandler(handler);
      reader.parse(new InputSource(new StringReader(getContent().toString())));
    } catch(final Exception pce) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, pce.getMessage());
    }
  }

  public String getDocumentId() {
    return id;
  }

  public String getId() {
  //<CG> Methode zur Erstellung einer eindeutigen ID?
    BaseX.notimplemented();
    return null;
  }

  public Collection getParentCollection() {
    return coll;
  }

  public String getResourceType() {
    return XMLResource.RESOURCE_TYPE;
  }

  public void setContent(final Object value) {
    content = value;
  }

  public void setContentAsDOM(final Node cont) throws XMLDBException {
    if(cont == null) throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    content = cont;
  }

  public ContentHandler setContentAsSAX() {
    // A custom SAX Content Handler is required to handle the SAX events
    return new BXSAXContentHandler(this);
  }

  /** SAX Parser. */
  class BXSAXContentHandler extends DefaultHandler {
    
    /** XMLResource. */
    protected BXXMLResource res;
    /** StringBuffer */
    protected StringBuilder cont;
    /** Hashtable */
    protected Hashtable<String, String> ns;
    
    /**
     * Standard Constructor.
     * @param xmlresource XMLResource 
     */
    public BXSAXContentHandler(BXXMLResource xmlresource) {
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
    public void endDocument() {
      res.setContent(cont);
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
      cont = new StringBuilder();
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
   * @return pre value
   */
  public int getPre() {
    return pre;
  }
  
  /**
   * Returns the Data of the Doc
   * @return Data value
   */
  public Data getData() {
    return data;
  }
 
}
