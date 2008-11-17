package org.basex.test.examples;

import org.xmldb.api.base.*;
import org.xmldb.api.*;

/**
 * This class serves an example for creating a Database with the XML:DB API.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author BaseX-Group
 */
public final class XMLDBExample {
  /** Database Driver. */
  private static final String DRIVER = "org.basex.api.xmldb.BXDatabase";

   /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
  try {
         Class c = Class.forName(DRIVER);

         Database database = (Database) c.newInstance();
             //Registers the Database.
         DatabaseManager.registerDatabase(database);
      }
      catch (XMLDBException e) {
         System.err.println("XML:DB Exception occured " + e.errorCode);
      }
   }
}