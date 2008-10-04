package org.basex.api.xmldb;

import java.io.IOException;
import org.w3c.dom.Document;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XMLResource;
import org.basex.BaseX;
import org.basex.build.xml.DOCWrapper;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Check;
import org.basex.core.proc.Close;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;

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
    if(type.equals(XMLResource.RESOURCE_TYPE)) {
        return new BXXMLResource(null, id, -1);
    }
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
    if(ctx.data().doc().length == 1) {
      if(new String(ctx.data().text(0)).equals(id)) {
        return new BXXMLResource(ctx.current(), id, 0);
      }
    } else {
      for (int i = 0; i < ctx.data().doc().length; i++) {
        int test = ctx.data().doc()[i];
        String name = new String(ctx.data().text(test));
        if(name.equals(id)) {
          Context tmpCtx = new Context();
          Nodes nodes = new Nodes(test, ctx.data());
          for (int j = test+1; j < ctx.data().doc()[i+1]; j++) {
            nodes.add(j);
          }
          tmpCtx.current(nodes);
          return new BXXMLResource(tmpCtx.current(), id, test);
        }
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
    return new Check(ctx.data().meta.dbname).execute(ctx);
  }

  public String[] listChildCollections() {
    BaseX.notimplemented();
    return null;
  }

  public String[] listResources() {
    String[] resources = {};
    for(int i = 0; i < ctx.data().doc().length; i++) {
      resources[i] = new String(ctx.data().text(ctx.data().doc()[i]));
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
      if(res.getContent() instanceof Document) {
        tmp = CreateDB.xml(new DOCWrapper((Document) res.getContent()), id);
      } else {
        tmp = CreateDB.xml(IO.get(res.getContent().toString()), id);
      }
      ctx.data().insert(ctx.data().size, -1, tmp);
      ctx.data().flush();
      tmp.close();
      DropDB.drop(id);
    } catch(final IOException ex) {
      ex.printStackTrace();
      
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    }
  }
}
