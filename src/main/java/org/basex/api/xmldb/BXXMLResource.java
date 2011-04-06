package org.basex.api.xmldb;

import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import javax.xml.parsers.SAXParserFactory;
import org.basex.api.dom.BXDoc;
import org.basex.data.Data;
import org.basex.data.Result;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.io.IOFile;
import org.basex.query.item.DBNode;
import org.basex.util.TokenBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class BXXMLResource implements XMLResource, BXXMLDBText {
  /** Collection reference. */
  private final Collection coll;
  /** String id. */
  private String id;
  /** Query result. */
  private Result result;
  /** Cached content. */
  Object content;
  /** Data reference. */
  Data data;
  /** Pre value or result position. */
  int pre;

  /**
   * Constructor for generated results.
   * @param d content data
   * @param c Collection
   */
  BXXMLResource(final byte[] d, final Collection c) {
    content = d;
    coll = c;
  }

  /**
   * Constructor for query results.
   * @param res query result
   * @param p query counter
   * @param c Collection
   */
  BXXMLResource(final Result res, final int p, final Collection c) {
    result = res;
    coll = c;
    pre = p;
  }

  /**
   * Standard constructor.
   * @param d data reference
   * @param p pre value
   * @param i id
   * @param c collection
   */
  BXXMLResource(final Data d, final int p, final String i, final Collection c) {
    id = i;
    coll = c;
    data = d;
    pre = p;
  }

  @Override
  public Collection getParentCollection() {
    return coll;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getResourceType() {
    return XMLResource.RESOURCE_TYPE;
  }

  @Override
  public Object getContent() throws XMLDBException {
    if(content == null) {
      try {
        // serialize and cache content
        final ArrayOutput ao = new ArrayOutput();
        final XMLSerializer xml = new XMLSerializer(ao);
        if(data != null) {
          new DBNode(data, pre).serialize(xml);
        } else if(result != null) {
          result.serialize(xml, pre);
        } else {
          return null;
        }
        content = ao.toArray();
      } catch(final IOException ex) {
        throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
      }
    }
    return content instanceof byte[] ? string((byte[]) content) : content;
  }

  @Override
  public void setContent(final Object value) throws XMLDBException {
    // allow only strings, byte arrays and {@link File} instances
    if(value instanceof byte[]) {
      content = value;
    } else if(value instanceof String) {
      content = token(value.toString());
    } else if(value instanceof File) {
      try {
        content = new IOFile((File) value).content();
      } catch(final IOException ex) {
        throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_CONT +
            "\n" + ex.getMessage());
      }
    } else {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_CONT);
    }
  }

  @Override
  public String getDocumentId() throws XMLDBException {
    // throw exception if resource result from query; does not conform to the
    // specs, but many query results are not related to a document anymore
    if(result != null)
     throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_DOC);

    // resource does not result from a query - return normal id
    if(id != null) return id;
    // get document root id
    int p = pre;
    while(p >= 0) {
      final int k = data.kind(p);
      if(k == Data.DOC) return string(data.text(p, true));
      p = data.parent(p, k);
    }
    return null;
  }

  @Override
  public Node getContentAsDOM() {
    if(!(content instanceof Node)) content = new BXDoc(new DBNode(data, pre));
    return (Node) content;
  }

  @Override
  public void setContentAsDOM(final Node cont) throws XMLDBException {
    // allow only document instances...
    if(cont == null) throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    if(cont instanceof Document) content = cont;
    else throw new XMLDBException(ErrorCodes.WRONG_CONTENT_TYPE);
  }

  @Override
  public void getContentAsSAX(final ContentHandler handler)
      throws XMLDBException {

    if(handler == null) throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);

    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    try {
      // caching should be avoided and replaced by a stream reader...
      final XMLReader reader = factory.newSAXParser().getXMLReader();
      reader.setContentHandler(handler);
      reader.parse(new InputSource(new StringReader(getContent().toString())));
    } catch(final Exception pce) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, pce.getMessage());
    }
  }

  @Override
  public ContentHandler setContentAsSAX() throws XMLDBException {
    try {
      // ..might be replaced by a custom SAX content handler in future
      return new BXSAXContentHandler(this);
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }

  /** SAX parser. */
  private static final class BXSAXContentHandler extends DefaultHandler {
    /** HashMap. */
    private final HashMap<String, String> ns = new HashMap<String, String>();
    /** Cached output. */
    private final ArrayOutput out = new ArrayOutput();
    /** Serializer. */
    private final XMLSerializer xml;
    /** XMLResource. */
    private final BXXMLResource res;

    /**
     * Default constructor.
     * @param r resource
     * @throws IOException I/O exception
     */
    BXSAXContentHandler(final BXXMLResource r) throws IOException {
      xml = new XMLSerializer(out);
      res = r;
    }

    @Override
    public void characters(final char[] ac, final int i, final int j)
        throws SAXException {
      try {
        final TokenBuilder tb = new TokenBuilder();
        for(int k = 0; k < j; ++k) tb.add(ac[i + k]);
        xml.text(tb.finish());
      } catch(final IOException ex) {
        throw new SAXException(ex);
      }
    }

    @Override
    public void endDocument() {
      res.content = out.toArray();
    }

    @Override
    public void endElement(final String s, final String s1, final String s2)
        throws SAXException {
      try {
        xml.closeElement();
      } catch(final IOException ex) {
        throw new SAXException(ex);
      }
    }

    @Override
    public void ignorableWhitespace(final char[] ac, final int i, final int j)
        throws SAXException {
      characters(ac, i, j);
    }

    @Override
    public void processingInstruction(final String s, final String s1)
        throws SAXException {
      try {
        xml.pi(token(s), s1 != null ? token(s1) : EMPTY);
      } catch(final IOException ex)  {
        throw new SAXException(ex);
      }
    }

    @Override
    public void startElement(final String s, final String s1, final String s2,
        final Attributes attributes) throws SAXException {

      try {
        xml.openElement(token(s2));
        for(int i = 0; i < attributes.getLength(); ++i) xml.attribute(
            token(attributes.getQName(i)), token(attributes.getValue(i)));
        for(final String k : ns.keySet()) xml.attribute(
            concat(XMLNSC, token(k)), token(ns.get(k)));
      } catch(final IOException ex)  {
        throw new SAXException(ex);
      }
    }

    @Override
    public void startPrefixMapping(final String s, final String s1) {
      ns.put(s, s1);
    }

    @Override
    public void endPrefixMapping(final String s) {
      ns.remove(s);
    }
  }
}
