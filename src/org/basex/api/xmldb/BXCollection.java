package org.basex.api.xmldb;

import java.io.IOException;
import org.w3c.dom.Document;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XMLResource;
import org.basex.BaseX;
import org.basex.build.xml.DOCWrapper;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Close;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Implementation of the Collection Interface for the XMLDB:API.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXCollection implements Collection {
  /** Context reference. */
  Context ctx;

  /**
   * Standard constructor.
   * @param c for Context
   */
  public BXCollection(final Context c) {
    ctx = c;
  }

  public void close() {
    new Close().execute(ctx);
  }

  public String createId() {
    BaseX.notimplemented();
    return null;
  }

  public Resource createResource(final String id, final String type)
      throws XMLDBException {
    
    if(type.equals(XMLResource.RESOURCE_TYPE))
      return new BXXMLResource(null, id, -1);

    throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED);
  }

  public Collection getChildCollection(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public int getChildCollectionCount() {
    BaseX.notimplemented();
    return 0;
  }

  public String getName() {
    return ctx.data().meta.dbname;
  }

  public Collection getParentCollection() {
    BaseX.notimplemented();
    return null;
  }

  public String getProperty(final String name) {
    BaseX.notimplemented();
    return null;
  }

  public Resource getResource(final String id) throws XMLDBException {
    byte[] idd = Token.token(id);
    int[] docs = ctx.data().doc();
    for (int i = 0; i < docs.length; i++) {
      int pre = docs[i];
      if(Token.eq(ctx.data().text(pre), idd)) {
        return new BXXMLResource(ctx.data(), id, pre);
      }
    }
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }

  public int getResourceCount() {
    return ctx.data().doc().length;
  }

  public Service getService(final String name, final String version)
      throws XMLDBException {
    
    if(name.equals(BXQueryService.XPATH) || name.equals(BXQueryService.XQUERY))
      return new BXQueryService(this, name);
    
    throw new XMLDBException(ErrorCodes.NO_SUCH_SERVICE);
  }

  public Service[] getServices() throws XMLDBException {
    return new Service[] {
        getService(BXQueryService.XPATH, null),
        getService(BXQueryService.XQUERY, null)
    };
  }

  public boolean isOpen() {
    return ctx.data() != null;
  }

  public String[] listChildCollections() {
    BaseX.notimplemented();
    return null;
  }

  public String[] listResources() {
    int[] docs = ctx.data().doc();
    String[] resources = new String[docs.length];
    for(int i = 0; i < docs.length; i++) {
      resources[i] = Token.string(ctx.data().text(docs[i]));
    }
    return resources;
  }
  
  public void removeResource(Resource res) {
    ctx.data().delete(((BXXMLResource) res).getPre());
    ctx.data().flush();
  }

  public void setProperty(final String name, final String value) {
    BaseX.notimplemented();
  }

  public void storeResource(final Resource res) throws XMLDBException {
    final String id = res.getId();
    
    Data tmp = null;
    try {
      final Object cont = res.getContent();
      
      if(cont instanceof Document) {
        tmp = CreateDB.xml(new DOCWrapper((Document) cont, id), id);
      } else {
        tmp = CreateDB.xml(IO.get(cont.toString()), id);
      }
      
      final Data data = ctx.data();
      data.insert(data.size, -1, tmp);
      data.flush();
      tmp.close();
      DropDB.drop(id);
    } catch(final IOException ex) {
      BaseX.debug(ex);
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    }
  }
}
