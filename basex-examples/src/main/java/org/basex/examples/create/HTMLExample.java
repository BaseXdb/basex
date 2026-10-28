package org.basex.examples.create;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This example demonstrates how to import a file in the HTML format
 * into the database. The specified input file will be converted to XML
 * if the HTML parser is found in the classpath.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HTMLExample {
  /**
   * Main test method.
   * @param args command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String... args) throws BaseXException {
    System.out.println("=== HTMLExample ===");

    // Create database context
    final Context ctx = new Context();

    // Input file and name of database
    final String file = "src/main/resources/html/news.html";
    final String name = "htmlexample";

    // Import the specified file
    System.out.println("\n* Import: \"" + file + "\".");
    new Set("PARSER", "html").execute(ctx);
    new CreateDB(name, file).execute(ctx);

    // Perform query
    System.out.println("\n* Headlines:");
    System.out.println(new XQuery("""
        for $a in //li/a
        return <news href='{ $a/@href }'>{ string($a) }</news>""").execute(ctx));

    // Drop database and close context
    System.out.println("\n* Drop database.");
    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
