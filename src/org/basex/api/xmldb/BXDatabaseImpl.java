package org.basex.api.xmldb;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.basex.core.Context;
import org.basex.core.proc.*;

/**
 * Implementation of the Database Interface for the XMLDB:API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXDatabaseImpl implements Database {
  /** DB URI. */
  public static final String BASEXDB_URI = "basex://";
  /** Instance Name. */
  public static final String INSTANCE_NAME = "basex";
  /** Localhost Name. */
  public static final String LOCALHOST = "localhost:8080/";
  /** Conformance Level of the implementation. */
  public static final String CONFORMANCE_LEVEL = "0";
  
  /**
   * Constructor.
   */
  public BXDatabaseImpl() {
    super();
  }

  public boolean acceptsURI(final String uri) throws XMLDBException {
    throw new XMLDBException();
    //return false;
  }

  public Collection getCollection(final String uri, final String username,
      final String password) throws XMLDBException {
    // create database context
    final Context ctx = new Context();
    if(uri.startsWith(BASEXDB_URI)) {
      final String host = uri.substring(BASEXDB_URI.length());
      if(host.startsWith(LOCALHOST)) {
      final String tmp = host.substring(LOCALHOST.length());
      if(new Open(tmp).execute(ctx)) {
        return new BXCollection(ctx);
        }
      }
    }
    throw new XMLDBException(ErrorCodes.INVALID_URI);
  }

  public String getConformanceLevel() {
    return CONFORMANCE_LEVEL;
  }

  public String getName() {
    return INSTANCE_NAME;
  }

  public String getProperty(final String name) throws XMLDBException {
    // TODO Auto-generated method stub
    throw new XMLDBException();
    //return null;
  }

  public void setProperty(final String name, final String value)
      throws XMLDBException {
    // TODO Auto-generated method stub
    throw new XMLDBException();
  }
}
