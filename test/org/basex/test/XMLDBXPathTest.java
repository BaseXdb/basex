package org.basex.test;

import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

/**
 * Test for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class XMLDBXPathTest {
  /** XMLDB driver. */
  static String driver = "org.basex.api.xmldb.BXDatabaseImpl";
  /** Database/document path. */
  static String url = "xmldb:basex://localhost:8080/input";
  /** Query. */
  static String query = "//li";

  /**
   * Main Method.
   * @param args command line arguments (ignored).
   * @exception Exception Exception.
   */
  public static void main(String[] args) throws Exception {
    Collection col = null;
    try {
      Class<?> c = Class.forName(driver);
      Database database = (Database) c.newInstance();
      DatabaseManager.registerDatabase(database);
      col = DatabaseManager.getCollection(url);

      XPathQueryService service = (XPathQueryService) col.getService(
          "XPathQueryService", "1.0");
      ResourceSet resultSet = service.query(query);
      
      ResourceIterator results = resultSet.getIterator();
      
      while(results.hasMoreResources()) {
        Resource res = results.nextResource();
        System.out.println(res.getContent());
      }
    } catch(XMLDBException e) {
      System.err.println("XML:DB Exception occured " + e.errorCode);
      e.printStackTrace();
    } finally {
      if(col != null) {
        col.close();
      }
    }
  }
}
