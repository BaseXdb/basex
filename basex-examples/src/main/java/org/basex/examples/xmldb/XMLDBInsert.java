package org.basex.examples.xmldb;

import org.xmldb.api.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * This class serves as an example for inserting an XML Document into a database with
 * the XML:DB API.
 * You first need to run {@link XMLDBCreate} in order to create the addressed database.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class XMLDBInsert {
  /** Database driver. */
  private static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Name of the referenced database. */
  private static final String DBNAME =
    "xmldb:basex://localhost:1984/XMLDBCollection";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== XMLDBInsert ===");

    // Collection instance
    Collection col = null;

    try {
      // Register the database
      Class<?> c = Class.forName(DRIVER);
      Database db = (Database) c.newInstance();
      DatabaseManager.registerDatabase(db);

      System.out.println("\n* Get collection.");

      // Receive the collection
      col = DatabaseManager.getCollection(DBNAME);

      // ID for the new document
      String id = "world";

      // Content of the new document
      String doc = "<xml>Hello World!</xml>";

      System.out.println("\n* Create new resource.");

      // Create a new XML resource with the specified ID
      XMLResource res = (XMLResource) col.createResource(id,
          XMLResource.RESOURCE_TYPE);

      // Set the content of the XML resource as the document
      res.setContent(doc);

      System.out.println("\n* Store new resource.");

      // Store the resource into the database
      col.storeResource(res);

    } catch(final XMLDBException ex) {
      // Handle exceptions
      System.err.println("XML:DB Exception occurred " + ex.errorCode);
      ex.printStackTrace();
    } finally {
      // Close the collection
      if(col != null) col.close();
    }
  }
}
