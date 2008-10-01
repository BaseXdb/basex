package org.basex.api.xmldb;

import java.io.IOException;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XMLResource;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Check;
import org.basex.core.proc.Close;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.io.IO;

/**
 * Implementation of the Collection Interface for the XMLDB:API.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXCollection implements Collection {
  /** Context reference. */
  private Context ctx;

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
    // TODO Auto-generated method stub
    return null;
  }

  public Resource createResource(final String id, final String type)
      throws XMLDBException {
    if(type.equals(XMLResource.RESOURCE_TYPE)) {
        return new BXXMLResource(ctx.current(), id, -1);
    }
    throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED);
  }

  public Collection getChildCollection(final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public int getChildCollectionCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getName() {
    return ctx.data().meta.dbname;
  }

  public Collection getParentCollection() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty(final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public Resource getResource(final String id) throws XMLDBException {
    
    for (int i = 0; i < ctx.data().doc().length; i++) {
      int test = ctx.data().doc()[i];
      String name = new String(ctx.data().text(test));
      if(name.equals(id)) {
        return new BXXMLResource(ctx.current(), id, test);
      }
    };
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }

  public int getResourceCount() {
    return ctx.data().doc().length;
  }

  public Service getService(final String name, final String version)
      throws XMLDBException {
    if(name.equals("XPathQueryService")) return new BXXPathQueryService(ctx);
    throw new XMLDBException(ErrorCodes.NO_SUCH_SERVICE);
  }

  public Service[] getServices() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isOpen() {
    return new Check(ctx.data().meta.dbname).execute(ctx);
  }

  public String[] listChildCollections() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] listResources() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void removeResource(Resource res) {
    ctx.data().delete(((BXXMLResource) res).getPre());
    ctx.data().flush();
  }

  public void setProperty(final String name, final String value) {
    // TODO Auto-generated method stub
  }

  public void storeResource(final Resource res) throws XMLDBException {
    String cont = res.getContent().toString();
    try {
      /*
      Context ctx = new Context();
      new CreateDB(cont, "tmp").execute(ctx);
      ctx.data().insert(ctx.data().size, -1, ctx.data());
      ctx.data().flush();
      new DropDB("tmp").execute(ctx);
      */
      final Data tmp = CreateDB.xml(IO.get(cont), res.getId());
      ctx.data().insert(ctx.data().size, -1, tmp);
      ctx.data().flush();
      DropDB.drop(res.getId());
      
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    }
  }
}
