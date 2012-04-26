package org.basex.examples.xmldb.eXist;

import org.xmldb.api.*;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;

/**
 * XML:DB Example, derived from the eXist documentation
 * <a href="http://exist.sourceforge.net/devguide_xmldb.html">
 * http://exist.sourceforge.net/devguide_xmldb.html</a>
 * from Wolfgang M. Meier
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class RetrieveExample extends Main {
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

    // Get resource and show content
    XMLResource res = (XMLResource) col.getResource("input.xml");
    if(res == null) {
      System.out.println("document not found!");
    } else {
      System.out.println(res.getContent());
    }
  }
}
