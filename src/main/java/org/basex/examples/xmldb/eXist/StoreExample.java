package org.basex.examples.xmldb.eXist;

import java.io.*;

import org.basex.api.xmldb.*;
import org.xmldb.api.*;
import org.xmldb.api.base.*;

/**
 * XML:DB Example, derived from the eXist documentation
 * <a href="http://exist.sourceforge.net/devguide_xmldb.html">
 * http://exist.sourceforge.net/devguide_xmldb.html</a>
 * from Wolfgang M. Meier
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class StoreExample extends Main {
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

    // Create a new collection
    Collection col = new BXCollection("xmldb", false, database);

    // Store new XMLResource
    Resource document = col.createResource(null, "XMLResource");

    File f = new File("src/main/resources/xml/orders.xml");
    document.setContent(f);
    System.out.println("Storing document " + f + "...");
    col.storeResource(document);

    // Add second resource
    document = col.createResource(null, "XMLResource");

    f = new File("src/main/resources/xml/input.xml");
    document.setContent(f);
    System.out.println("Storing document " + f + "...");
    col.storeResource(document);

    // Print number of resources
    System.out.println("Number of resources: " + col.getResourceCount());
  }
}
