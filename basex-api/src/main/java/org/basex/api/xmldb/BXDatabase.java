package org.basex.api.xmldb;

import static org.basex.api.xmldb.BXXMLDBText.*;
import static org.basex.core.Text.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.util.*;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;

/**
 * Implementation of the Database Interface for the XMLDB:API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BXDatabase implements Database {
  /** Database context. */
  final Context ctx = new Context();

  @Override
  public boolean acceptsURI(final String uri) throws XMLDBException {
    getCollectionName(uri);
    return true;
  }

  @Override
  public Collection getCollection(final String uri, final String user, final String password)
      throws XMLDBException {
    // create database context
    final String name = getCollectionName(uri);
    final boolean exists = ctx.soptions.dbExists(name);
    return exists ? new BXCollection(name, true, this) : null;
  }

  @Override
  public String getConformanceLevel() {
    return CONFORMANCE_LEVEL;
  }

  @Override
  public String getName() {
    return Prop.PROJECT;
  }

  @Override
  public String getProperty(final String name) {
    final Object value = Get.get(name.toUpperCase(Locale.ENGLISH), ctx);
    return value == null ? null : value.toString();
  }

  @Override
  public void setProperty(final String name, final String value) throws XMLDBException {
    try {
      new Set(name, value).execute(ctx);
    } catch(final BaseXException ex) {
      Util.debug(ex);
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + name);
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
        final int port = ctx.soptions.get(StaticOptions.SERVERPORT);
        final String localhost = S_LOCALHOST + ':' + port + '/';
        if(host.startsWith(localhost)) return host.substring(localhost.length());
      }
    }
    throw new XMLDBException(ErrorCodes.INVALID_URI, ERR_URI + uri);
  }
}
