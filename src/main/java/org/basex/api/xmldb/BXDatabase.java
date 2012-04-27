package org.basex.api.xmldb;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.Set;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;

/**
 * Implementation of the Database Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXDatabase implements Database, BXXMLDBText {
  /** Database context. */
  public final Context ctx = new Context();

  @Override
  public boolean acceptsURI(final String uri) throws XMLDBException {
    getCollectionName(uri);
    return true;
  }

  @Override
  public Collection getCollection(final String uri, final String user,
      final String password) throws XMLDBException {

    // create database context
    final String name = getCollectionName(uri);
    final boolean exists = ctx.mprop.dbexists(name);
    return exists ? new BXCollection(name, true, this) : null;
  }

  @Override
  public String getConformanceLevel() {
    return CONFORMANCE_LEVEL;
  }

  @Override
  public String getName() {
    return NAMELC;
  }

  @Override
  public String getProperty(final String key) {
    try {
      final String prop = key.toUpperCase(Locale.ENGLISH);
      return ((Object[]) Prop.class.getField(prop).get(null))[1].toString();
    } catch(final Exception ex) {
      return null;
    }
  }

  @Override
  public void setProperty(final String key, final String value) throws XMLDBException {
    try {
      new Set(key, value).execute(ctx);
    } catch(final BaseXException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + key);
    }
  }

  /**
   * Returns the name of a collection.
   * @param uri input uri
   * @return collection name
   * @throws XMLDBException exception
   */
  private String getCollectionName(final String uri) throws XMLDBException {
    // try to extract name of collection; otherwise, throw exception
    if(uri != null) {
      final String main = uri.startsWith(XMLDBC) ? uri : XMLDBC + uri;
      if(main.startsWith(XMLDBURI)) {
        final String host = main.substring(XMLDBURI.length());
        final String lh = "localhost:" +
          ctx.mprop.num(MainProp.SERVERPORT) + '/';
        if(host.startsWith(lh)) return host.substring(lh.length());
      }
    }
    throw new XMLDBException(ErrorCodes.INVALID_URI, ERR_URI + uri);
  }
}
