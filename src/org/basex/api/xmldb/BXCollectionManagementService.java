package org.basex.api.xmldb;

import org.basex.core.proc.DropDB;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;

/**
 * Implementation of the CollectionManagementService Interface for the
 * XMLDB:API. Note that a BaseX database has one collection at a time,
 * so creating a new collection creates a new database as well, and the
 * specified collection reference is reset every time a database is created.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class BXCollectionManagementService implements
    CollectionManagementService, BXXMLDBText {

  /** Service constant. */
  static final String MANAGEMENT = "CollectionManagementService";
  /** Service constant. */
  static final String VERSION = "1.0";
  /** BXCollection col. */
  private BXCollection coll;

  /**
   * Standard Constructor.
   * @param c Collection reference
   */
  BXCollectionManagementService(final Collection c) {
    coll = (BXCollection) c;
  }

  /**
   * Creates a new collection. Note that a new collection equals the creation
   * of a new database.
   * @param name name of collection
   * @return collection
   * @throws XMLDBException exception
   */
  public Collection createCollection(final String name) throws XMLDBException {
    return new BXCollection(name, false);
  }

  public void removeCollection(final String name) throws XMLDBException {
    final DropDB drop = new DropDB(name);
    if(!drop.execute(coll.ctx)) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, drop.info());
    }
  }

  public String getName() {
    return MANAGEMENT;
  }

  public String getVersion() {
    return VERSION;
  }

  public void setCollection(final Collection c) {
    coll = (BXCollection) c;
  }

  public String getProperty(final String nm) {
    return null;
  }

  public void setProperty(final String nm, final String value)
      throws XMLDBException {
    throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + nm);
  }
}
