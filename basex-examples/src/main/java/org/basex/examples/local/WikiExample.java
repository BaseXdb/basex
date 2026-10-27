package org.basex.examples.local;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This example demonstrates how databases can be created from remote HTML
 * documents, and how XQuery can be used to locally update the document and
 * perform full-text requests.
 *
 * @author BaseX Team, BSD License
 */
public final class WikiExample {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String... args) throws BaseXException {
    // Database context.
    Context context = new Context();

    System.out.println("=== WikiExample ===");

    // Create a database from a remote HTML document
    System.out.println("\n* Create a database from a web page.");
    new Set("PARSER", "html").execute(context);
    new CreateDB("WikiExample", "https://en.wikipedia.org/wiki/Wikipedia").execute(context);

    // Insert a paragraph into the body element
    System.out.println("\n* Update the document.");
    new XQuery("""
        insert node <p>I will match the following query because I contain
          the terms 'ARTICLE' and 'EDITABLE'. :-)</p>
        into //body""").execute(context);

    // Match all paragraphs' textual contents against
    // 'edit.*' AND ('article' or 'page'), and return the beginning of each match
    System.out.println("\n* Perform a full-text query:");
    System.out.println(new XQuery("""
        for $p in //p
        where $p contains text ('edit.*' ftand ('article' ftor 'page'))
          using wildcards distance at most 10 words
        return <p>{ substring(normalize-space($p), 1, 60) }...</p>""").execute(context));

    // Drop the database
    System.out.println("\n* Drop the database.");
    new DropDB("WikiExample").execute(context);

    // Close the database context
    context.close();
  }
}
