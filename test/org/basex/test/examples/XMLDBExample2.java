package org.basex.test.examples;

import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

  /**
   * This class serves an example for inserting a XML Document into a 
   * Database using the XML:DB API.
   * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
   * @author BaseX-Group
   */ 
  public final class XMLDBExample2 {
    /** Database Driver. */
    private static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
    /** Name of the referenced database. */
    private static final String DBNAME = "xmldb:basex://localhost:1984/input";

     /**
     * Main method of the example class.
     * @param args (ignored) command-line arguments
     * @throws Exception exception
     */
    public static void main(final String[] args) throws Exception {
    Collection col = null;
    try {
           Class c = Class.forName(DRIVER);

           Database database = (Database) c.newInstance();
               //Registers the Database.
           DatabaseManager.registerDatabase(database);
               //Receives the Database.
           col = DatabaseManager.getCollection(DBNAME);
               //ID for the new Document.
           String id = "NewDocument";
               //Content of the new Document.
           String document = "This is the new Document!";
               //Creates a  new XMLResource with the ID.
           XMLResource resource = (XMLResource) col.createResource(id,
               XMLResource.RESOURCE_TYPE);
               //Sets the content of the XMLResource as the Document.
           resource.setContent(document);
               //Stores the Resource into the Database.
           col.storeResource(resource);
        }
        catch (XMLDBException e) {
           System.err.println("XML:DB Exception occured " + e.errorCode);
        }
        finally {
           if (col != null) {
              col.close();
           }
        }
     }
  }