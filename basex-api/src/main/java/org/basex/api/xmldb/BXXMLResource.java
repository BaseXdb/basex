package org.basex.api.xmldb;

import static org.basex.util.Token.*;

import java.io.*;

import javax.xml.parsers.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-13, BSD License
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
  int pos;

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
    pos = p;
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
    pos = p;
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
        final Serializer ser = Serializer.get(ao);
        if(data != null) {
          ser.serialize(new DBNode(data, pos));
        } else if(result != null) {
          result.serialize(ser, pos);
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
        content = new IOFile((File) value).read();
      } catch(final IOException ex) {
        throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_CONT +
                '\n' + ex.getMessage());
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
    int p = pos;
    while(p >= 0) {
      final int k = data.kind(p);
      if(k == Data.DOC) return string(data.text(p, true));
      p = data.parent(p, k);
    }
    return null;
  }

  @Override
  public Node getContentAsDOM() {
    if(!(content instanceof Node)) content = new BXDoc(new DBNode(data, pos));
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
  public void getContentAsSAX(final ContentHandler handler) throws XMLDBException {
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
    // ..might be replaced by a custom SAX content handler in future
    final MemBuilder mb = new MemBuilder("", Parser.emptyParser(new MainOptions()));
    mb.init();
    return new BXSAXContentHandler(this, mb);
  }

  /** SAX parser. */
  private static final class BXSAXContentHandler extends SAXHandler {
    /** XMLResource. */
    private final BXXMLResource res;

    /**
     * Default constructor.
     * @param mb memory builder
     * @param r resource
     */
    BXSAXContentHandler(final BXXMLResource r, final MemBuilder mb) {
      super(mb, false, false);
      res = r;
    }

    @Override
    public void endDocument() throws SAXException {
      try {
        res.content = new DBNode(((MemBuilder) builder).data()).serialize().toArray();
      } catch(final QueryException ex) {
        error(new BaseXException(ex));
      }
    }
  }
}
