package org.basex.api.xmldb;

import java.io.IOException;
import org.w3c.dom.Document;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XMLResource;
import org.basex.BaseX;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DOCWrapper;
import org.basex.build.xml.DirParser;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Implementation of the Collection Interface for the XMLDB:API.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXCollection implements Collection {
  /** Context reference. */
  Context ctx;
  /** Boolean value if Collection is closed. */
  boolean closed;

  /**
   * Standard constructor.
   * @param c for Context
   */
  public BXCollection(final Context c) {
    ctx = c;
    closed = false;
  }

  public void close() {
    new Close().execute(ctx);
    closed = true;
  }

  public String createId() {
    //<CG> Methode zur Erstellung einer eindeutigen ID?
    BaseX.notimplemented();
    return null;
  }

  public Resource createResource(final String id, final String type)
      throws XMLDBException {
    if(isOpen()) {
      if(type.equals(XMLResource.RESOURCE_TYPE)) {
        return new BXXMLResource(null, id, -1, this);
      }
      throw new XMLDBException(ErrorCodes.UNKNOWN_RESOURCE_TYPE);
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public Collection getChildCollection(final String name) throws XMLDBException {
    if(isOpen()) {
      return null;
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public int getChildCollectionCount() throws XMLDBException {
    if(isOpen()) {
      return 0;
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public String getName() {
    return ctx.data().meta.dbname;
  }

  public Collection getParentCollection() throws XMLDBException {
    if(isOpen()) {
      return null;
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public String getProperty(final String name) {
    //<CG> Was für Properties gibt es?
    BaseX.notimplemented();
    return null;
  }

  public Resource getResource(final String id) throws XMLDBException {
    if(isOpen()) {
      byte[] idd = Token.token(id);
      int[] docs = ctx.data().doc();
      for(int i = 0; i < docs.length; i++) {
        int pre = docs[i];
        if(Token.eq(ctx.data().text(pre), idd)) {
          return new BXXMLResource(ctx.data(), id, pre, this);
        }
        return null;
      }
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public int getResourceCount() throws XMLDBException {
    if(isOpen()) {
      return ctx.data().doc().length;
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public Service getService(final String name, final String version)
      throws XMLDBException {
    if(isOpen()) {
      if(name.equals(BXQueryService.XPATH)
          || name.equals(BXQueryService.XQUERY)) return new BXQueryService(
          this, name);
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public Service[] getServices() throws XMLDBException {
    if(isOpen()) {
      return new Service[] { getService(BXQueryService.XPATH, null),
          getService(BXQueryService.XQUERY, null) };
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public boolean isOpen() {
    return !closed;
  }

  public String[] listChildCollections() throws XMLDBException {
    if(isOpen()) {
      String[] empty = {};
      return empty;
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public String[] listResources() throws XMLDBException {
    if(isOpen()) {
      int[] docs = ctx.data().doc();
      String[] resources = new String[docs.length];
      for(int i = 0; i < docs.length; i++) {
        resources[i] = Token.string(ctx.data().text(docs[i]));
      }
      return resources;
    }
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  public void removeResource(Resource res) throws XMLDBException {
    if(isOpen()) {
      if(res instanceof BXXMLResource) {
        BXXMLResource tmp = (BXXMLResource) res;
        if(Token.string(ctx.data().text(tmp.getPre())).equals(
            tmp.getDocumentId())) {
          ctx.data().delete(((BXXMLResource) res).getPre());
          ctx.data().flush();
        } else {
        throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
        }
      } else {
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
      }
    } else {
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
    }
  }

  public void setProperty(final String name, final String value) {
    //<CG> Was für Properties gibt es?
    BaseX.notimplemented();
  }

  public void storeResource(final Resource res) throws XMLDBException {
    if(isOpen()) {
      final String id = ((BXXMLResource) res).getDocumentId();
      Data tmp = null;
      final Object cont = res.getContent();
      Parser p = null;
      if(cont instanceof Document) {
        p = new DOCWrapper((Document) cont, id);
      } else {
        p = new DirParser(IO.get(cont.toString()));
      }
      try {
        tmp = new MemBuilder().build(p, id);
        final Data data = ctx.data();
        data.insert(data.size, -1, tmp);
        data.flush();
      } catch(final IOException ex) {
        BaseX.debug(ex);
        throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
      }
    } else {
    throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
    }
  }
}
