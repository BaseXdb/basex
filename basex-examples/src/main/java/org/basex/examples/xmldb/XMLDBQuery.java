package org.basex.examples.xmldb;

import org.xmldb.api.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class serves as an example for executing XPath requests with the XML:DB API.
 * You first need to run {@link XMLDBCreate} in order to create the addressed database.
 *
 * @author BaseX Team 2005-13, BSD License
 */
public final class XMLDBQuery {
  /** Database driver. */
  private static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Name of the referenced database. */
  private static final String DBNAME =
    "xmldb:basex://localhost:1984/XMLDBCollection";
  /** Sample query. */
  private static final String QUERY = "/";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== XMLDBQuery ===\n");

    System.out.println("* Run query via XML:DB:");

    // Collection instance
    Collection coll = null;

    try {
      // Register the database
      Class<?> c = Class.forName(DRIVER);
      Database db = (Database) c.newInstance();
      DatabaseManager.registerDatabase(db);

      // Receive the database
      coll = DatabaseManager.getCollection(DBNAME);

      // Receive the XPath query service
      XPathQueryService service = (XPathQueryService)
        coll.getService("XPathQueryService", "1.0");

      // Execute the query and receives all results
      ResourceSet set = service.query(QUERY);

      // Create a result iterator
      ResourceIterator iter = set.getIterator();

      // Loop through all result items
      while(iter.hasMoreResources()) {
        // Receive the next results
        Resource res = iter.nextResource();

        // Write the result to the console
        System.out.println(res.getContent());
      }
    } catch(final XMLDBException ex) {
      // Handle exceptions
      System.err.println("XML:DB Exception occurred " + ex.errorCode);
    } finally {
      // Close the collection
      if(coll != null) coll.close();
    }
  }
}
