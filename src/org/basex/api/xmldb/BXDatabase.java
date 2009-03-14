package org.basex.api.xmldb;

import static org.basex.Text.*;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the Database Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BXDatabase implements Database, BXXMLDBText {

  public boolean acceptsURI(final String uri) throws XMLDBException {
    return getCollectionName(uri) != null;
  }

  public Collection getCollection(final String uri, final String username,
      final String password) throws XMLDBException {

    // create database context
    final String name = getCollectionName(uri);
    final Context ctx = new Context();
    return new Open(name).execute(ctx) ? new BXCollection(ctx) : null;
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
      final String main = uri.startsWith(XMLDB) ? uri : XMLDB + uri;
      if(main.startsWith(XMLDBURI)) {
        final String host = main.substring(XMLDBURI.length());
        if(host.startsWith(LOCALHOST)) {
          return host.substring(LOCALHOST.length());
        }
      }
    }
    throw new XMLDBException(ErrorCodes.INVALID_URI, ERR_URI + uri);
  }

  public String getConformanceLevel() {
    return CONFORMANCE_LEVEL;
  }

  public String getName() {
    return NAMESPACE;
  }

  public String getProperty(final String key) {
    try {
      return Prop.class.getField(key).get(null).toString();
    } catch(final Exception e) {
      return null;
    }
  }

  public void setProperty(final String key, final String value)
      throws XMLDBException {
    final Process proc = new Set(key, value);
    if(!proc.execute(null)) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + key);
    }
  }
}
