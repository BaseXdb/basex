package org.basex.test.examples;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;

/**
 * This example demonstrates how databases can be created from remote XML
 * documents, and how XQuery can be used to locally update the document and
 * perform full-text requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class WikiExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  private WikiExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== WikiExample ===");

    // ----------------------------------------------------------------------
    // Create a database from a remote XML document
    System.out.println("\n* Create a database from a file via http.");

    final String doc = "http://en.wikipedia.org/wiki/Wikipedia";
    new CreateDB(doc, "WikiExample").execute(CONTEXT);

    // -------------------------------------------------------------------------
    // Insert a node before the closing body tag
    // N.B. do not forget to specify the namespace
    System.out.println("\n* Update the document.");

    new XQuery(
        "declare namespace xhtml='http://www.w3.org/1999/xhtml';" +
        "insert node " +
        "  <xhtml:p>I will match the following query because I contain" +
        "   the terms 'ARTICLE' and 'EDITABLE'. :-)</xhtml:p> " +
        "into //xhtml:body"
    ).execute(CONTEXT, OUT);

    // ----------------------------------------------------------------------
    // Match all paragraphs' textual contents against
    // 'edit.*' AND ('article' or 'page').
    System.out.println("\n* Perform a full-text query:");

    new XQuery(
        "declare namespace xhtml='http://www.w3.org/1999/xhtml';" +
        "for $x in //xhtml:p/text()" +
        "where $x contains text ('edit.*' ftand ('article' ftor 'page')) " +
        "  using wildcards distance at most 10 words " +
        "return <p>{ $x }</p>"
    ).execute(CONTEXT, OUT);

    // ----------------------------------------------------------------------
    // Drop the database
    System.out.println("\n\n* Drop the database.");

    new DropDB("WikiExample").execute(CONTEXT);
  }
}
