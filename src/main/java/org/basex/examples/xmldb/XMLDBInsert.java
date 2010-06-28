package org.basex.examples.xmldb;

import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

/**
 * This class serves as an example for inserting a XML Document into a database
 * using the XML:DB API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class XMLDBInsert {
  /** Database driver. */
  private static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Name of the referenced database. */
  private static final String DBNAME = "xmldb:basex://localhost:1984/input";

  /** Private constructor. */
  private XMLDBInsert() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Collection instance.
    Collection col = null;

    try {
      // Register the database.
      Class<?> c = Class.forName(DRIVER);
      Database db = (Database) c.newInstance();
      DatabaseManager.registerDatabase(db);

      // Receive the collection.
      col = DatabaseManager.getCollection(DBNAME);

      // ID for the new document.
      String id = "SecondDoc";

      // Content of the new document.
      String doc = "<xml>This is the second document.</xml>";

      // Create a new XML resource with the specified ID.
      XMLResource res = (XMLResource) col.createResource(id,
          XMLResource.RESOURCE_TYPE);

      // Set the content of the XML resource as the document.
      res.setContent(doc);

      // Store the resource into the database.
      col.storeResource(res);
    } catch(final XMLDBException ex) {
      // Handle exceptions.
      System.err.println("XML:DB Exception occured " + ex.errorCode);
      ex.printStackTrace();
    } finally {
      // Close the collection.
      if(col != null) col.close();
    }
  }
}
