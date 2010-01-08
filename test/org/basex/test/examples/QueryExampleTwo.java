package org.basex.test.examples;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.io.PrintOutput;

/**
 * Some Examples for XQuery Evaluation. This class focusses on some more
 * scenarios for XQuery Processing in BaseX. For further information on BaseX
 * Client-Side abilities in XQuery Processing please see:
 * @see XQueryExample#main(String[])
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class QueryExampleTwo {
  /** The current database Context. */
  static final Context CONTEXT = new Context();

  /**
   * insert a node before the closing body tag. 
   * N.B. do not forget to preserve the namespace.
   */
  private static final String UPDATE = "declare namespace xhtml="
      + "\"http://www.w3.org/1999/xhtml\";" 
      + "insert node "
      + "<xhtml:p>I match the WIKIFIND query because I contain"
      + " article and edit. :-)</xhtml:p>" 
      + " into //xhtml:body";

  /**
   * Matches all paragraphs text contents against 'edit.*' AND ('article' or
   * 'page').
   */
  private static final String WIKIFIND = "declare namespace xhtml="
      + "\"http://www.w3.org/1999/xhtml\";" + "for $x in //xhtml:p/text()"
      + "   where $x contains text (\"edit.*\" ftand "
      + "     (\"article\" ftor \"page\")) "
      + "     using wildcards distance at most 10 words "
      + "return <p>{$x}</p>";

  /**
   * PrintOutput Context. Point the PrintOutput to whatever file you like to
   * store the serializing results in a file. You may as well point it to
   * System.out.
   */
  private PrintOutput out;

  /**
   * Starts the QueryExample from the Commandline.
   * @param args not used.
   */
  public static void main(final String[] args) {
    final QueryExampleTwo queryExample = new QueryExampleTwo();
    try {
      queryExample.run();
    } catch(final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets up the example XMLSerializer instance.
   */
  private QueryExampleTwo() {
    out = new PrintOutput(System.out);
  }

  /**
   * Runs the Example Queries.
   * @throws IOException for XMLSerializer and PrintOutput errors.
   */
  private void run() throws IOException {
    // -------------------------------------------------------------------------
    // Creates a new database context, referencing the database.
    System.out.println("\n=== Create a database from a file via http.");

    // -------------------------------------------------------------------------
    // Creates a new database.
    new CreateDB("http://en.wikipedia.org/wiki/Wikipedia", "XMLWIKI").
      execute(CONTEXT);
    new Open("XMLWIKI").execute(CONTEXT);
    
    // -------------------------------------------------------------------------
    // Update your instance
    try {
      updateExample();
    } catch(final BaseXException e) {
      System.err.println(e.getMessage());
    }
    // -------------------------------------------------------------------------
    // Run the FulltextFind Query
    try {
      queryUrlExample();
    } catch(BaseXException e) {
      System.err.println(e.getMessage());
    } finally {
      // ----------------------------------------------------------------------
      // Close & Drop the Database
      new DropDB("XMLWIKI").execute(CONTEXT);
      new Close().execute(CONTEXT);

      // ----------------------------------------------------------------------
      // Close the PrintOutput stream
      out.close();
    }

  }
  /**
   * This function uses XQuery Update.
   * XHTML documents can be easily manipulated via XQuery update.
   * @throws BaseXException in case the update query contains errors.
   */
  private void updateExample() throws BaseXException {
    System.out.print("=== Updating the instance.");
    new XQuery(UPDATE).exec(CONTEXT, System.out);
  }

  
  /**
   * Runs a fulltext find query to extract specific nodes 
   * from an Wikipedia article.
   * @throws BaseXException on error.
   */
  private void queryUrlExample() throws BaseXException {
    System.out.println("\n---------------------------------------------------");
    System.out.println("== Querying XHTML");

    // Print the result:
    new XQuery(WIKIFIND).exec(CONTEXT, System.out);
  }

}
