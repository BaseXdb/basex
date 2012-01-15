package org.basex.api.xmldb;

import org.basex.core.BaseXException;
import org.basex.core.cmd.DropDB;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;

/**
 * Implementation of the CollectionManagementService Interface for the
 * XMLDB:API. Note that a database has one collection at a time,
 * so creating a new collection creates a new database as well, and the
 * specified collection reference is reset every time a database is created.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class BXCollectionManagementService implements
    CollectionManagementService, BXXMLDBText {

  /** Service constant. */
  static final String MANAGEMENT = "CollectionManagementService";
  /** Service constant. */
  private static final String VERSION = "1.0";
  /** BXCollection col. */
  private BXCollection coll;

  /**
   * Default constructor.
   * @param c collection reference
   */
  BXCollectionManagementService(final BXCollection c) {
    coll = c;
  }

  @Override
  public Collection createCollection(final String name) throws XMLDBException {
    return new BXCollection(name, false, coll.ctx);
  }

  @Override
  public void removeCollection(final String name) throws XMLDBException {
    try {
      new DropDB(name).execute(coll.ctx);
    } catch(final BaseXException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }

  @Override
  public String getName() {
    return MANAGEMENT;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public void setCollection(final Collection c) {
    coll = (BXCollection) c;
  }

  @Override
  public String getProperty(final String nm) {
    return null;
  }

  @Override
  public void setProperty(final String nm, final String value)
      throws XMLDBException {
    throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + nm);
  }
}
