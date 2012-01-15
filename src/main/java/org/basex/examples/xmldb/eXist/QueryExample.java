package org.basex.examples.xmldb.eXist;

import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

/**
 * XML:DB Example, derived from the eXist documentation
 * <a href="http://exist.sourceforge.net/devguide_xmldb.html">
 * http://exist.sourceforge.net/devguide_xmldb.html</a>
 * from Wolfgang M. Meier
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class QueryExample extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Initialize database driver
    Class<?> cl = Class.forName(DRIVER);
    Database database = (Database) cl.newInstance();
    DatabaseManager.registerDatabase(database);

    // Get the collection
    Collection col = DatabaseManager.getCollection(URI);

    // Get query service
    XPathQueryService service = (XPathQueryService)
      col.getService("XPathQueryService", "1.0");

    // Run query
    ResourceSet result = service.query("//*[text()]");
    ResourceIterator i = result.getIterator();
    while(i.hasMoreResources()) {
      Resource r = i.nextResource();
      System.out.println(r.getContent());
    }
  }
}
