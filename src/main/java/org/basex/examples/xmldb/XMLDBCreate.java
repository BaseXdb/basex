package org.basex.examples.xmldb;

import org.basex.api.xmldb.BXCollection;
import org.xmldb.api.base.*;
import org.xmldb.api.*;

/**
 * This class serves as an example for creating a database with the XML:DB API.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class XMLDBCreate {
  /** Database driver. */
  public static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Name of the collection. */
  public static final String COLL = "XMLDBCollection";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== XMLDBCreate ===");

    try {
      // Register the database
      Class<?> c = Class.forName(DRIVER);
      Database db = (Database) c.newInstance();
      DatabaseManager.registerDatabase(db);

      System.out.println("\n* Create a new collection.");

      // Create a new collection
      BXCollection coll = new BXCollection(COLL, false, db);

      // Close the connection
      coll.close();

      System.out.println("\n* Create existing collection.");

      // Open an existing collection
      coll = new BXCollection(COLL, true, db);

      // Close the connection
      coll.close();

    } catch(final XMLDBException ex) {
      // Handle exceptions
      System.err.println("XML:DB Exception occured " + ex.errorCode);
    }
  }
}
