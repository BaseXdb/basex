package org.basex.api.xmldb;

import static org.basex.core.Text.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.w3c.dom.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * Implementation of the Collection Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXCollection implements Collection, BXXMLDBText {
  /** Database context. */
  final BXDatabase db;
  /** Database context. */
  Context ctx;

  /**
   * Constructor to create/open a collection.
   * @param name name of the database
   * @param open open existing database
   * @param d database context
   * @throws XMLDBException exception
   */
  public BXCollection(final String name, final boolean open, final Database d)
      throws XMLDBException {

    db = (BXDatabase) d;
    ctx = db.ctx;
    try {
      ctx.openDB(open ? Open.open(name, ctx) :
        CreateDB.create(name, Parser.emptyParser(ctx.prop), ctx));
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }

  @Override
  public String getName() {
    return ctx.data().meta.name;
  }

  @Override
  public Service[] getServices() throws XMLDBException {
    check();
    return new Service[] {
        getService(BXQueryService.XPATH, "1.0"),
        getService(BXQueryService.XQUERY, "1.0"),
        getService(BXCollectionManagementService.MANAGEMENT, "1.0") };
  }

  @Override
  public Service getService(final String nm, final String ver) throws XMLDBException {
    check();
    if(ver.equals("1.0")) {
      if(Token.eq(nm, BXQueryService.XPATH, BXQueryService.XQUERY))
        return new BXQueryService(this, nm, ver);
      if(nm.equals(BXCollectionManagementService.MANAGEMENT))
        return new BXCollectionManagementService(this);
    }
    return null;
  }

  @Override
  public Collection getParentCollection() throws XMLDBException {
    check();
    return null;
  }

  @Override
  public Collection getChildCollection(final String name) throws XMLDBException {
    check();
    return null;
  }

  @Override
  public int getChildCollectionCount() throws XMLDBException {
    check();
    return 0;
  }

  @Override
  public String[] listChildCollections() throws XMLDBException {
    check();
    return new String[] {};
  }

  @Override
  public int getResourceCount() throws XMLDBException {
    check();
    return ctx.data().resources.docs().size();
  }

  @Override
  public String[] listResources() throws XMLDBException {
    check();
    final StringList sl = new StringList();
    final Data data = ctx.data();
    final IntList il = data.resources.docs();
    for(int i = 0, is = il.size(); i < is; i++) {
      sl.add(Token.string(data.text(il.get(i), true)));
    }
    return sl.toArray();
  }

  @Override
  public BXXMLResource createResource(final String id, final String type)
      throws XMLDBException {

    check();
    if(type.equals(XMLResource.RESOURCE_TYPE)) {
      // create new id, if necessary
      final String uid = id == null || id.isEmpty() ? createId() : id;
      return new BXXMLResource(null, 0, uid, this);
    }
    // reject binary and other resources
    throw type.equals(BinaryResource.RESOURCE_TYPE) ?
      new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_BINARY) :
      new XMLDBException(ErrorCodes.UNKNOWN_RESOURCE_TYPE, ERR_TYPE + type);
  }

  @Override
  public void removeResource(final Resource res) throws XMLDBException {
    check();

    // check if the resource is an xml resource
    final BXXMLResource del = checkXML(res);
    final Data data = ctx.data();

    // check if data instance refers to another database
    if(del.data != data && del.data != null) throw new XMLDBException(
        ErrorCodes.NO_SUCH_RESOURCE, ERR_UNKNOWN + data.meta.name);

    if(!data.startUpdate()) throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_LOCK);
    data.delete(getResource(del.getId()).pre);
    ctx.update();
    data.finishUpdate();
  }

  @Override
  public void storeResource(final Resource res) throws XMLDBException {
    check();

    // check if resource has any contents
    final BXXMLResource xml = checkXML(res);
    if(res.getContent() == null)
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, ERR_EMPTY);

    // disallow storage of resources without id
    final String id = res.getId();
    if(id == null) throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, ERR_ID);

    // document exists - delete old one first
    final Resource old = getResource(id);
    if(old != null) removeResource(getResource(id));

    // create parser, dependent on input type
    final Object cont = xml.content;

    // insert document
    final Data md;
    try {
      final Parser p = cont instanceof Document ?
        new DOMWrapper((Document) cont, id, ctx.prop) :
        Parser.singleParser(new IOContent((byte[]) cont, id), ctx.prop, "");
      md = MemBuilder.build(id, p);
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, ex.getMessage());
    }

    final Data data = ctx.data();
    if(!data.startUpdate()) throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_LOCK);
    data.insert(data.meta.size, -1, md);
    ctx.update();
    data.finishUpdate();
  }

  @Override
  public BXXMLResource getResource(final String id) throws XMLDBException {
    check();
    if(id == null) return null;
    final Data data = ctx.data();
    final int pre = data.resources.doc(id);
    return pre == -1 ? null : new BXXMLResource(data, pre, id, this);
  }

  @Override
  public String createId() throws XMLDBException {
    final String[] res = listResources();
    String id;
    do {
      id = Long.toString(System.currentTimeMillis());
    } while(exists(res, id));
    return id;
  }

  @Override
  public boolean isOpen() {
    return ctx != null;
  }

  @Override
  public void close() {
    if(ctx != null) ctx.close();
    ctx = null;
  }

  @Override
  public String getProperty(final String key) throws XMLDBException {
    check();
    try {
      return MetaData.class.getField(key).get(ctx.data().meta).toString();
    } catch(final Exception ex) {
      return null;
    }
  }

  @Override
  public void setProperty(final String key, final String val) throws XMLDBException {
    check();
    try {
      final MetaData md = ctx.data().meta;
      final Field f = MetaData.class.getField(key);
      final Object k = f.get(md);

      if(k instanceof Boolean) {
        final boolean b = val == null ? !(Boolean) k :
          val.equalsIgnoreCase(TRUE);
        f.setBoolean(md, b);
      } else if(k instanceof Integer) {
        f.setInt(md, Integer.parseInt(val));
      } else {
        f.set(md, val);
      }
    } catch(final Exception ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + key);
    }
  }

  /**
   * Checks if the specified id exists in the specified id list.
   * @param list id list
   * @param id id to be found
   * @return result of check
   */
  private static boolean exists(final String[] list, final String id) {
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
  private static BXXMLResource checkXML(final Resource res) throws XMLDBException {
    if(!(res instanceof BXXMLResource)) {
      throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE, ERR_UNKNOWN + res);
    }
    return (BXXMLResource) res;
  }
}
