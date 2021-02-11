package org.basex.examples.local;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This class shows the basic usage of the BaseX collection commands.
 * Collections provide access to several XML documents inside one database.
 * Collections may be created by importing a folder or adding single files.
 * For some XQuery examples in a collection context,
 * please see {@link QueryCollection}
 *
 * @author BaseX Team 2005-21, BSD License
 * @author BaseXTeam
 */
public final class CreateCollection {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String... args) throws BaseXException {
    // Create database context
    Context context = new Context();

    System.out.println("=== CreateCollection ===");

    // You can modify the CREATEFILTER property to import XML
    // files with suffixes other than XML (for example KML):
    new Set("CREATEFILTER", "*.xml").execute(context);

    // Variant 1:
    // Create a collection and add all documents within the specified path
    System.out.println("\n* Create a collection.");

    new CreateDB("Collection", "src/main/resources/").execute(context);
    new DropDB("Collection").execute(context);

    // Variant 2:
    // Or: Create an empty collection, add documents in a second pass
    // and optimize the database to refresh the index structures
    System.out.println("\n* Create an empty collection and add documents.");

    new CreateDB("Collection").execute(context);
    new Add("", "src/main/resources/").execute(context);
    new Optimize().execute(context);

    // Remove a single document from the collection
    System.out.println("\n* Remove a single document.");

    new Delete("test.xml").execute(context);

    // Show information on the currently opened database
    System.out.println("\n* Show database information:");

    System.out.println(new InfoDB().execute(context));

    // Drop the database
    System.out.println("\n* Drop the collection.");

    new DropDB("Collection").execute(context);

    // Close the database context
    context.close();
  }
}
