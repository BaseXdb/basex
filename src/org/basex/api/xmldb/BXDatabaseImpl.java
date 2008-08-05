package org.basex.api.xmldb;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.basex.core.Context;
import org.basex.core.proc.*;

/**
 * Implementation of the Database Interface for the XMLDB:API
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

  /**
   * @see org.xmldb.api.base.Database#acceptsURI(java.lang.String)
   */
  public boolean acceptsURI(String uri) throws XMLDBException {
    throw new XMLDBException();
    //return false;
  }

  /**
   * @see org.xmldb.api.base.Database#getCollection(java.lang.String, java.lang.String, java.lang.String)
   */
  public Collection getCollection(String uri, String username, String password) throws XMLDBException {
    // create database context
    final Context ctx = new Context();
    if(uri.startsWith(BASEXDB_URI)) {
      final String host = uri.substring(BASEXDB_URI.length());
      if(host.startsWith(LOCALHOST)) {
      final String tmp = host.substring(LOCALHOST.length());
      if(new Open(tmp).execute(ctx)) {
        return new BXCollection(ctx);
      }
      } else {
        throw new XMLDBException(1, "Not implemented yet");
      }
    } else {
      throw new XMLDBException(ErrorCodes.INVALID_URI);
    }
    return null;
  }

  /**
   * @see org.xmldb.api.base.Database#getConformanceLevel()
   */
  public String getConformanceLevel() {
    return CONFORMANCE_LEVEL;
  }

  /**
   * @see org.xmldb.api.base.Database#getName()
   */
  public String getName() {
    return INSTANCE_NAME;
  }

  /**
   * @see org.xmldb.api.base.Configurable#getProperty(java.lang.String)
   */
  public String getProperty(String name) throws XMLDBException {
    // TODO Auto-generated method stub
    throw new XMLDBException();
    //return null;
  }

  /**
   * @see org.xmldb.api.base.Configurable#setProperty(java.lang.String, java.lang.String)
   */
  public void setProperty(String name, String value) throws XMLDBException {
    // TODO Auto-generated method stub
    throw new XMLDBException();
  }
}
