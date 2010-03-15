package org.basex.examples.query;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.CreateColl;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.Set;

/**
 * This class shows the basic usage of the BaseX collection commands.
 * Collections provide access to several XML documents inside one database.
 * Collections may be created by importing a folder or adding single files.
 * For some XQuery examples in a collection context,
 * please see {@link CollectionQueryExample}
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseXTeam
 */
public final class CollectionExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  protected CollectionExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== CollectionExample ===");

    // ------------------------------------------------------------------------
    // You can modify the CREATEFILTER property to import XML
    // files with suffixes other than XML (for example KML):
    new Set("CREATEFILTER", "*.xml").execute(CONTEXT);

    // Variant 1 --------------------------------------------------------------
    // Create a collection and add all documents within the specified path
    System.out.println("\n* Create a collection.");

    new CreateDB("etc/", "Collection").execute(CONTEXT);
    new DropDB("Collection").execute(CONTEXT);

    // Variant 2 --------------------------------------------------------------
    // Or: Create an empty collection, add documents in a second pass
    // and optimize the database to refresh the index structures
    System.out.println("\n* Create an empty collection and add documents.");

    new CreateColl("Collection").execute(CONTEXT);
    new Add("etc/").execute(CONTEXT);
    new Optimize().execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Remove a single document from the collection
    System.out.println("\n* Remove a single document.");

    new Delete("test.xml").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Show information on the currently opened database
    System.out.println("\n* Show database information:");

    new InfoDB().execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Drop the database
    System.out.println("\n* Drop the collection.");

    new DropDB("Collection").execute(CONTEXT);
  }
}
