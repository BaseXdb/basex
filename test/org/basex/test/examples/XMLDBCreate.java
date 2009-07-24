package org.basex.test.examples;

import org.basex.api.xmldb.BXCollection;
import org.xmldb.api.base.*;
import org.xmldb.api.*;

/**
 * This class serves an example for creating a Database with the XML:DB API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author BaseX Team
 */
public final class XMLDBCreate {
  /** Database Driver. */
  public static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Name of the collection. */
  public static final String COLL = "XMLDBCollection";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    try {
      Class<?> c = Class.forName(DRIVER);
      Database database = (Database) c.newInstance();
      // Register the Database.
      DatabaseManager.registerDatabase(database);

      // Create a new collection
      BXCollection coll = new BXCollection(COLL);

      // Close the connection
      coll.close();

    } catch(XMLDBException e) {
      System.err.println("XML:DB Exception occured " + e.errorCode);
    }
  }
}
