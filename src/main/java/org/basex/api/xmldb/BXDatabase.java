package org.basex.api.xmldb;

import static org.basex.core.Text.*;

import java.util.Locale;

import org.basex.core.MainProp;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

/**
 * Implementation of the Database Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
public final class BXDatabase implements Database, BXXMLDBText {
  /** Database context. */
  private final Context ctx = new Context();

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
    return exists ? new BXCollection(name, exists, ctx) : null;
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
  public void setProperty(final String key, final String value)
      throws XMLDBException {

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
      final String main = uri.startsWith(XMLDB) ? uri : XMLDB + uri;
      if(main.startsWith(XMLDBURI)) {
        final String host = main.substring(XMLDBURI.length());
        final String lh = "localhost:" +
          ctx.mprop.num(MainProp.SERVERPORT) + "/";
        if(host.startsWith(lh)) return host.substring(lh.length());
      }
    }
    throw new XMLDBException(ErrorCodes.INVALID_URI, ERR_URI + uri);
  }
}
