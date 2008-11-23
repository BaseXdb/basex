package org.basex.api.xmldb;

import java.io.IOException;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.io.IO;
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
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public final class BXCollectionManagementService implements
    CollectionManagementService, BXXMLDBText {

  /** Service constant. */
  static final String MANAGEMENT = "CollectionManagementService";
  /** Service constant. */
  static final String VERSION = "1.0";
  /** BXCollection col. */
  private Collection coll;

  /**
   * Standard Constructor.
   * @param c Collection reference
   */
  public BXCollectionManagementService(final Collection c) {
    coll = c;
  }

  /**
   * Creates a new collection. Note that a new collection equals the creation
   * of a new database.
   * @param name name of collection
   * @return collection
   * @throws XMLDBException exception
   */
  public Collection createCollection(final String name) throws XMLDBException {
    coll = new BXCollection(create(name));
    return coll;
  }

  /**
   * Creates a new collection and returns the context.
   * @param name collection name
   * @return context
   * @throws XMLDBException exception
   */
  public static Context create(final String name) throws XMLDBException {
    // Creates a new database context
    try {
      final Context ctx = new Context();
      final Parser p = new Parser(IO.get(name)) {
        @Override
        public void parse(final Builder build) { }
        @Override
        public String det() { return ""; }
        @Override
        public String head() { return ""; }
        @Override
        public double prog() { return 0; }
      };
      ctx.data(CreateDB.xml(p, name));
      return ctx;
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ex.getMessage());
    }
  }

  public void removeCollection(final String name) {
    // apply context reference to possibly close database first
    new DropDB(name).execute(((BXCollection) coll).ctx);
  }

  public String getName() {
    return MANAGEMENT;
  }

  public String getVersion() {
    return VERSION;
  }

  public void setCollection(final Collection c) {
    coll = c;
  }

  public String getProperty(final String nm) {
    return null;
  }

  public void setProperty(final String nm, final String value)
      throws XMLDBException {
    throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ERR_PROP + nm);
  }
}
