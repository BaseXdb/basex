package org.basex.examples.query;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.*;

/**
 * This class demonstrates collection relevant queries.
 * It shows how to find and query specific documents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class QueryCollection {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {
    /** Database context. */
    Context context = new Context();

    System.out.println("=== CollectionQueryExample ===\n");

    // ------------------------------------------------------------------------
    // Create a collection from all XML documents in the 'etc' directory
    System.out.println("* Create a collection.");

    new CreateDB("Collection", "etc/").execute(context);

    // ------------------------------------------------------------------------
    // List all documents in the database
    System.out.println("* List all documents in the database:");

    // The XQuery base-uri() function returns a file path
    System.out.println(new XQuery(
        "for $doc in collection('Collection')" +
        "return <doc path='{ base-uri($doc) }'/>"
    ).execute(context));

    // ------------------------------------------------------------------------
    // Evaluate a query on a single document
    System.out.println("* Evaluate a query on a single document:");

    // If the name of the database is omitted in the collection() function,
    // the currently opened database will be referenced
    System.out.println(new XQuery(
        "for $doc in collection()" +
        "let $file-path := base-uri($doc)" +
        "where ends-with($file-path, 'factbook.xml')" +
        "return concat($file-path, ' has ', count($doc//*), ' elements')"
    ).execute(context));

    // ------------------------------------------------------------------------
    // Drop the database
    System.out.println("* Drop the database.");

    new DropDB("Collection").execute(context);

    // ------------------------------------------------------------------------
    // Close the database context
    context.close();
  }
}
