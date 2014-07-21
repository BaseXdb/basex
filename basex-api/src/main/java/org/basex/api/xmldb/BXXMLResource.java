package org.basex.api.xmldb;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.parse.xml.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * Implementation of the XMLResource Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class BXXMLResource implements XMLResource, BXXMLDBText {
  /** Collection reference. */
  private final Collection coll;
  /** String id. */
  private String id;
  /** Query result. */
  private Item item;
  /** Cached content. */
  Object content;
  /** Data reference. */
  Data data;
  /** Pre value or result position. */
  int pre;

  /**
   * Constructor for generated results.
   * @param content content data
   * @param coll Collection
   */
  BXXMLResource(final byte[] content, final Collection coll) {
    this.content = content;
    this.coll = coll;
  }

  /**
   * Constructor for query results.
   * @param item query result
   * @param coll Collection
   */
  BXXMLResource(final Item item, final Collection coll) {
    this.item = item;
    this.coll = coll;
  }

  /**
   * Standard constructor.
   * @param data data reference
   * @param pre pre value
   * @param id id
   * @param coll collection
   */
  BXXMLResource(final Data data, final int pre, final String id, final Collection coll) {
    this.id = id;
    this.coll = coll;
    this.data = data;
    this.pre = pre;
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
          ser.serialize(new DBNode(data, pre));
        } else if(item != null) {
          ser.serialize(item);
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
    // throw exception if resource results from query; does not conform to the
    // specs, but many query results are not related to a document anymore
    if(item != null) throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_DOC);

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
  public void getContentAsSAX(final ContentHandler handler) throws XMLDBException {
    if(handler == null) throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    try {
      new XmlParser().contentHandler(handler).parse(new ArrayInput(getContent().toString()));
    } catch(final Exception pce) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, pce.getMessage());
    }
  }

  @Override
  public ContentHandler setContentAsSAX() {
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
     * @param builder memory builder
     * @param resource resource
     */
    BXSAXContentHandler(final BXXMLResource resource, final MemBuilder builder) {
      super(builder, false, false);
      res = resource;
    }

    @Override
    public void endDocument() throws SAXException {
      try {
        res.content = new DBNode(((MemBuilder) builder).data()).serialize().toArray();
      } catch(final QueryIOException ex) {
        error(new BaseXException(ex));
      }
    }
  }
}
