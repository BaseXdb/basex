package org.basex.examples.create;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.*;

/**
 * This example demonstrates how to import a file in the HTML format
 * into the database. The specified input file will be converted to XML
 * if TagSoup is found in the classpath.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTMLExample {
  /**
   * Main test method.
   * @param args command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== HTMLExample ===");

    // ------------------------------------------------------------------------
    // Create database context
    final Context ctx = new Context();

    // Input file and name of database
    final String file = "http://news.google.com/index.html";
    final String name = "htmlexample";

    // ------------------------------------------------------------------------
    // Import the specified file
    System.out.println("\n* Import: \"" + file + "\".");

    new Set(Prop.PARSER, "html").execute(ctx);
    new CreateDB(name, file).execute(ctx);

    // ------------------------------------------------------------------------
    // Perform query
    System.out.println("\n* First 10 headlines:");

    String query =
      "(for $i in //*:h2/*:a/*:span[@class='titletext']" +
      "return <news>{ $i/text() }</news>)[position() <= 10]";
    System.out.println(new XQuery(query).execute(ctx));

    // ------------------------------------------------------------------------
    // Drop database and close context
    System.out.println("\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
