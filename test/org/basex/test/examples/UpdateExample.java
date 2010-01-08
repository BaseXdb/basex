package org.basex.test.examples;

import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Close;
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
  /** The database context. */
  private Context context;
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
   * Constructor, initializing the database context and the output stream.
   */
  private UpdateExample() {
    context = new Context();
    out = new PrintOutput(System.out);
  }

  /**
   * Runs the examples.
   * @throws IOException if an error occurs while serializing the results
   * @throws BaseXException if a database command fails
   */
  private void run() throws IOException, BaseXException {
    // ----------------------------------------------------------------------
    // Create and open a new database from a remote XML document.
    System.out.println("\n=== Create a database from a file via http.");
    new CreateDB("http://en.wikipedia.org/wiki/Wikipedia", "XMLWIKI").
      execute(context, out);
    
    // ----------------------------------------------------------------------
    // XQuery Update is used here to modify the database.
    // XHTML documents can be easily manipulated via XQuery Update.
    try {
      System.out.print("=== Perform a database update.");

      // Insert a node before the closing body tag.
      // N.B. do not forget to specify the namespace
      query("declare namespace xhtml='http://www.w3.org/1999/xhtml';"
          + "insert node "
          + "<xhtml:p>I match the WIKIFIND query because I contain"
          + " article and edit. :-)</xhtml:p>"
          + " into //xhtml:body");
    } catch(final BaseXException e) {
      System.err.println(e.getMessage());
    }

    // ----------------------------------------------------------------------
    // XQuery Full Text is used to extract nodes from a Wikipedia article.
    try {
      System.out.println("=== Perform a query.");

      // Match all paragraphs text contents against 
      // 'edit.*' AND ('article' or 'page').
      query("declare namespace xhtml='http://www.w3.org/1999/xhtml';"
          + "for $x in //xhtml:p/text()"
          + "  where $x contains text (\"edit.*\" ftand "
          + "    (\"article\" ftor \"page\")) "
          + "    using wildcards distance at most 10 words "
          + "return <p>{$x}</p>");
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    }

    // ----------------------------------------------------------------------
    // Close and drop the database
    new Close().execute(context, out);
    new DropDB("XMLWIKI").execute(context, out);

    // ----------------------------------------------------------------------
    // Close the output stream
    out.close();
  }

  /**
   * This method executes an XQuery process for the given database context.
   * The results are automatically serialized and printed to a specified
   * output stream.
   *
   * @param query query to be evaluated
   * @throws BaseXException if a database command fails
   */
  private void query(final String query) throws BaseXException {
    new XQuery(query).execute(context, out);
  }
}
