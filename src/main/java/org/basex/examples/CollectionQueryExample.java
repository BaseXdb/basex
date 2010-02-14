package org.basex.examples;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;

/**
 * This class demonstrates collection relevant queries.
 * It shows how to find and query specific documents. 
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class CollectionQueryExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  protected CollectionQueryExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== CollectionQueryExample ===");

    // ------------------------------------------------------------------------
    // Create a collection from all XML documents in the 'etc' directory
    System.out.println("\n* Create a collection.");

    new CreateDB("etc/", "Collection").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // List all documents in the database
    System.out.println("\n* List all documents in the database:");

    // The XQuery base-uri() function returns a file path
    new XQuery(
        "for $doc in collection('Collection')" +
        "return <doc path='{ base-uri($doc) }'/>"
    ).execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Evaluate a query on a single document
    System.out.println("\n\n* Evaluate a query on a single document:");

    // If the name of the database is omitted in the collection() function,
    // the currently opened database will be referenced
    new XQuery(
        "for $doc in collection()" +
        "let $file-path := base-uri($doc)" +
        "where ends-with($file-path, 'factbook.xml')" +
        "return concat($file-path, ' has ', count($doc//*), ' elements')"
    ).execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Drop the database
    System.out.println("\n\n* Drop the database.");

    new DropDB("Collection").execute(CONTEXT);
  }
}
