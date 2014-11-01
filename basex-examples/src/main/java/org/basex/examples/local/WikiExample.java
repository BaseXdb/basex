package org.basex.examples.local;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This example demonstrates how databases can be created from remote XML
 * documents, and how XQuery can be used to locally update the document and
 * perform full-text requests.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class WikiExample {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {
    // Database context.
    Context context = new Context();

    System.out.println("=== WikiExample ===");

    // Create a database from a remote XML document
    System.out.println("\n* Create a database from a file via http.");

    // Use internal parser to skip DTD parsing
    new Set("intparse", true).execute(context);

    final String doc = "http://en.wikipedia.org/wiki/Wikipedia";
    new CreateDB("WikiExample", doc).execute(context);

    // Insert a node before the closing body tag
    // N.B. do not forget to specify the namespace
    System.out.println("\n* Update the document.");

    new XQuery(
        "declare namespace xhtml='http://www.w3.org/1999/xhtml';" +
        "insert node " +
        "  <p>I will match the following query because I contain " +
        "the terms 'ARTICLE' and 'EDITABLE'. :-)</p> " +
        "into //body"
    ).execute(context);

    // Match all paragraphs' textual contents against
    // 'edit.*' AND ('article' or 'page')
    System.out.println("\n* Perform a full-text query:");

    System.out.println(new XQuery(
        "declare namespace xhtml='http://www.w3.org/1999/xhtml';" +
        "for $x in //p/text()" +
        "where $x contains text ('edit.*' ftand ('article' ftor 'page')) " +
        "  using wildcards distance at most 10 words " +
        "return <p>{ $x }</p>"
    ).execute(context));

    // ----------------------------------------------------------------------
    // Drop the database
    System.out.println("\n* Drop the database.");

    new DropDB("WikiExample").execute(context);

    // ------------------------------------------------------------------------
    // Close the database context
    context.close();
  }
}
