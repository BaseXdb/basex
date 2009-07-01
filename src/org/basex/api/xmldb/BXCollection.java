package org.basex.api.xmldb;

import static org.basex.Text.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;
import org.w3c.dom.Document;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.XMLResource;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DOCWrapper;
import org.basex.build.xml.DirParser;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * Implementation of the Collection Interface for the XMLDB:API.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BXCollection implements Collection, BXXMLDBText {
  /** Context reference. */
  Context ctx;
  
  /**
   * Standard constructor.
   * @param c for Context
   */
  public BXCollection(final Context c) {
    ctx = c;
  }

  /**
   * Constructor to create a collection for the specified document.
   * @param name name of the database
   * @throws XMLDBException exception
   */
  public BXCollection(final String name) throws XMLDBException {
    try {
      ctx = new Context();
      final Parser p = new Parser(IO.get(name)) {
        @Override
        public void parse(final Builder build) { }
      };
      ctx.data(CreateDB.xml(p, name));
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }

  public String getName() {
    return ctx.data().meta.dbname;
  }

  public Service[] getServices() throws XMLDBException {
    check();
    return new Service[] {
        getService(BXQueryService.XPATH, "1.0"),
        getService(BXQueryService.XQUERY, "1.0"),
        getService(BXCollectionManagementService.MANAGEMENT, "1.0") };
  }

  public Service getService(final String nm, final String ver)
      throws XMLDBException {

    check();
    if(ver.equals("1.0")) {
      if(nm.equals(BXQueryService.XPATH) || nm.equals(BXQueryService.XQUERY))
        return new BXQueryService(this, nm, ver);
      if(nm.equals(BXCollectionManagementService.MANAGEMENT))
        return new BXCollectionManagementService(this);
    }
    return null;
  }

  public Collection getParentCollection() throws XMLDBException {
    check();
    return null;
  }

  public Collection getChildCollection(final String name)
      throws XMLDBException {
    check();
    return null;
  }

  public int getChildCollectionCount() throws XMLDBException {
    check();
    return 0;
  }

  public String[] listChildCollections() throws XMLDBException {
    check();
    return new String[] {};
  }

  public int getResourceCount() throws XMLDBException {
    check();
    return ctx.data().doc().length;
  }

  public String[] listResources() throws XMLDBException {
    check();
    final StringList sl = new StringList();
    final Data data = ctx.data();
    for(int d : data.doc()) sl.add(Token.string(data.text(d)));
    return sl.finish();
  }

  public BXXMLResource createResource(final String id, final String type)
      throws XMLDBException {

    check();
    if(type.equals(XMLResource.RESOURCE_TYPE)) {
      // create new id, if necessary
      final String uid = id == null || id.length() == 0 ? createId() : id;
      return new BXXMLResource(null, 0, uid, this);
    }
    // reject binary resources
    if(type.equals(BinaryResource.RESOURCE_TYPE)) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_BINARY);
    }
    throw new XMLDBException(ErrorCodes.UNKNOWN_RESOURCE_TYPE, ERR_TYPE + type);
  }

  public void removeResource(final Resource res) throws XMLDBException {
    check();

    // resource is no relevant xml resource
    final BXXMLResource del = checkXML(res);
    final Data data = ctx.data();

    // check if data instance refers to another database
    if(del.data != data && del.data != null) throw new XMLDBException(
        ErrorCodes.NO_SUCH_RESOURCE, ERR_UNKNOWN + data.meta.dbname);

    // find correct value and remove the node
    data.delete(getResource(del.getId()).pos);
    data.flush();
  }

  public void storeResource(final Resource res) throws XMLDBException {
    check();

    // check if resource has any contents
    BXXMLResource xml = checkXML(res);
    if(res.getContent() == null)
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, ERR_EMPTY);

    // disallow storage of resources without id
    final String id = res.getId();
    if(id == null) throw new XMLDBException(
        ErrorCodes.INVALID_RESOURCE, ERR_ID);

    // document exists - delete old one first
    final Resource old = getResource(id);
    if(old != null) removeResource(getResource(id));
    
    // create parser, dependent on input type
    final Object cont = xml.content;
    Parser p = null;
    if(cont instanceof Document) p = new DOCWrapper((Document) cont, id);
    else p = new DirParser(new IOContent((byte[]) cont, id));

    // insert document
    try {
      final Data data = ctx.data();
      data.insert(data.meta.size, -1, new MemBuilder().build(p, id));
      data.flush();
      ctx.update();
    } catch(final IOException ex) {
      BaseX.debug(ex);
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, ex.getMessage());
    }
  }

  public BXXMLResource getResource(final String id) throws XMLDBException {
    check();
    if(id == null) return null;
    final Data data = ctx.data();
    final byte[] idd = Token.token(id);
    for(final int d : data.doc()) {
      if(Token.eq(data.text(d), idd)) {
        return new BXXMLResource(data, d, id, this);
      }
    }
    return null;
  }

  /**
   * Creates a random numeric id and check if it's not already contained in the
   * database. Collisions can still occur, if resources are not immediately
   * stored in the database, so it's advisable in general to specify
   * your own IDs.
   * @return id
   * @throws XMLDBException exception
   */
  public String createId() throws XMLDBException {
    final String[] res = listResources();
    String id = null;
    do {
      id = Integer.toString(new Random().nextInt() & 0x7FFFFFFF);
    } while(exists(res, id));
    return id;
  }

  public boolean isOpen() {
    return ctx != null;
  }

  public void close() {
    if(ctx != null) new Close().execute(ctx);
    ctx = null;
  }

  public String getProperty(final String key) throws XMLDBException {
    check();
    try {
      return MetaData.class.getField(key).get(ctx.data().meta).toString();
    } catch(final Exception e) {
      return null;
    }
  }

  /**
   * Be aware what you're doing here..
   * @param key key
   * @param val value
   * @throws XMLDBException exception
   */
  public void setProperty(final String key, final String val)
      throws XMLDBException {
    check();
    try {
      final MetaData md = ctx.data().meta;
      final Field f = MetaData.class.getField(key);
      final Object k = f.get(md);
      
      if(k instanceof Boolean) {
        final boolean b = val == null ? !((Boolean) k).booleanValue() :
          val.equalsIgnoreCase(ON) || !val.equalsIgnoreCase(OFF);
        f.setBoolean(md, b);
      } else if(k instanceof Integer) {
        f.setInt(md, Integer.parseInt(val));
      } else {
        f.set(md, val);
      }
    } catch(final Exception e) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + key);
    }
  }
  
  /**
   * Checks if the specified id exists in the specified id list.
   * @param list id list
   * @param id id to be found
   * @return result of check
   */
  private boolean exists(final String[] list, final String id) {
    for(final String l : list) if(l.equals(id)) return true;
    return false;
  }

  /**
   * Checks if the collection is currently open.
   * @throws XMLDBException exception
   */
  private void check() throws XMLDBException {
    if(ctx == null) throw new XMLDBException(ErrorCodes.COLLECTION_CLOSED);
  }

  /**
   * Returns the specified resource as a project specific XML resource.
   * If that's not possible, throws an exception
   * @param res input resource
   * @return xml resource
   * @throws XMLDBException exception
   */
  private BXXMLResource checkXML(final Resource res) throws XMLDBException {
    if(!(res instanceof BXXMLResource)) {
      throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE, ERR_UNKNOWN + res);
    }
    return (BXXMLResource) res;
  }

}
