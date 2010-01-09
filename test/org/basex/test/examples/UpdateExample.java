package org.basex.test.examples;

import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.io.PrintOutput;

/**
 * This example demonstrates how databases can be created from remote XML
 * documents, and how XQuery Update and Full Text can be applied on a database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class UpdateExample {
  /** The database ctx. */
  private Context ctx;
  /** Output stream, initialized by the constructor. */
  private PrintOutput out;

  /**
   * Runs the example class.
   * @param args (ignored) command-line arguments
   */
  public static void main(final String[] args) {
    try {
      new UpdateExample().run();
    } catch(final IOException e) {
      e.printStackTrace();
    } catch(final BaseXException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Constructor, initializing the database ctx and the output stream.
   */
  private UpdateExample() {
    ctx = new Context();
    out = new PrintOutput(System.out);
  }

  /**
   * Runs the examples.
   * First creates a database from an XHTML Document.
   * Then updates and fulltext-searches that Document.
   * @throws IOException if an error occurs while serializing the results
   * @throws BaseXException if a database command fails
   */
  private void run() throws IOException, BaseXException {
    // ----------------------------------------------------------------------
    // Create and open a new database from a remote XML document.
    System.out.println("\n=== Create a database from a file via http.");
    new CreateDB("http://en.wikipedia.org/wiki/Wikipedia", "XMLWIKI").
      execute(ctx, out);
    
    // -------------------------------------------------------------------------
    // Insert a node before the closing body tag.
    // N.B. do not forget to specify the namespace
    System.out.println("=== Perform a update query.");
    query("declare namespace xhtml='http://www.w3.org/1999/xhtml';"
        + "insert node "
        + "<xhtml:p>I match the WIKIFIND query because I contain"
        + " article and edit. :-)</xhtml:p>"
        + " into //xhtml:body");

    // ----------------------------------------------------------------------
    // Match all paragraphs text contents against 
    // 'edit.*' AND ('article' or 'page').
    System.out.println("=== Perform a fulltext-search.");
    query("declare namespace xhtml='http://www.w3.org/1999/xhtml';"
        + "for $x in //xhtml:p/text()"
        + "  where $x contains text (\"edit.*\" ftand "
        + "    (\"article\" ftor \"page\")) "
        + "    using wildcards distance at most 10 words "
        + "return <p>{$x}</p>");

    // ----------------------------------------------------------------------
    // Close and drop the database
    new DropDB("XMLWIKI").execute(ctx, out);

    // ----------------------------------------------------------------------
    // Close the output stream
    out.close();
  }

  /**
   * This method executes an XQuery process for the given database ctx.
   * The results are automatically serialized and printed to a specified
   * output stream.
   * BaseXExceptions are printed to System.err
   * @param query query to be evaluated
   */
  private void query(final String query)  {
    try {
      new XQuery(query).execute(ctx, out);
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    }
  }
}
